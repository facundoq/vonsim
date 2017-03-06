package vonemu.lang

trait ParseErrorType
case object VariableDefinitionWithLabel extends ParseErrorType
case object InvalidLabelSyntax extends ParseErrorType
case object InvalidNumberOfOperands extends ParseErrorType
case object FirstOperandHasNoComma extends ParseErrorType
case object BinaryNoOperands extends ParseErrorType
case object UnaryNoOperands extends ParseErrorType
case object UnaryInvalidOperand extends ParseErrorType

case object UnaryTooManyOperands extends ParseErrorType
case object BinaryOnlyOneOperand extends ParseErrorType
case object BinaryTooManyOperands extends ParseErrorType
case object InvalidNumberLiteral extends ParseErrorType
case object InvalidCharacterLiteral extends ParseErrorType
case object InvalidOperand extends ParseErrorType
case object InvalidOffsetLabel extends ParseErrorType

class ParseError(val line: Int, val reason: ParseErrorType)

class Parser {

  def parse(code: String): Either[Array[ParseError], Program] = {
    parse(code.split('\n'))
  }

  def parse(code: Array[String]): Either[Array[ParseError], Program] = {
    val lowercaseCode = code.map(_.toLowerCase)
    val result = syntacticCheck(lowercaseCode)
    return if (result.isLeft) result else return semanticCheck(result.right.get)
  }

  def syntacticCheck(code: Array[String]): Either[Array[ParseError], Program] = {
    val tokensPerLine = code.map(line => line.split(' ').map(_.trim()))
    val nonemptyTokensPerLine = tokensPerLine.filter(_.isEmpty)
    val lines = nonemptyTokensPerLine.zipWithIndex.map { case (tokens, line) => new Line(line, tokens) }
    val parsedLines = lines.map(parseLine)
    val errors = parsedLines.filter(p => p.isLeft).map(_.left.get)
    if (!errors.isEmpty) {
      return Left(errors)
    }
    val correctlyParsedLinesResults = parsedLines.filter(p => p.isRight).map(_.right.get)
    val instructions = correctlyParsedLinesResults.flatMap { case x: InstructionResult => Some(x.instruction) }
    val definitions = correctlyParsedLinesResults.flatMap { case x: VariableDefinitionResult => Some(x.definition) }

    val labels = correctlyParsedLinesResults.flatMap {
      case x: LabeledInstructionResult => Some(new JumpLabel(x.instruction.line, x.label.id))
    }

    val p = new Program(definitions, instructions, labels)

    return Right(p)
  }

  abstract class ParseLineResult()
  abstract class InstructionResult(val instruction: Instruction) extends ParseLineResult
  case class SimpleInstructionResult(i: Instruction) extends InstructionResult(i)
  case class LabeledInstructionResult(i: Instruction, val label: Label) extends InstructionResult(i)
  case class VariableDefinitionResult(val definition: VariableDefinition) extends ParseLineResult

  def labelSyntax(label: String): Option[ParseErrorType] = {
    val pattern = "^[a-zA-z][\\w]*$".r
    if (pattern.findFirstIn(label).isDefined) return None else return Some(InvalidLabelSyntax)
  }

  def parseLabel(line: Line): Either[ParseError, Option[Label]] = {
    val tokens = line.tokens
    val token = tokens(0)
    if (token.endsWith(":")) {
      tokens.drop(1)
      val labelString = token.take(token.length - 1)
      labelSyntax(labelString) match {
        case None        => return Right(Some(new Label(labelString)))
        case Some(error) => return Left(new ParseError(line.line, error))
      }
    }
    return Right(None)
  }

  def parseRegister(operand: String): Option[Register] = {
    operand match {
      case "AX" => return Option(new Register(RegisterID.a, RegisterPart.X))
      case "BX" => return Option(new Register(RegisterID.b, RegisterPart.X))
      case "CX" => return Option(new Register(RegisterID.c, RegisterPart.X))
      case "DX" => return Option(new Register(RegisterID.d, RegisterPart.X))
      case "AL" => return Option(new Register(RegisterID.a, RegisterPart.L))
      case "BL" => return Option(new Register(RegisterID.b, RegisterPart.L))
      case "CL" => return Option(new Register(RegisterID.c, RegisterPart.L))
      case "DL" => return Option(new Register(RegisterID.d, RegisterPart.L))
      case "AH" => return Option(new Register(RegisterID.a, RegisterPart.H))
      case "BH" => return Option(new Register(RegisterID.b, RegisterPart.H))
      case "CH" => return Option(new Register(RegisterID.c, RegisterPart.H))
      case "DH" => return Option(new Register(RegisterID.d, RegisterPart.H))
    }
    return None
  }
  def parseIndirectMemory(operand: String): Option[IndirectMemory] = {
    if ((operand.startsWith("[") && operand.endsWith("]")) && operand.length() > 3) {
      val registerLabel = operand.drop(1).dropRight(1)
      parseRegister(registerLabel) match {
        case Some(r) => return Some(new IndirectMemory(r))
        case None =>
      }
    }
    return None
  }
  def parseInmediate(operand: String): Option[Either[ParseErrorType, Inmediate]] = {
    val decimal = "^-?[0-9]+$".r
    val binary = "^[0-1]+b$".r
    val hexa = "^-?[0-9a-f]+h$".r
    if (decimal.findFirstMatchIn(operand).isDefined) {
      return Some(Right(new Inmediate(operand.toInt)))
    }
    if (binary.findFirstMatchIn(operand).isDefined) {
      return Some(Right(new Inmediate(Integer.parseInt(operand.dropRight(1), 2))))
    }
    if (hexa.findFirstMatchIn(operand).isDefined) {
      return Some(Right(new Inmediate(Integer.parseInt(operand.dropRight(1), 16))))
    }

    if ("^-?[0-9]".r.findFirstIn(operand).isDefined) {
      return Some(Left(InvalidNumberLiteral))
    }
    val alpha = "^''$".r
    if (operand.startsWith("'") && operand.endsWith("'")) {
      if (operand.length() == 3) {
        return Some(Right(new Inmediate(operand.charAt(1))))
      }
      return Some(Left(InvalidCharacterLiteral))
    }
    return None
  }
  def parseLabel(operand: String): Option[Label] = {
    val alpha = "^[a-z_][0-9a-z_]*$".r
    if (alpha.findFirstIn(operand).isDefined) {
      return Some(Label(operand))
    }
    return None
  }
  def parseOffset(operand: String): Option[Either[ParseErrorType, Offset]] = {
    val tokens = operand.split(" ").map(_.trim)
    if (tokens(0).equals("offset")) {
      if (tokens.length != 2) {
        return Some(Left(InvalidOffsetLabel))
      }
      parseLabel(tokens(1)) match {
        case Some(label) => return Some(Right(new Offset(label)))
        case None =>
      }
    }
    return None
  }

  def parseOperand(operandLabel: String, line: Int): Either[ParseError, Operand] = {

    parseRegister(operandLabel) match {
      case Some(register) => return Right(register)
      case None =>
    }
    parseRegister(operandLabel) match {
      case Some(indirect) => return Right(indirect)
      case None =>
    }
    parseInmediate(operandLabel) match {
      case Some(Right(r)) => return Right(r)
      case Some(Left(e))  => return Left(new ParseError(line, e))
      case None =>
    }
    parseLabel(operandLabel) match {
      case Some(r) => return Right(r)
      case None =>
    }
    parseOffset(operandLabel) match {
      case Some(Right(r)) => return Right(r)
      case Some(Left(e))  => return Left(new ParseError(line, e))
      case None =>
    }
    return Left(new ParseError(line, InvalidOperand))
  }

  def parseInstruction(line: Line): Option[Either[ParseError, Instruction]] = {
    val tokens = line.tokens
    val operatorLabel = tokens(0)
    val error = (e: ParseErrorType) => Some(Left(new ParseError(line.line, e)))

    val zeroaryOperators = Map("ret" -> Ret, "end" -> End, "hlt" -> Hlt, "popf" -> PopF, "pushf" -> PushF)
    if (zeroaryOperators.keys.exists { x => x == operatorLabel }) {
      if (tokens.length != 1) return error(InvalidNumberOfOperands)
      return Some(Right(new ZeroaryInstruction(line.line, zeroaryOperators(operatorLabel))))
    }

    val binaryOperators = Map("add" -> Add, "sub" -> Sub, "mov" -> Mov, "cmp" -> Cmp)
    if (binaryOperators.keys.exists { x => x == operatorLabel }) {
      val operator = binaryOperators(operatorLabel)
      if (tokens.length == 1) return error(BinaryNoOperands)
      val operandsLabels = tokens.drop(1).fold("")((a, b) => a + " " + b)
      if (!operandsLabels.contains(",")) return error(BinaryOnlyOneOperand)
      val operandsLabelsTokenized = operandsLabels.split(",").map(_.trim)
      if (operandsLabelsTokenized.length > 2) return error(BinaryTooManyOperands)
      var operand1Label = operandsLabelsTokenized(0)
      operand1Label = operand1Label.take(operand1Label.length - 1)
      val operand2Label = operandsLabelsTokenized(1)

      val operand1 = parseOperand(operand1Label.trim(), line.line)
      val operand2 = parseOperand(operand2Label.trim(), line.line)

      operand1 match {
        case Left(e) => return Some(Left(e))
        case Right(o1) => operand2 match {
          case Left(e2)  => return Some(Left(e2))
          case Right(o2) => return Some(Right(new BinaryInstruction(line.line, operator, o1, o2)))
        }
      }
    }
    val unaryOperators = Map("jmp" -> Jmp, "call" -> Call, "pop" -> Pop, "push" -> Push, "org" -> Org, "not" -> Not, "dec" -> Dec, "inc" -> Inc)
    if (unaryOperators.keys.exists { x => x == operatorLabel }) {
      
      if (tokens.length == 0) return error(UnaryNoOperands)
      if (tokens.length > 2) return error(UnaryTooManyOperands)
      val operator = unaryOperators(operatorLabel)
      val operand=tokens(1)
      operator match{
        case inmediateOp@ (Org | IntN) => {
          parseInmediate(operand) match{
            case Some(Left(r)) => return error(r)
            case Some(Right(o)) => return Some(Right(new UnaryInstruction(line.line,inmediateOp,o)))
            case None => return error(UnaryInvalidOperand)
          }
        }
        case s:Stack => parseRegister(operand) match{ // stack has to go first
            case Some(o) => return Some(Right(new UnaryInstruction(line.line,s,o)))
            case None => return error(UnaryInvalidOperand)
          }
        case x:UnaryArithmeticOperator => parseOperand(operand,line.line) match{ // stack has to go first
            case Left(r) => return Some(Left(r))
            case Right(o) => return Some(Right(new UnaryInstruction(line.line,x,o)))
          }
        case labelOp@ (Call | _:Jump) => parseLabel(operand) match{ // stack has to go first
            case None => return error(UnaryInvalidOperand)
            case Some(o) => return Some(Right(new UnaryInstruction(line.line,labelOp,o)))
          }
      }
    }
    return None
  }

  def parseLine(line: Line): Either[ParseError, ParseLineResult] = {
    //label
    val possibleLabel = parseLabel(line)
    var label: Option[Label] = null
    possibleLabel match {
      case Left(error) => return Left(error)
      case Right(l)    => label = l
    }
    // check if instruction or variable declaration

    // TODO  Instruction 
    val instruction: Instruction = null

    return Right(label match {
      case None    => new SimpleInstructionResult(instruction)
      case Some(l) => new LabeledInstructionResult(instruction, l)
    })

    // TODO  Variable declaration
    val definition: VariableDefinition = null
    return label match {
      case None    => Right(new VariableDefinitionResult(definition))
      case Some(l) => Left(new ParseError(line.line, VariableDefinitionWithLabel))
    }
    // TODO  EQU
  }

  // TODO SEMANTIC CHECK, MOVE ELSEWHERE?
  def semanticCheck(p: Program): Either[Array[ParseError], Program] = {
    val labelErrors = sematicCheckJumps(p)
    // jump labels exists
    // variable reference exists
    // at least one ORG
    // only one END, has to be the last instruction
    // no memory/memory operations
    // check operand sizes (ie add bx,bl is wrong, add bl,0FFFFh is also wrong )
    // same for variable declaration
    // only [BX] for indirect addressing
    // only AL for in and out
    // variable declarations and labels cannot use register labels
    return Right(p)
  }

  def sematicCheckJumps(p: Program) {
    p.instructions.filter { i => i.isInstanceOf[BinaryInstruction] }

    for (jump <- p.instructions) {

    }

  }

  def semanticCheck(instructions: Array[Instruction]): Either[Array[ParseError], Array[Instruction]] = {

    return Right(instructions)
  }

  def semanticCheck(instruction: Instruction): Either[ParseError, Instruction] = {

    return Right(instruction)
  }
}

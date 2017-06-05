package vonsim.assembly

import vonsim.assembly.lexer.Lexer

import scala.collection.mutable
import vonsim.assembly.lexer.EMPTY
import vonsim.assembly.lexer.Token
import vonsim.simulator
import vonsim.simulator._
import vonsim.assembly.parser.ZeroAry
import vonsim.utils.CollectionUtils._
import vonsim.assembly.parser.LabeledInstruction
import scala.util.parsing.input.Positional
import vonsim.assembly.lexer.VarType
import scala.collection.mutable.ListBuffer
import vonsim.assembly.parser.VarDef
import vonsim.simulator.UndefinedIndirectMemoryAddress
import PositionalExtension._
import vonsim.assembly.parser.ConstantExpression

object PositionalExtension{
  implicit class RichPositional(p:Positional){
    def location=new Location(p.pos.line, p.pos.column)
  }
}

sealed trait CompilationError {
  def location: Location
  def msg: String
}

case class LexerError(location: Location, msg: String) extends CompilationError
case class ParserError(location: Location, msg: String) extends CompilationError
abstract class SemanticError extends CompilationError

abstract class InstructionSemanticError(i:parser.Instruction) extends SemanticError{  
  def location=i.location
}
case class MemoryMemoryReferenceError(i:parser.Instruction) extends InstructionSemanticError(i){
  def msg="Both operands access memory. Cannot access two memory locations in the same instruction."
}

case class IndirectPointerTypeUndefined(i:parser.Instruction) extends InstructionSemanticError(i){
  def msg="Indirect addressing with an immediate operand requires specifying the type of pointer with WORD PTR or BYTE PTR before [BX]."
}
case class OperandSizeMismatchError(i:parser.Instruction) extends InstructionSemanticError(i){
  def msg="The second operand needs 16 bits to be encoded, but the first one only has 8 bits."
}

case class GenericSemanticError(p:Positional,msg:String) extends SemanticError{
  def location=p.location
}

case class GlobalError(location: Option[Location], msg: String)

object Location {
  def apply(line: Int) = new Location(line, 0)
}
case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}

object Compiler {

  class SuccessfulCompilation(val instructions: List[InstructionInfo], val addressToInstruction: Map[MemoryAddress, InstructionInfo], val memory: Map[MemoryAddress, Int], val warnings: List[Warning]) {
    override def toString() = {
      s"SuccessfulCompilation(${instructions.length} instructions)"
    }
  }
  class FailedCompilation(val instructions: List[Either[CompilationError, InstructionInfo]], val globalErrors: List[GlobalError]) {
    override def toString() = {
      s"FailedCompilation:\n ${instructions.lefts().mkString("\n")}"
    }
  }
  type InstructionCompilationResult = Either[CompilationError, simulator.InstructionInfo]
  type CompilationResult = Either[FailedCompilation, SuccessfulCompilation]
  type MemoryAddress = Int
  type Warning = (Line, String)
  type Line = Int
  type ParsingResult = List[Either[CompilationError, parser.Instruction]]

  def apply(code: String): CompilationResult = {
    val instructions = code.split("\n")
    var optionTokens = instructions map { Lexer(_) }
    optionTokens.foreach(f => println(f))

    val fixedTokens = Lexer.fixLineNumbers(optionTokens)
    val fixedTokensNoEmpty = fixedTokens.filter(p => {
      !(p.isRight && p.right.get.length == 1 && p.right.get(0).equals(EMPTY()))
    })

    def parseValidTokens(t: Either[LexerError, List[Token]]): Either[CompilationError, parser.Instruction] = {
      if (t.isLeft) Left(t.left.get) else parser.Parser(t.right.get.toSeq)
    }

    val parsedInstructions = fixedTokensNoEmpty map parseValidTokens toList
    
    parsedInstructions.foreach(f => println(f))
    
    val compilation = transformToSimulatorInstructions(parsedInstructions)
    compilation

  }

  def transformToSimulatorInstructions(instructions: ParsingResult): CompilationResult = {
    if (instructions.isEmpty) {
      return Left(new FailedCompilation(List(), List(GlobalError(Option.empty, "Empty program. Missing END statement"))))
    }
    var ins = instructions
    val globalErrors = mutable.ListBuffer[GlobalError]()

    // check final end
    if (!(instructions.last.isRight && instructions.last.right.get.isInstanceOf[parser.End])) {
      globalErrors += GlobalError(Option.empty, "Missing END statment")
    }
    ins = checkRepeatedEnds(ins)
    ins = checkRepeatedLabels(ins)
    ins = checkFirstOrgBeforeInstructionsWithAddress(ins)

    val equ = ins.collect({ case Right(x: parser.EQU) => (x.label, x.value) }).toMap

    val (vardefLabelToLine, vardefLabelToType, jumpLabelToLine) = getLabelToLineMappings(ins)
//    println("Vardef label to line " + vardefLabelToLine)
//    println("Vardef label to type" + vardefLabelToType)
//    println("jump label to line" + jumpLabelToLine)

    val unlabeledInstructions = unlabelInstructions(ins)

    val warnings = ListBuffer[Warning]()

    //Transform from parser.Instruction to simulator.Instruction 
    // Note that at this point the jumps and memory addresses are actually line numbers  
    val r = unlabeledInstructions.mapRightEither(x => parserToSimulatorInstruction(x, vardefLabelToType, vardefLabelToLine, jumpLabelToLine))
    if (r.allRight && globalErrors.isEmpty) {
      //      println(s"Instructions $r")
      var instructions = r.rights
      if (instructions.filter(_.instruction == Hlt).isEmpty) {
        val hltWarning = (0, "No Hlt instructions found.")
        warnings += hltWarning
      }
      //Build a db of information after getting correctly parsed instructions
      val (vardefLineToAddress, executableLineToAddress) = getMemoryLayout(instructions)
      //    println("Memory"+memory)
//      println("Vardef address" + vardefLineToAddress)
//      println("executable" + executableLineToAddress)


      instructions = replaceLinesForAddresses(instructions, vardefLineToAddress, executableLineToAddress)
      val memory = getMemory(instructions,executableLineToAddress)
      val executableInstructions = instructions.filter(_.instruction.isInstanceOf[ExecutableInstruction])
      val addressToInstruction = executableInstructions.map(x => (executableLineToAddress(x.line), x)).toMap
      Right(new SuccessfulCompilation(instructions, addressToInstruction, memory, warnings.toList))
    } else {
      Left(new FailedCompilation(r, globalErrors.toList))
    }
  }
  def replaceLinesForAddresses(instructions: List[InstructionInfo], vardefLineToAddress: Map[Int, Int], executableLineToAddress: Map[Int, Int]) = {

    instructions.map(i => {
      val updatedInstruction = i.instruction match {
        case x: WordDef            => WordDef(x.label, vardefLineToAddress(x.address), x.values)
        case x: DWordDef           => DWordDef(x.label, vardefLineToAddress(x.address), x.values)
        case x: ALUBinary          => ALUBinary(x.op, replaceLinesForAdresses(x.binaryOperands, vardefLineToAddress))
        case x: Mov                => Mov(replaceLinesForAdresses(x.binaryOperands, vardefLineToAddress))
        case x: ALUUnary           => ALUUnary(x.op, replaceLinesForAdresses(x.unaryOperand, vardefLineToAddress))
        case Call(m)               => Call(executableLineToAddress(m))
        case Jump(m)               => Jump(executableLineToAddress(m))
        case ConditionalJump(m, c) => ConditionalJump(executableLineToAddress(m), c)
        case x                     => x
      }
      new InstructionInfo(i.line, updatedInstruction)
    })

  }
  def replaceLinesForAdresses(x: BinaryOperands, vardefLineToAddress: Map[Int, Int]) = {
    x match {
      case DWordRegisterMemory(o1, o2) => DWordRegisterMemory(o1, replaceLineForAdress(o2, vardefLineToAddress))
      case WordRegisterMemory(o1, o2)  => WordRegisterMemory(o1, replaceLineForAdress(o2, vardefLineToAddress))
      case DWordMemoryRegister(o1, o2) => DWordMemoryRegister(replaceLineForAdress(o1, vardefLineToAddress), o2)
      case WordMemoryRegister(o1, o2)  => WordMemoryRegister(replaceLineForAdress(o1, vardefLineToAddress), o2)
      case DWordMemoryValue(o1, o2)    => DWordMemoryValue(replaceLineForAdress(o1, vardefLineToAddress), o2)
      case WordMemoryValue(o1, o2)     => WordMemoryValue(replaceLineForAdress(o1, vardefLineToAddress), o2)
      case x                           => x
    }
  }
  def replaceLineForAdress(mem: DWordMemoryAddress, vardefLineToAddress: Map[Int, Int]) = {
    DWordMemoryAddress(vardefLineToAddress(mem.address))
  }
  def replaceLineForAdress(mem: WordMemoryAddress, vardefLineToAddress: Map[Int, Int]) = {
    WordMemoryAddress(vardefLineToAddress(mem.address))
  }

  def replaceLinesForAdresses(x: UnaryOperandUpdatable, vardefLineToAddress: Map[Int, Int]) = {
    x match {
      case a: DWordMemoryAddress => replaceLineForAdress(a, vardefLineToAddress)
      case a: WordMemoryAddress  => replaceLineForAdress(a, vardefLineToAddress)
      case z                     => z
    }

  }

  def checkRepeatedEnds(ins: ParsingResult) = {
    val lastLine = ins.last.fold(_.location.line, _.pos.line)
    ins.mapRightEither(_ match {
      case end: parser.End => {
        if (end.pos.line < lastLine) { // 
          semanticError(end, "There should be only one END, and it should be the last instruction.")
        } else { // leave End if it is the last instruction
          Right(end)
        }
      }
      case other => Right(other)
    })
  }
  def checkRepeatedLabels(ins: ParsingResult) = {
    val labels = ins.rights.collect { case x: parser.LabelDefinition => x.label }
    val labelCounts = mutable.Map[String, Int]()
    labels.foreach(label => labelCounts(label) = labelCounts.getOrElse(label, 0) + 1)

    ins.mapRightEither(_ match {
      case x: parser.LabelDefinition => {
        if (labelCounts(x.label) > 1) {
          semanticError(x, s"Label ${x.label} has multiple definitions")
        } else {
          Right(x)
        }
      }
      case x => Right(x)
    })

  }
  def checkFirstOrgBeforeInstructionsWithAddress(ins: ParsingResult) = {
    val firstOrg = ins.indexWhere(x => x.isRight && x.right.get.isInstanceOf[parser.Org])

    ins.zipWithIndex.map {
      case (e, i) =>
        e match {
          case Left(x) => Left(x)
          case Right(x) => {
            if ((i < firstOrg || firstOrg == -1) && (!x.isInstanceOf[parser.NonAddressableInstruction])) {
              semanticError(x, "No ORG before this instruction; cannot determine memory address.")
            } else {
              Right(x)
            }
          }
        }
    }
  }
  def getMemory(instructions: List[InstructionInfo],executableLineToAddress:Map[Int,Int]) = {
    val memory = mutable.Map[MemoryAddress, Int]()
    
    instructions.foreach(f =>
      f.instruction match{
      case v:VarDefInstruction =>
        setMemory(memory,v.address,v.values)
        
      case x:ExecutableInstruction =>{
        setMemory(memory,executableLineToAddress(f.line),Simulator.encode(x))
      }
      case other => 
      
    })
    
    memory.toMap
  }
  def setMemory(memory:mutable.Map[MemoryAddress, Int],baseAddress:Int,values:List[ComputerWord]){
    var address = baseAddress

    values.foreach(cw =>
      cw.toByteList().foreach(b => {
        memory(address) = b.toInt
        address += 1
      }))
  }
  

  def getMemoryLayout(instructions: List[InstructionInfo]) = {

    val vardefLineToAddress = mutable.Map[Line, MemoryAddress]()
    val executableLineToAddress = mutable.Map[Line, MemoryAddress]()

    val firstOrgIndex = instructions.indexWhere(_.instruction.isInstanceOf[Org])
    if (firstOrgIndex >= 0) {
      val firstOrg = instructions(firstOrgIndex).instruction.asInstanceOf[Org]
      var address = firstOrg.address
      instructions.indices.foreach(i => {
        val line = instructions(i).line
        instructions(i).instruction match {
          case x: Org => {
            address = x.address
          }
          case x: VarDefInstruction => {
            vardefLineToAddress(line) = address
            address += x.bytes
          }
          case x: ExecutableInstruction => {
            executableLineToAddress(line) = address
            address += Simulator.instructionSize(x)
          }
          case other => {}
        }
      })
    }

    (vardefLineToAddress.toMap, executableLineToAddress.toMap)
  }

  def instructionSize(x: parser.Instruction) = {
    2
  }

  def getLabelToLineMappings(instructions: ParsingResult): (Map[String, Line], Map[String, VarType], Map[String, Line]) = {
    val vardefLabelToLine = mutable.Map[String, Line]()
    val vardefLabelToType = mutable.Map[String, VarType]()
    val jumpLabelToLine = mutable.Map[String, Line]()

    val unlabeledInstructions = instructions.rights.foreach(
      _ match {
        case li: parser.LabeledInstruction => {
          jumpLabelToLine(li.label) = li.pos.line
        }
        case v: parser.VarDef => {
          vardefLabelToLine(v.label) = v.pos.line
          vardefLabelToType(v.label) = v.t
        }
        case other => {}
      })

    (vardefLabelToLine.toMap, vardefLabelToType.toMap, jumpLabelToLine.toMap)
  }
  def unlabelInstructions(instructions: ParsingResult): ParsingResult = {

    instructions.mapRight(_ match {
      case li: parser.LabeledInstruction => li.i
      case other                         => other
    })
  }

  def parserToSimulatorInstruction(i: parser.Instruction,
                                   vardefLabelToType: Map[String, lexer.VarType], vardefLabelToLine: Map[String, MemoryAddress], jumpLabelToLine: Map[String, MemoryAddress]): Either[CompilationError, simulator.InstructionInfo] = {

    val zeroary = Map(parser.Popf() -> Popf, parser.Pushf() -> Pushf, parser.Hlt() -> Hlt, parser.Nop() -> Nop, parser.IRet() -> Iret, parser.Ret() -> Ret, parser.Cli() -> Cli, parser.Sti() -> Sti, parser.End() -> End)
    i match {
      case x: ZeroAry     => successfulTransformation(x, zeroary(x))
      case x: parser.IntN => successfulTransformation(x, IntN(WordValue(x.n)))
      case x: parser.Org  => successfulTransformation(x, Org(x.dir))
      case x: parser.Jump => {
        if (jumpLabelToLine.keySet.contains(x.label)) {
          successfulTransformation(x, x match {
            case x: parser.ConditionalJump   => ConditionalJump(jumpLabelToLine(x.label), jumpConditions(x.op))
            case x: parser.Call              => Call(jumpLabelToLine(x.label))
            case x: parser.UnconditionalJump => Jump(jumpLabelToLine(x.label))
          })
        } else {
          semanticError(x, s"Label ${x.label} undefined")
        }
      }
      case x: parser.Stack => successfulTransformation(x, x.i match {
        case st: lexer.POP  => Pop(fullRegisters(x.r))
        case st: lexer.PUSH => Push(fullRegisters(x.r))
      })
      case x: parser.Mov =>
        parserToSimulatorBinaryOperands(x, x.m, x.v, vardefLabelToType, vardefLabelToLine).right.flatMap(
          op => successfulTransformation(x, Mov(op)))
      case x: parser.BinaryArithmetic =>{        
        parserToSimulatorBinaryOperands(x, x.m, x.v, vardefLabelToType, vardefLabelToLine).right.flatMap(
          operands => successfulTransformation(x, ALUBinary(binaryOperations(x.op), operands)))
          
      }
      case x: parser.UnaryArithmetic =>
        parserToSimulatorOperand(x.m, vardefLabelToType, vardefLabelToLine).right.flatMap(
          _ match {
            case operand: UnaryOperandUpdatable => successfulTransformation(x, ALUUnary(unaryOperations(x.op), operand))
            case other                          => semanticError(x, s"Operand $other is not updatable")
          })

      case x: parser.VarDef => {
        val optionValues = x.values.map(ComputerWord.minimalWordFor)
        if (optionValues.map(_.isEmpty).fold(false)(_ || _)) {
          semanticError(x, "Some values are too small or too large.")
        } else {
          val values = optionValues.filter(_.isDefined).map(_.get)
          x.t match {
            case t: lexer.DB => {
              if (!values.map(_.isInstanceOf[Word]).fold(true)(_ && _)) {
                semanticError(x, "Some values do not fit into an 8 bit representation.")
              } else {
                successfulTransformation(x, WordDef(x.label, vardefLabelToLine(x.label), values.asInstanceOf[List[Word]]))
              }
            }
            case t: lexer.DW => {
              successfulTransformation(x, DWordDef(x.label, vardefLabelToLine(x.label), values.map(_.toDWord)))
            }
          }

        }

      }
      case other => semanticError(other, "Not Supported:" + other)

    }

  }

  def successfulTransformation[T](x: parser.Instruction, y: Instruction) = {
    Right[T, InstructionInfo](new InstructionInfo(x.pos.line, y))
  }
  
  def parserToSimulatorBinaryOperands(i: parser.Instruction, x: lexer.Mutable, y: lexer.Value, labelToType: Map[String, lexer.VarType], labelToAddress: Map[String, MemoryAddress]): Either[SemanticError, BinaryOperands] = {
    parserToSimulatorOperand(x, labelToType, labelToAddress).right.flatMap(o1 =>
      parserToSimulatorOperand(y, labelToType, labelToAddress).right.flatMap(o2 =>
        
        unaryOperandsToBinaryOperands(i, o1, o2)))
  }

  def unaryOperandsToBinaryOperands(i: parser.Instruction, op1: UnaryOperand, op2: UnaryOperand): Either[SemanticError, BinaryOperands] = {
    (op1, op2) match {
      case (r: FullRegister, x: FullRegister)            => Right(DWordRegisterRegister(r, x))
      case (r: HalfRegister, x: HalfRegister)            => Right(WordRegisterRegister(r, x))
      case (r: HalfRegister, x: WordValue)               => Right(WordRegisterValue(r, x))
      case (r: FullRegister, x: WordValue)               => Right(DWordRegisterValue(r, DWordValue(x.v)))
      case (r: FullRegister, x: DWordValue)              => Right(DWordRegisterValue(r, x))
      case (r: HalfRegister, x: WordMemoryAddress)       => Right(WordRegisterMemory(r, x))
      case (r: FullRegister, x: DWordMemoryAddress)      => Right(DWordRegisterMemory(r, x))
      case (r: HalfRegister, WordIndirectMemoryAddress)  => Right(WordRegisterIndirectMemory(r, WordIndirectMemoryAddress))
      case (r: FullRegister, DWordIndirectMemoryAddress) => Right(DWordRegisterIndirectMemory(r, DWordIndirectMemoryAddress))
      case (r: HalfRegister, UndefinedIndirectMemoryAddress)  => Right(WordRegisterIndirectMemory(r, WordIndirectMemoryAddress))
      case (r: FullRegister, UndefinedIndirectMemoryAddress) => Right(DWordRegisterIndirectMemory(r, DWordIndirectMemoryAddress))
      
      case (r: DWordMemoryAddress, x: FullRegister)      => Right(DWordMemoryRegister(r, x))
      case (r: WordMemoryAddress, x: HalfRegister)       => Right(WordMemoryRegister(r, x))
      case (r: WordMemoryAddress, x: WordValue)          => Right(WordMemoryValue(r, x))
      case (r: DWordMemoryAddress, x: WordValue)         => Right(DWordMemoryValue(r, DWordValue(x.v)))
      case (r: DWordMemoryAddress, x: DWordValue)        => Right(DWordMemoryValue(r, x))
      
      
      case (DWordIndirectMemoryAddress, x: DWordValue)   => Right(DWordIndirectMemoryValue(DWordIndirectMemoryAddress, x))
      case (WordIndirectMemoryAddress, x: WordValue)     => Right(WordIndirectMemoryValue(WordIndirectMemoryAddress, x))
      case (DWordIndirectMemoryAddress, x: WordValue)    => Right(DWordIndirectMemoryValue(DWordIndirectMemoryAddress, DWordValue(x.v)))
      
      case (DWordIndirectMemoryAddress, x: FullRegister) => Right(DWordIndirectMemoryRegister(DWordIndirectMemoryAddress, x))
      case (WordIndirectMemoryAddress, x: HalfRegister)  => Right(WordIndirectMemoryRegister(WordIndirectMemoryAddress, x))
      case (UndefinedIndirectMemoryAddress, x: FullRegister) => Right(DWordIndirectMemoryRegister(DWordIndirectMemoryAddress, x))
      case (UndefinedIndirectMemoryAddress, x: HalfRegister)  => Right(WordIndirectMemoryRegister(WordIndirectMemoryAddress, x))
      
      case (UndefinedIndirectMemoryAddress, x: ImmediateOperand)    => Left(IndirectPointerTypeUndefined(i))
      case (r: MemoryOperand, x: MemoryOperand)          => Left(MemoryMemoryReferenceError(i))
      case (r: WordOperand, x: DWordOperand)             => Left(OperandSizeMismatchError(i))  
      case other                                         => semanticError(i, "Invalid operands.")
    }

  }
  
  def semanticError[T](p: Positional, message: String): Left[SemanticError, T] = {
    Left(new GenericSemanticError(p, message))
  }
  def parserToSimulatorOperand(op: lexer.Value, labelToType: Map[String, lexer.VarType], labelToAddress: Map[String, MemoryAddress]): Either[SemanticError, UnaryOperand] = {
    op match {
      case x: lexer.IDENTIFIER => {
        if (!labelToType.keySet.contains(x.str)) {
          semanticError(op, s"Undefined identifier ${x.str}")
        } else {
          val varType = labelToType(x.str)
          val varAddress = labelToAddress(x.str)
          Right(varType match {
            case lexer.DB() => WordMemoryAddress(varAddress)
            case lexer.DW() => DWordMemoryAddress(varAddress)
          })
        }
      }
      //        case x:lexer.SP => semanticError(x, s"Using SP as a register is not supported")
      case x: lexer.RegisterToken => Right(registers(x))
      case x: lexer.LITERALINTEGER => {
        ComputerWord.bytesFor(x.v) match {
          case 1 => Right(WordValue(x.v))
          case 2 => Right(DWordValue(x.v))
          case _ => semanticError(x, s"The number ${x.v} cannot be represented with 8 or 16 bits")
        }
      }
      
      case x: parser.Expression => {
        def valueToWord(v:Integer)={
          ComputerWord.bytesFor(v) match {
            case 1 => Right(WordValue(v))
            case 2 => Right(DWordValue(v))
            case _ => semanticError(x, s"The number ${v} cannot be represented with 8 or 16 bits")
          }
        }
        x match {
          case c:ConstantExpression => valueToWord(c.v)
          case other => Right(DWordValue(-1))
        }
      }
      // TODO check for EQUs when literal strings appear
      case x: lexer.LITERALSTRING => semanticError(x, s"Cannot use literal strings as inmediate operands (${x.str})")
      // TODO INFER FROM OTHER OPERANDS?
      case x: lexer.INDIRECTBX    => Right(UndefinedIndirectMemoryAddress)
      case x: lexer.WORDINDIRECTBX    => Right(WordIndirectMemoryAddress)
      case x: lexer.DWORDINDIRECTBX    => Right(DWordIndirectMemoryAddress)

    }
  }

  val jumpConditions = Map(
    lexer.JC() -> JC, lexer.JNC() -> JNC, lexer.JZ() -> JZ, lexer.JNZ() -> JNZ, lexer.JO() -> JO, lexer.JNO() -> JNO, lexer.JS() -> JS, lexer.JNS() -> JNS)
  val fullRegisters = Map(
    lexer.AX() -> AX, lexer.BX() -> BX, lexer.CX() -> CX, lexer.DX() -> DX, lexer.SP() -> SP, lexer.IP() -> IP)
  val halfRegisters = Map(
    lexer.AL() -> AL, lexer.AH() -> AH, lexer.BL() -> BL, lexer.BH() -> BH, lexer.CL() -> CL, lexer.CH() -> CH, lexer.DL() -> DL, lexer.DH() -> DH)
  val registers = fullRegisters ++ halfRegisters
  val binaryOperations = Map[lexer.BinaryArithmeticOp, ALUOpBinary](
    lexer.ADD() -> ADD, lexer.ADC() -> ADC, lexer.SUB() -> SUB, lexer.SBB() -> SBB, lexer.XOR() -> XOR, lexer.OR() -> OR, lexer.AND() -> AND, lexer.CMP() -> CMP)
  val unaryOperations = Map[lexer.UnaryArithmeticOp, ALUOpUnary](
    lexer.NOT() -> NOT, lexer.DEC() -> DEC, lexer.INC() -> INC)

}
  
  
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
import vonsim.assembly.parser.BinaryExpression
import vonsim.assembly.lexer.ExpressionOperation
import vonsim.assembly.lexer.PlusOp
import vonsim.assembly.lexer.MultOp
import vonsim.assembly.lexer.DivOp
import vonsim.assembly.lexer.MinusOp
import vonsim.assembly.parser.LabelExpression
import vonsim.assembly.parser.OffsetLabelExpression

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
    //optionTokens.foreach(f => println(f))

    val fixedTokens = Lexer.fixLineNumbers(optionTokens)
    val fixedTokensNoEmpty = fixedTokens.filter(p => {
      !(p.isRight && p.right.get.length == 1 && p.right.get(0).equals(EMPTY()))
    })

    def parseValidTokens(t: Either[LexerError, List[Token]]): Either[CompilationError, parser.Instruction] = {
      if (t.isLeft) Left(t.left.get) else parser.Parser(t.right.get.toSeq)
    }

    val parsedInstructions = fixedTokensNoEmpty map parseValidTokens toList
    
//    println("Compiler: parsed instructions")
//    parsedInstructions.foreach(f => println(f))
    
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
    ins = checkLoopsInLabels(ins)
    ins = checkFirstOrgBeforeInstructionsWithAddress(ins)

    val firstPassResolver=new FirstPassResolver(ins)
    
//    println("Vardef label to line " + vardefLabelToLine)
//    println("Vardef label to type" + vardefLabelToType)
//    println("jump label to line" + jumpLabelToLine)

    val unlabeledInstructions = unlabelInstructions(ins)

    //Transform from parser.Instruction to simulator.Instruction 
    // Note that at this point the jump and memory addresses are actually dummy values    
    val firstPassResult = unlabeledInstructions.mapRightEither(x => parserToSimulatorInstruction(x, firstPassResolver))
    
    if (firstPassResult.allRight && globalErrors.isEmpty) {
      //      println(s"Instructions $r")
      val firstPassInstructions = firstPassResult.rights
      val warnings = ListBuffer[Warning]()
      if (firstPassInstructions.filter(_.instruction == Hlt).isEmpty) {
        val hltWarning = (0, "No Hlt instructions found.")
        warnings += hltWarning
      }
      
      //Build a db of information after getting correctly parsed instructions
      val secondPassResolver=new SecondPassResolver(firstPassInstructions,firstPassResolver)
      //println("Memory"+memory)
      //println("Vardef address" + vardefLineToAddress)
      //println("executable" + executableLineToAddress)
      val secondPassResult= unlabeledInstructions.mapRightEither(x => parserToSimulatorInstruction(x,secondPassResolver))
      val secondPassInstructions=secondPassResult.rights()

      val memory = getMemory(secondPassInstructions,secondPassResolver.executableLineToAddress)
      val executableInstructions = secondPassInstructions.filter(_.instruction.isInstanceOf[ExecutableInstruction])
      val addressToInstruction = executableInstructions.map(x => (secondPassResolver.executableLineToAddress(x.line), x)).toMap
      Right(new SuccessfulCompilation(secondPassInstructions, addressToInstruction, memory, warnings.toList))
    } else {
      Left(new FailedCompilation(firstPassResult, globalErrors.toList))
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
  def checkLoopsInLabels(ins: ParsingResult) = {
    //TODO
    ins
  }
  def checkRepeatedLabels(ins: ParsingResult) = {
    val labels = ins.rights.collect { 
      case x: parser.LabelDefinition => x.label
      case x: parser.EQUDef => x.label
      }
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
      case x: parser.EQUDef => {
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
  

  


 
  def unlabelInstructions(instructions: ParsingResult): ParsingResult = {

    instructions.mapRight(_ match {
      case li: parser.LabeledInstruction => li.i
      case other                         => other
    })
  }

  def parserToSimulatorInstruction(i: parser.Instruction,resolver:Resolver): Either[CompilationError, simulator.InstructionInfo] = {

    val zeroary = Map(parser.Popf() -> Popf, parser.Pushf() -> Pushf, parser.Hlt() -> Hlt, parser.Nop() -> Nop, parser.IRet() -> Iret, parser.Ret() -> Ret, parser.Cli() -> Cli, parser.Sti() -> Sti, parser.End() -> End)
    i match {
      case x: ZeroAry     => successfulTransformation(x, zeroary(x))
      case x: parser.IntN => successfulTransformation(x, IntN(WordValue(x.n)))
      case x: parser.Org  => successfulTransformation(x, Org(x.dir))
      case x: parser.Jump => {
        if (resolver.jumpLabelDefined(x.label)) {
          successfulTransformation(x, x match {
            case x: parser.ConditionalJump   => ConditionalJump(resolver.jumpLabelAddress(x.label), jumpConditions(x.op))
            case x: parser.Call              => Call(resolver.jumpLabelAddress(x.label))
            case x: parser.UnconditionalJump => Jump(resolver.jumpLabelAddress(x.label))
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
        parserToSimulatorBinaryOperands(x, x.m, x.v, resolver).right.flatMap(
          op => successfulTransformation(x, Mov(op)))
      case x: parser.BinaryArithmetic =>{        
        parserToSimulatorBinaryOperands(x, x.m, x.v, resolver).right.flatMap(
          operands => successfulTransformation(x, ALUBinary(binaryOperations(x.op), operands)))
          
      }
      case x: parser.UnaryArithmetic =>
        parserToSimulatorOperand(x.m, resolver).right.flatMap(
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
                successfulTransformation(x, WordDef(x.label, resolver.vardefLabelAddress(x.label), values.asInstanceOf[List[Word]]))
              }
            }
            case t: lexer.DW => {
              successfulTransformation(x, DWordDef(x.label, resolver.vardefLabelAddress(x.label), values.map(_.toDWord)))
            }
          }

        }

      }
      case x: parser.EQUDef => {
        successfulTransformation(x, EQUDef(x.label,resolver.expression(x.value)))
      }
      case other => semanticError(other, "Not Supported:" + other)

    }

  }

  def successfulTransformation[T](x: parser.Instruction, y: Instruction) = {
    Right[T, InstructionInfo](new InstructionInfo(x.pos.line, y))
  }
  
  def parserToSimulatorBinaryOperands(i: parser.Instruction, x: lexer.Operand, y: lexer.Operand, resolver:Resolver): Either[SemanticError, BinaryOperands] = {
    parserToSimulatorOperand(x, resolver).right.flatMap(o1 =>
      parserToSimulatorOperand(y, resolver).right.flatMap(o2 =>
        
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
      case (r: WordOperand, x: DWordOperand)             => Left(WordDWordOperandSizeMismatchError(i))
      case (r: DWordOperand, x: WordOperand)             => Left(DWordWordOperandSizeMismatchError(i))
      case other                                         => semanticError(i, "Invalid operands.")
    }

  }
  
  def semanticError[T](p: Positional, message: String): Left[SemanticError, T] = {
    Left(new GenericSemanticError(p, message))
  }
  def parserToSimulatorOperand(op: lexer.Operand, resolver:Resolver): Either[SemanticError, UnaryOperand] = {
    op match {

      //        case x:lexer.SP => semanticError(x, s"Using SP as a register is not supported")
      case x: lexer.RegisterToken => Right(registers(x))
      
      case e: parser.Expression => {
        def valueToWord(v:Integer)={
          ComputerWord.bytesFor(v) match {
            case 1 => Right(WordValue(v))
            case 2 => Right(DWordValue(v))
            case _ => semanticError(e, s"The number ${v} cannot be represented with 8 or 16 bits")
          }
        }
        def valueToMemoryAddress(v:Integer)={
          
          resolver.memoryExpressionType(e) match{
            case None => semanticError(e,s"Cannot determine type of memory reference")
            case Some(varType) => Right(varType match {
              case lexer.DB() => WordMemoryAddress(v)
              case lexer.DW() => DWordMemoryAddress(v)
              })
          }
        }
        val expressionValue=resolver.expression(e)
        if (resolver.isMemoryExpression(e)){
          valueToMemoryAddress(expressionValue)
        }else{
          valueToWord(expressionValue)
        }
      }
      
      // TODO check for EQUs when literal strings appear
      case x: lexer.LITERALSTRING => semanticError(x, s"Cannot use literal strings as inmediate operands (${x.str})")
      // TODO remove UndefinedIndirectMemoryAddress and use hints to remove its need
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
  
  
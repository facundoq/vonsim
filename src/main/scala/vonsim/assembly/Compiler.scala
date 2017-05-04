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


sealed trait CompilationError{
  def location:Location
  def msg:String
}

case class LexerError(location: Location, msg: String) extends CompilationError
case class ParserError(location: Location, msg: String) extends CompilationError
case class SemanticError(location: Location, msg: String) extends CompilationError

case class GlobalError(location:Option[Location],msg:String) 

object Location{
  def apply(line:Int)= new Location(line,0)
}
case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}




object Compiler {
  
  class SuccessfulCompilation(val instructions:List[InstructionInfo], val addressToInstruction:Map[MemoryAddress,InstructionInfo],val memory:Map[MemoryAddress,Int],val warnings:List[Warning]){
    override def toString()={
      s"SuccessfulCompilation(${instructions.length} instructions)"
    }
  }
  class FailedCompilation(val instructions:List[Either[CompilationError,InstructionInfo]],val globalErrors:List[GlobalError]){
    override def toString()={
      s"FailedCompilation:\n ${instructions.lefts().mkString("\n")}"
    }
  }
  type InstructionCompilationResult = Either[CompilationError,simulator.InstructionInfo]
  type CompilationResult = Either[FailedCompilation, SuccessfulCompilation]
  type MemoryAddress = Int
  type Warning=(Line,String)
  type Line = Int
  type ParsingResult=List[Either[CompilationError,parser.Instruction]]
  
  def apply(code: String): CompilationResult= {
    val instructions = code.split("\n")
    var optionTokens = instructions map { Lexer(_) } 
    
    val fixedTokens = Lexer.fixLineNumbers(optionTokens)
    val fixedTokensNoEmpty = fixedTokens.filter(p => {
      !(p.isRight && p.right.get.length==1 && p.right.get(0).equals(EMPTY()))
    })

    def parseValidTokens(t:Either[LexerError,List[Token]]):Either[CompilationError,parser.Instruction]={
      if (t.isLeft) Left(t.left.get) else parser.Parser(t.right.get.toSeq)
    }
    
    val parsedInstructions = fixedTokensNoEmpty map parseValidTokens toList
    
    val compilation = transformToSimulatorInstructions(parsedInstructions)
    compilation  

  }
  
  def transformToSimulatorInstructions(instructions:ParsingResult): CompilationResult ={
    if (instructions.isEmpty){
      return Left(new FailedCompilation(List(),List( GlobalError(Option.empty,"Empty program. Missing END statement"))))  
    }
    var ins=instructions
    val globalErrors=mutable.ListBuffer[GlobalError]()
    
    // check final end
    if (!(instructions.last.isRight && instructions.last.right.get.isInstanceOf[parser.End])){
      globalErrors+=GlobalError(Option.empty,"Missing END statment")
    }
    ins=checkRepeatedEnds(ins)
    ins=checkRepeatedLabels(ins)
    ins=checkFirstOrgBeforeInstructionsWithAddress(ins)
    
    val equ=ins.collect({case Right(x:parser.EQU) => (x.label,x.value)}).toMap
    
    val (vardefLabelToLine, jumpLabelToLine)=getLabelToLineMappings(ins)
    //println("Vardef label to line "+vardefLabelToLine)
//    println("jump label to line"+jumpLabelToLine)
    
    val unlabeledInstructions = unlabelInstructions(ins)
    
    // TODO build a db of information before
    val (vardefLineToAddress,vardefLineToType,executableLineToAddress) = getMemoryLayout(unlabeledInstructions)
//    println("Memory"+memory)
    //println("Vardef address"+vardefLineToAddress)
//    println("Vardef type"+vardefLineToType)
//    println("executable"+executableLineToAddress)
    val vardefLabelToAddress=vardefLabelToLine filter{case (label,line) => vardefLineToAddress.keySet.contains(line)} map { case (x,y) => (x,vardefLineToAddress(y))}
    val jumpLabelToAddress=jumpLabelToLine filter{case (label,line) => executableLineToAddress.keySet.contains(line)} map { case (x,y) => (x,executableLineToAddress(y))}
    val vardefLabelToType=vardefLabelToLine filter{case (label,line) => vardefLineToType.keySet.contains(line)} map { case (x,y) => (x,vardefLineToType(y))}
//    println("Vardef address"+vardefLabelToAddress)
//    println("Vardef type"+vardefLabelToType)
    
    val warnings=List[Warning]()
    
    
    //TODO process variable declaration statements
    
    val r=unlabeledInstructions.mapRightEither(x => parserToSimulatorInstruction(x,vardefLabelToType,vardefLabelToAddress,jumpLabelToAddress))
    if (r.allRight){
      val instructions=r.rights
      val memory=getMemory(r.rights(),vardefLabelToAddress)
      val executableInstructions=instructions.filter(_.instruction.isInstanceOf[ExecutableInstruction])
      val addressToInstruction=executableInstructions.map(x => (executableLineToAddress(x.line),x)).toMap
      Right(new SuccessfulCompilation(instructions,addressToInstruction,memory,warnings))
    }else{
      Left(new FailedCompilation(r,globalErrors.toList))
    }
  }
  def checkRepeatedEnds(ins:ParsingResult)={
    val lastLine=ins.last.fold(_.location.line, _.pos.line)
    ins.mapRightEither(_ match {
          case end:parser.End => {
            if (end.pos.line<lastLine){ // 
              semanticError(end,"There should be only one END, and it should be the last instruction.")
            }else{ // leave End if it is the last instruction
              Right(end)              
            }
          }
          case other => Right(other) 
    })
  }
  def checkRepeatedLabels(ins:ParsingResult)={
    val labels=ins.rights.collect{ case x:parser.LabelDefinition => x.label}
    val labelCounts=mutable.Map[String,Int]()
    labels.foreach(label => labelCounts(label)=labelCounts.getOrElse(label, 0)+1 )
    
    ins.mapRightEither(_ match {
      case x:parser.LabelDefinition => {
        if(labelCounts(x.label)>1)  {
          semanticError(x,s"Label ${x.label} has multiple definitions")
        }else{
          Right(x)
        }
      }
      case x => Right(x)
    })

    
  }
  def checkFirstOrgBeforeInstructionsWithAddress(ins:ParsingResult)={
    val firstOrg =ins.indexWhere(x => x.isRight && x.right.get.isInstanceOf[parser.Org])
    ins.zipWithIndex.map { case (e,i) =>
        e match{
          case Left(x) => Left(x)
          case Right(x) => {
            if ((i<firstOrg ) && (!x.isInstanceOf[parser.NonAddressableInstruction])) {
              semanticError(x,"No ORG before this instruction; cannot determine memory address.")
            }else{
              Right(x)
            }
          }
        }
    }
  }
  def getMemory(instructions:List[InstructionInfo],labelToAddress:Map[String,Int])={
    val memory=mutable.Map[MemoryAddress,Int]()
    val vardefInstructions = instructions map {_.instruction} collect { case x:VarDefInstruction => x}
    vardefInstructions.foreach(i =>{
      var address=labelToAddress(i.label)
      
      i.values.foreach(cw =>
         cw.toByteList().foreach(b =>{ 
          memory(address)=b.toInt
          address+=1
      }))
    })
    memory.toMap
  }
  
  def getMemoryLayout(instructions:ParsingResult)={
    
    val vardefLineToAddress=mutable.Map[Line,MemoryAddress]()
    val vardefLineToType=mutable.Map[Line,lexer.VarType]()
    val executableLineToAddress=mutable.Map[Line,MemoryAddress]()
    
    val correctInstructions=instructions.rights
    val firstOrgIndex=correctInstructions.indexWhere(_.isInstanceOf[parser.Org])
    if (firstOrgIndex>=0){
      val firstOrg=correctInstructions(firstOrgIndex).asInstanceOf[parser.Org]
      var address=firstOrg.dir
      correctInstructions.indices.foreach(i =>
        correctInstructions(i) match{
        case x:parser.ExecutableInstruction =>{
          executableLineToAddress(x.pos.line)=address
          address+=Simulator.instructionSize
        }
        case x:parser.VarDef =>{
          vardefLineToAddress(x.pos.line)=address
          vardefLineToType(x.pos.line)=x.t
          val typeToBytes=Map(lexer.DB()->1,lexer.DW()->2)
          val bytesForType=typeToBytes(x.t)
          address+=bytesForType*x.values.length
        }
        case x:parser.Org =>{
          address=x.dir
          
        }
        case other => {}
       } 
      )  
    }
    
    (vardefLineToAddress.toMap,vardefLineToType.toMap,executableLineToAddress.toMap) 
  }
  
  
  def getLabelToLineMappings(instructions:ParsingResult):(Map[String,Line],Map[String,Line])={
    val vardefLabelToLine=mutable.Map[String,Line]()
    val jumpLabelToLine=mutable.Map[String,Line]()
    
    val unlabeledInstructions=instructions.rights.foreach(
        _ match {
          case li:parser.LabeledInstruction => {
            jumpLabelToLine(li.label)=li.pos.line
          }
          case v:parser.VarDef => {
            vardefLabelToLine(v.label)=v.pos.line
          }
          case other => {}
        }
        
    )
    (vardefLabelToLine.toMap,jumpLabelToLine.toMap)
  }
  def unlabelInstructions(instructions:ParsingResult):ParsingResult={

    instructions.mapRight(_ match {
      case li:parser.LabeledInstruction => li.i
      case other=>other
    })
  }
   
  
  
  def parserToSimulatorInstruction(i:parser.Instruction,
      vardefLabelToType:Map[String,lexer.VarType],vardefLabelToAddress:Map[String,MemoryAddress]
  ,jumpLabelToAddress:Map[String,MemoryAddress]):Either[CompilationError,simulator.InstructionInfo] = {
    
    val zeroary=Map(parser.Popf() -> Popf
                    ,parser.Pushf() -> Pushf
                    ,parser.Hlt() -> Hlt
                    ,parser.Nop() -> Nop
                    ,parser.IRet() -> Iret
                    ,parser.Ret() -> Ret
                    ,parser.Cli() -> Cli
                    ,parser.Sti() -> Sti
                    ,parser.End() -> End
                    )  
    i match{
      case x:ZeroAry => successfulTransformation(x,zeroary(x))
      case x:parser.IntN => successfulTransformation(x,IntN(WordValue(x.n)))
      case x:parser.Org => successfulTransformation(x,Org(x.dir))
      case x:parser.Jump => { 
        if (jumpLabelToAddress.keySet.contains(x.label)){
          successfulTransformation(x,x match{
            case x:parser.ConditionalJump => ConditionalJump(jumpLabelToAddress(x.label),jumpConditions(x.op))
            case x:parser.Call => Call(jumpLabelToAddress(x.label))
            case x:parser.UnconditionalJump => Jump(jumpLabelToAddress(x.label))
          })
        }else{
          semanticError(x,s"Label ${x.label} undefined")
        }
      }
      case x:parser.Stack => successfulTransformation(x,x.i match {
        case st:lexer.POP => Pop( fullRegisters(x.r))
        case st:lexer.PUSH => Push( fullRegisters(x.r))
        })
      case x:parser.Mov => 
        parserToSimulatorBinaryOperands(x,x.m,x.v,vardefLabelToType,vardefLabelToAddress).right.flatMap(
            op => successfulTransformation(x,Mov(op)))
      case x:parser.BinaryArithmetic => 
        parserToSimulatorBinaryOperands(x,x.m,x.v,vardefLabelToType,vardefLabelToAddress).right.flatMap(
            operands => successfulTransformation(x,ALUBinary(binaryOperations(x.op), operands)))      
      case x:parser.UnaryArithmetic => 
        parserToSimulatorOperand(x.m,vardefLabelToType,vardefLabelToAddress).right.flatMap(
            _ match{
              case operand:UnaryOperandUpdatable => successfulTransformation(x,ALUUnary(unaryOperations(x.op), operand))
              case other => semanticError(x,s"Operand $other is not updatable") 
            })
                  
      case x:parser.VarDef => {
        val optionValues=x.values.map(ComputerWord.minimalWordFor)
        if (optionValues.map(_.isEmpty).fold(false)(_ || _)){
          semanticError(x,"Some values are too small or too large.")
        }else{
          val values=optionValues.filter(_.isDefined).map(_.get) 
          x.t match{
          case t:lexer.DB =>{
            if (!values.map(_.isInstanceOf[Word]).fold(true)(_&&_)){
              semanticError(x,"Some values do not fit into an 8 bit representation.")
            }else{      
              successfulTransformation(x,WordDef(x.label,vardefLabelToAddress(x.label),values.asInstanceOf[List[Word]]))
            }
          }
          case t:lexer.DW =>{      
              successfulTransformation(x,DWordDef(x.label,vardefLabelToAddress(x.label),values.map(_.toDWord)))
          }
        }
        
        }
          
      }
      case other => semanticError(other,"Not Supported:"+other)
                        
    }
    
    
  }
  
  def successfulTransformation[T](x:parser.Instruction,y:Instruction)={
      Right[T,InstructionInfo](new InstructionInfo(x.pos.line,y))
    }
  def parserToSimulatorBinaryOperands(i:parser.Instruction,x:lexer.Mutable,y:lexer.Value,labelToType:Map[String,lexer.VarType],labelToAddress:Map[String,MemoryAddress]):Either[SemanticError,BinaryOperands]={
    parserToSimulatorOperand(x,labelToType,labelToAddress).right.flatMap(o1 =>
    parserToSimulatorOperand(y,labelToType,labelToAddress).right.flatMap(o2 =>
    unaryOperandsToBinaryOperands(i,o1,o2) ))
  }

  def unaryOperandsToBinaryOperands(i:parser.Instruction,op1:UnaryOperand,op2:UnaryOperand):Either[SemanticError,BinaryOperands]={
    (op1,op2) match {
      case (r:FullRegister,x:FullRegister) => Right(DWordRegisterRegister(r,x))
      case (r:HalfRegister,x:HalfRegister) => Right(WordRegisterRegister(r,x))
      case (r:HalfRegister,x:WordValue) => Right(WordRegisterValue(r,x))
      case (r:FullRegister,x:WordValue) => Right(DWordRegisterValue(r,DWordValue(x.v)))
      case (r:FullRegister,x:DWordValue) => Right(DWordRegisterValue(r,x))
      case (r:HalfRegister,x:WordMemoryAddress) => Right(WordRegisterMemory(r,x))
      case (r:FullRegister,x:DWordMemoryAddress) => Right(DWordRegisterMemory(r,x))
      case (r:HalfRegister,WordIndirectMemoryAddress) => Right(WordRegisterIndirectMemory(r,WordIndirectMemoryAddress))
      case (r:FullRegister,DWordIndirectMemoryAddress) => Right(DWordRegisterIndirectMemory(r,DWordIndirectMemoryAddress))
      
      case (r:DWordMemoryAddress,x:FullRegister) => Right(DWordMemoryRegister(r,x))
      case (r:WordMemoryAddress,x:HalfRegister) => Right(WordMemoryRegister(r,x))
      case (r:WordMemoryAddress,x:WordValue) => Right(WordMemoryValue(r,x))
      case (r:DWordMemoryAddress,x:WordValue) => Right(DWordMemoryValue(r,DWordValue(x.v)))
      case (r:DWordMemoryAddress,x:DWordValue) => Right(DWordMemoryValue(r,x))
      
      case (DWordIndirectMemoryAddress,x:DWordValue) => Right(DWordIndirectMemoryValue(DWordIndirectMemoryAddress,x))
      case (WordIndirectMemoryAddress,x:WordValue) => Right(WordIndirectMemoryValue(WordIndirectMemoryAddress,x))
      case (DWordIndirectMemoryAddress,x:WordValue) => Right(DWordIndirectMemoryValue(DWordIndirectMemoryAddress,DWordValue(x.v)))
      case (DWordIndirectMemoryAddress,x:FullRegister) => Right(DWordIndirectMemoryRegister(DWordIndirectMemoryAddress,x))
      case (WordIndirectMemoryAddress,x:HalfRegister) => Right(WordIndirectMemoryRegister(WordIndirectMemoryAddress,x))
      
      case (r:MemoryOperand,x:MemoryOperand) => semanticError(i,"Both operands access memory. Cannot read two memory locations in the same instruction.")
      case other => semanticError(i,"Invalid operands.")
    }
    
  }
  def semanticError[T](p:Positional,message:String):Left[SemanticError,T]={
    Left(new SemanticError(new Location(p.pos.line,p.pos.column),message))
    
  }
  def parserToSimulatorOperand(op:lexer.Value,labelToType:Map[String,lexer.VarType],labelToAddress:Map[String,MemoryAddress]):Either[SemanticError,UnaryOperand]={
      op match{
        case x:lexer.IDENTIFIER => {
          if (!labelToType.keySet.contains(x.str)){
            semanticError(op,s"Undefined identifier ${x.str}")
          }else{
            val varType=labelToType(x.str)
            val varAddress=labelToAddress(x.str)
            Right(varType match{
              case lexer.DB() => WordMemoryAddress(varAddress)
              case lexer.DW() => DWordMemoryAddress(varAddress)
            })
          }
        }
//        case x:lexer.SP => semanticError(x, s"Using SP as a register is not supported")
        case x:lexer.RegisterToken => Right(registers(x))
        case x:lexer.LITERALINTEGER => {
          ComputerWord.bytesFor(x.v) match{
            case 1 => Right(WordValue(x.v))
            case 2 => Right(DWordValue(x.v))
            case _ => semanticError(x,s"The number ${x.v} cannot be represented with 8 or 16 bits")
          }          
        }
        // TODO check for EQUs when literal strings appear
        case x:lexer.LITERALSTRING => semanticError(x, s"Cannot use literal strings as inmediate operands (${x.str})")
        case x:lexer.INDIRECTBX => Right(DWordIndirectMemoryAddress)
        
      }
  }
  
  val jumpConditions=Map(
      lexer.JC() -> JC
      ,lexer.JNC() -> JNC
      ,lexer.JZ() -> JZ
      ,lexer.JNZ() -> JNZ
      ,lexer.JO() -> JO
      ,lexer.JNO() -> JNO
      ,lexer.JS() -> JS
      ,lexer.JNS() -> JNS
      )
  val fullRegisters=Map(
       lexer.AX() -> AX
      ,lexer.BX() -> BX
      ,lexer.CX() -> CX
      ,lexer.DX() -> DX
      ,lexer.SP() -> IP
      ,lexer.IP() -> SP
      )
  val halfRegisters=Map(
         lexer.AL() -> AL
        ,lexer.AH() -> AH
        ,lexer.BL() -> BL
        ,lexer.BH() -> BH
        ,lexer.CL() -> CL
        ,lexer.CH() -> CH
        ,lexer.DL() -> DL
        ,lexer.DH() -> DH
        )
  val registers = fullRegisters++halfRegisters
  val binaryOperations= Map[lexer.BinaryArithmeticOp,ALUOpBinary](
      lexer.ADD() -> ADD
      ,lexer.ADC() -> ADC
      ,lexer.SUB() -> SUB
      ,lexer.SBB() -> SBB
      ,lexer.XOR() -> XOR
      ,lexer.OR() -> OR
      ,lexer.AND() -> AND
      ,lexer.CMP() -> CMP
      )
  val unaryOperations= Map[lexer.UnaryArithmeticOp,ALUOpUnary](
      lexer.NOT() -> NOT
      ,lexer.DEC() -> DEC
      ,lexer.INC() -> INC
      
      )
  
}
  
  
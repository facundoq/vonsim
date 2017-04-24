package vonsim.assembly

import vonsim.assembly.lexer.Lexer

import scala.collection.mutable
import vonsim.assembly.lexer.EMPTY
import vonsim.assembly.lexer.Token
import vonsim.simulator
import vonsim.simulator._
import vonsim.assembly.parser.ZeroAry
import vonsim.utils.CollectionUtils._

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
  
  class SuccessfulCompilation(val instructions:Map[MemoryAddress,InstructionInfo],val memory:Map[MemoryAddress,Int],val warnings:List[Warning]){
  
  }
  class FailedCompilation(val instructions:List[Either[CompilationError,InstructionInfo]],val globalErrors:List[GlobalError]){
  
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
    // TODO change return type to include a list of global errors /(like not having an end) and warnings
    // TODO change return type to include memory that has to be set (for variable initializations)
  }
  
  def transformToSimulatorInstructions(instructions:ParsingResult): CompilationResult ={
    if (instructions.isEmpty){
      return Left(new FailedCompilation(List(),List( GlobalError(Option.empty,"Empty program. Missing END statement"))))  
    }
    var ins=instructions
    val globalErrors=mutable.ListBuffer[GlobalError]()
    
    // check and remove final end
    if (instructions.last.isRight && instructions.last.right.get.isInstanceOf[parser.End]){
      ins=instructions.dropRight(1)
    }else{
      globalErrors+=GlobalError(Option.empty,"Missing END statment")
    }
    // check that no more ENDs are around
    ins=ins.map(_ match {
      case Right(i)=>{
        i match {
          case end:parser.End => Left(SemanticError(Location(end.pos.line),"There should be only one END, and it should be the last instruction."))
          case other => Right(other)
        }
      } 
      case Left(x) => Left(x)
    }
    )
    
    //TODO process EQU statements
    //TODO check duplicate EQU definitions
    val equ=Map[String,Int]()
    //The first ORG should come before the first instruction with address
    ins=detectRepeatedLabels(ins)
    ins=invalidateInstructionsWithoutAddress(ins)
    
    val (unlabeledInstructions, labelToLine) = unlabelInstructions(ins)
    
    // TODO build a db of information before
    val (memory,lineToAddress) = getMemoryLayout(unlabeledInstructions,labelToLine)
    
    val labelToAddress=Map[String,MemoryAddress]("hola" -> 123,"HOLA"->2345)
    val warnings=List[Warning]()
    
    
    // TODO check duplicate definitions
    
    //TODO process variable declaration statements

    val r=unlabeledInstructions.map(x =>
      if (x.isLeft) Left(x.left.get) else parserToSimulator(x.right.get,labelToAddress)
    )
    if (r.map(_.isRight).forall(identity)){
      val addressToInstruction=r.rights.map(i => (lineToAddress(i.line),i) ).toMap
      Right(new SuccessfulCompilation(addressToInstruction,memory,warnings))
    }else{
      Left(new FailedCompilation(r,globalErrors.toList))
    }
  }
  def detectRepeatedLabels(ins:ParsingResult)={
//    val jumpLabels=ins.rights.collect{ case x:parser.LabeledInstruction => (x.label,x.pos.line) }
//    val memoryLabels=ins.rights.collect{ case x:parser.VarDef => (x.label,x.pos.line) }
//    val equLabels=ins.rights.collect{ case x:parser.EQU => (x.label,x.pos.line) }
//    val labels=jumpLabels++memoryLabels++equLabels
    val labels=ins.rights.collect{ case x:parser.LabelDefinition => x.label}
    val labelCounts=mutable.Map[String,Int]()
    labels.foreach(label => labelCounts(label)=labelCounts.getOrElse(label, 0)+1 )
    ins.map(_ match {
      case Left(x) => Left(x)
      case Right(x:parser.LabelDefinition) => {
        if(labelCounts(x.label)>1)  {
          Left(new SemanticError(Location(x.pos.line),s"Label ${x.label} has multiple definitions"))
        }else{
          Right(x)
        }
      }
      case Right(x) => Right(x)
    })

    
  }
  def invalidateInstructionsWithoutAddress(ins:ParsingResult)={
    val firstOrg =ins.indexWhere(x => x.isRight && x.right.get.isInstanceOf[parser.Org])
    ins.zipWithIndex.map { case (e,i) =>
        e match{
          case Left(x) => Left(x)
          case Right(x) => {
            if ((i<firstOrg ) && (!x.isInstanceOf[parser.NonAddressableInstruction])) {
              Left(new SemanticError(Location(x.pos.line),"No ORG before this instruction; cannot determine memory address."))
            }else{
              Right(x)
            }
          }
        }
    }
  }
  
  def getMemoryLayout(unlabeledInstructions:ParsingResult,labelToLine:Map[String,Int])={
    val memory=Map[MemoryAddress,Int]()
    val lineToAddress=Map[Line,MemoryAddress]()
    
    (memory,lineToAddress) 
  }
  def unlabelInstructions(instructions:ParsingResult):(ParsingResult,Map[String,Line])={
    val labelToLine=mutable.Map[String,Line]()
    
    val unlabeledInstructions=instructions.map( i =>
      if (i.isLeft){
        i
      }else{
        i.right.get match {
          case li:parser.LabeledInstruction => {
            labelToLine(li.label)=li.pos.line
            Right(li.i)
          }
          case other => Right(other) 
        }
        
      }
    )
    
    (unlabeledInstructions,labelToLine.toMap)
  }
   
  
  
  def parserToSimulator(i:parser.Instruction,labelToAddress:Map[String,MemoryAddress]):Either[CompilationError,simulator.InstructionInfo] = {
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
      case x:ZeroAry => Right(new InstructionInfo(x.pos.line,zeroary(x)))
      case x:parser.IntN => Right( new InstructionInfo(x.pos.line,IntN(WordValue(x.n))))
      case x:parser.Org => Right( new InstructionInfo(x.pos.line,Org(x.dir)))
      case x:parser.Jump => { 
        if (labelToAddress.keySet.contains(x.label)){
          Right( new InstructionInfo(x.pos.line,x match{
            case x:parser.ConditionalJump => ConditionalJump(labelToAddress(x.label),jumpConditions(x.op))
            case x:parser.Call => Call(labelToAddress(x.label))
            case x:parser.UnconditionalJump => Jump(labelToAddress(x.label))
          }))
        }else{
          Left(new SemanticError(new Location(x.pos.line,x.pos.column),s"Label ${x.label} undefined"))
        }
      }
      case x:parser.Stack => Right( new InstructionInfo(x.pos.line,x.i match {
        case st:lexer.POP => Pop( fullRegisters(x.r))
        case st:lexer.PUSH => Push( fullRegisters(x.r))
        }))
        
      case other => Left(new SemanticError(new Location(other.pos.line,other.pos.column),"Not Supported:"+other))
                        
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
  
}
  
  
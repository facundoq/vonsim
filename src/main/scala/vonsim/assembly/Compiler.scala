package vonsim.assembly

import vonsim.assembly.lexer.Lexer


import vonsim.assembly.lexer.EMPTY
import vonsim.assembly.lexer.Token
import vonsim.simulator
import vonsim.simulator._
import vonsim.assembly.parser.ZeroAry

sealed trait CompilationError{
  def location:Location
}

case class LexerError(location: Location, msg: String) extends CompilationError
case class ParserError(location: Location, msg: String) extends CompilationError
case class SemanticError(location: Location, msg: String) extends CompilationError

object Location{
  def apply(line:Int)= new Location(line,0)
}
case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}

object Compiler {
  type CompilationResult = Either[CompilationError,simulator.Instruction]
  
  def apply(code: String): List[CompilationResult ] = {
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
    
    val simulatorInstructions = transformToSimulatorInstructions(parsedInstructions)
    
    simulatorInstructions 
    // TODO change return type to include a list of global errors /(like not having an end) and warnings
    // TODO change return type to include memory that has to be set (for variable initializations)
  }
  def transformToSimulatorInstructions(instructions:List[Either[CompilationError,parser.Instruction]]): List[CompilationResult]={
    // TODO check and remove final end
    
    // TODO build a db of information before 
    
    // TODO process EQU statements
    
    //TODO process variable declaration statements
    
    val r=instructions.map(x =>
      if (x.isLeft) Left(x.left.get) else parserToSimulator(x.right.get)
    )
    r
  }
  
  
  def parserToSimulator(i:parser.Instruction):Either[CompilationError,simulator.Instruction] = {
    val zeroary=Map(parser.Popf() -> Popf
                    ,parser.Pushf() -> Pushf
                    ,parser.Hlt() -> Hlt
                    ,parser.Nop() -> Nop
                    ,parser.IRet() -> Iret
                    ,parser.Ret() -> Ret
                    ,parser.Cli() -> Sti
                    ,parser.Sti() -> Cli
                    )  
    i match{
      case x:ZeroAry => if (zeroary.keySet.contains(x)) 
                          Right(zeroary(x)) 
                        else 
                          Left(new SemanticError(Location(x.pos.line),"Invalid instruction")) 
    }
    
    
  }
  
  
}
  
  
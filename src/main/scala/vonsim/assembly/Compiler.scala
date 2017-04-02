package vonsim.assembly

import vonsim.assembly.lexer.Lexer
import vonsim.assembly.parser.Parser
import vonsim.assembly.parser.Instruction
import vonsim.assembly.lexer.EMPTY
import vonsim.assembly.lexer.Token

sealed trait CompilationError{
  def location:Location
}

case class LexerError(location: Location, msg: String) extends CompilationError
case class ParserError(location: Location, msg: String) extends CompilationError

case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}

object Compiler {

  def apply(code: String): List[Either[CompilationError,Instruction]] = {
    val instructions = code.split("\n")
    var optionTokens = instructions map { Lexer(_) } 
    val fixedTokens = Lexer.fixLineNumbers(optionTokens)
    val fixedTokensNoEmpty = fixedTokens.filter(p => {
      !(p.isRight && p.right.get.length==1 && p.right.get(0).equals(EMPTY()))
    })

    def parseValidTokens(t:Either[LexerError,List[Token]]):Either[CompilationError,Instruction]={
      if (t.isLeft) Left(t.left.get) else Parser(t.right.get.toSeq)
    }
    
    fixedTokensNoEmpty map parseValidTokens toList
    
    
  }
}
  
  
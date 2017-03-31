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
//  def apply(code: String): Either[List[CompilationError], List[Instruction]] = {
//    val instructions = code.split("\n")
//    var optionTokens = instructions map { Lexer(_) }
//    val fixedTokens = Lexer.fixLineNumbers(optionTokens)
//
//    if (fixedTokens.count(_.isRight) == fixedTokens.length) {
//      val tokens = (fixedTokens map { _.right.get }) filter { t => !(t.length == 1 && t(0) == EMPTY()) }
//
//      val asts = tokens map { Parser(_) }
//      if (asts.count(_.isRight) == asts.length) {
//        Right(asts.map(v => v.right.get).toList)
//      }else{
//        Left(asts.filter(_.isLeft).map(v => v.left.get).toList)  
//      }
//    } else {
//      Left(fixedTokens.filter(_.isLeft).map(v => v.left.get).toList)
//    }
//  }
  def apply(code: String): List[Either[CompilationError,Instruction]] = {
    val instructions = code.split("\n")
    var optionTokens = instructions map { Lexer(_) }
    val fixedTokens = Lexer.fixLineNumbers(optionTokens)
    def parseValidTokens(t:Either[LexerError,List[Token]]):Either[CompilationError,Instruction]={
      if (t.isLeft) Left(t.left.get) else Parser(t.right.get.toSeq)
    }
    
    fixedTokens map parseValidTokens toList
  }
}
  
  
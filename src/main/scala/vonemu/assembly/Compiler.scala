package vonemu.assembly

import vonemu.assembly.lexer.Lexer
import vonemu.assembly.parser.Parser
import vonemu.assembly.parser.Instruction
import vonemu.assembly.lexer.EMPTY

sealed trait CompilationError

case class LexerError(location: Location, msg: String) extends CompilationError
case class ParserError(location: Location, msg: String) extends CompilationError

case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}

object Compiler {
  def apply(code: String): Either[List[CompilationError], List[Instruction]] = {
    val instructions = code.split("\n")
    var optionTokens = instructions map { Lexer(_) }
    val fixedTokens = Lexer.fixLineNumbers(optionTokens)

    if (fixedTokens.count(_.isRight) == fixedTokens.length) {
      val tokens = (fixedTokens map { _.right.get }) filter { t => !(t.length == 1 && t(0) == EMPTY()) }

      val asts = tokens map { Parser(_) }
      if (asts.count(_.isRight) == asts.length) {
        Right(asts.map(v => v.right.get).toList)
      }else{
        Left(asts.filter(_.isLeft).map(v => v.left.get).toList)  
      }
    } else {
      Left(fixedTokens.filter(_.isLeft).map(v => v.left.get).toList)
    }
  }
}
  
  
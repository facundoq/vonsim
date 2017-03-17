package vonemu.assembly

import vonemu.assembly.lexer.Lexer
import vonemu.assembly.parser.Parser
import vonemu.assembly.parser.Instruction


sealed trait CompilationError

case class LexerError(location: Location, msg: String) extends CompilationError
case class ParserError(location: Location, msg: String) extends CompilationError

case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}


object WorkflowCompiler {
  def apply(code: String): Either[CompilationError, Instruction] = {
    for {
      tokens <- Lexer(code).right
      
      ast <- Parser(tokens).right
    } yield ast
    
  }
}
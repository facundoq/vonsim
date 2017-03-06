package vonemu.parsercombinators.compiler

import vonemu.parsercombinators.lexer.WorkflowLexer
import vonemu.parsercombinators.parser.{WorkflowParser, WorkflowAST}

object WorkflowCompiler {
  def apply(code: String): Either[WorkflowCompilationError, WorkflowAST] = {
    for {
      tokens <- WorkflowLexer(code).right
      ast <- WorkflowParser(tokens).right
    } yield ast
  }
}

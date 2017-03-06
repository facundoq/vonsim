package vonemu.parsercombinators.compiler

sealed trait WorkflowCompilationError

case class WorkflowLexerError(location: Location, msg: String) extends WorkflowCompilationError
case class WorkflowParserError(location: Location, msg: String) extends WorkflowCompilationError

case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}

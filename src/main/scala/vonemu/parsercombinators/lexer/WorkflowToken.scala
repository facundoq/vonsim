package vonemu.parsercombinators.lexer

import scala.util.parsing.input.Positional

sealed trait WorkflowToken extends Positional

case class IDENTIFIER(str: String) extends WorkflowToken
case class LITERAL(str: String) extends WorkflowToken
case class INDENTATION(spaces: Int) extends WorkflowToken
case class EXIT() extends WorkflowToken
case class READINPUT() extends WorkflowToken
case class CALLSERVICE() extends WorkflowToken
case class SWITCH() extends WorkflowToken
case class OTHERWISE() extends WorkflowToken
case class COLON() extends WorkflowToken
case class ARROW() extends WorkflowToken
case class EQUALS() extends WorkflowToken
case class COMMA() extends WorkflowToken
case class INDENT() extends WorkflowToken
case class DEDENT() extends WorkflowToken

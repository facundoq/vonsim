package vonemu.assembly

import scala.util.parsing.input.Positional

sealed trait Token extends Positional



case class IDENTIFIER(str: String) extends Token
case class LITERALSTRING(str: String) extends Token
case class LITERALINTEGER(v: Int) extends Token
case class COMMA() extends Token
case class NEWLINE() extends Token

trait InstructionToken extends Token
case class RET() extends InstructionToken
case class MOV() extends InstructionToken
case class ADD() extends InstructionToken
case class NOP() extends InstructionToken
case class END() extends InstructionToken
case class ORG() extends InstructionToken

trait RegisterToken extends Token
case class AX() extends RegisterToken
case class BX() extends RegisterToken
case class CX() extends RegisterToken
case class DX() extends RegisterToken
case class AL() extends RegisterToken
case class BL() extends RegisterToken
case class CL() extends RegisterToken
case class DL() extends RegisterToken
case class AH() extends RegisterToken
case class BH() extends RegisterToken
case class CH() extends RegisterToken
case class DH() extends RegisterToken




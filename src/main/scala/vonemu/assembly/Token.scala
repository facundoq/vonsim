package vonemu.assembly

import scala.util.parsing.input.Positional

sealed trait Token extends Positional



case class IDENTIFIER(str: String) extends Token
case class LITERALSTRING(str: String) extends Token
case class LITERALINTEGER(v: Int) extends Token
case class COMMA() extends Token
case class NEWLINE() extends Token
case class EMPTY() extends Token

trait InstructionToken extends Token
case class RET() extends InstructionToken
case class MOV() extends InstructionToken

case class NOP() extends InstructionToken
case class END() extends InstructionToken
case class ORG() extends InstructionToken

case class ADD() extends InstructionToken
case class ADC() extends InstructionToken
case class SUB() extends InstructionToken
case class SBB() extends InstructionToken
case class NOR() extends InstructionToken
case class OR() extends InstructionToken
case class XOR() extends InstructionToken
case class CMP() extends InstructionToken
case class DEC() extends InstructionToken
case class INC() extends InstructionToken

trait JumpInstructionToken extends InstructionToken
case class JMP() extends JumpInstructionToken
case class JC() extends JumpInstructionToken
case class JNC() extends JumpInstructionToken
case class JS() extends JumpInstructionToken
case class JNS() extends JumpInstructionToken
case class JO() extends JumpInstructionToken
case class JNO() extends JumpInstructionToken
case class JZ() extends JumpInstructionToken
case class JNZ() extends JumpInstructionToken
case class CALL() extends JumpInstructionToken

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




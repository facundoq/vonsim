package vonemu.assembly

import scala.util.parsing.input.Positional


//.runtime.{universe => ru}
/*
 * Missing:
 * 
 * IO
 * in
 * out
 * 
 * Interrupt
 * cli
 * sti
 * iret
 * int N
 * 
 * Stack
 * pop
 * push
 * popf
 * pushf
 * 
 * SP 
 */

object Token{
  
  def special = List(COMMA(),NEWLINE(),EMPTY())
  def keyword= List( END(),NOP(),RET(),HLT())
  def ops = List(ORG(),MOV(),CMP())++binaryArithmetic++unaryArithmetic
  def binaryArithmetic = List(ADD(),ADC(),SUB(),SBB(),OR(),XOR(),AND())
  def unaryArithmetic = List(INC(),DEC(),NOT())
  def registers:List[RegisterToken] = lRegisters++hRegisters++xRegisters
  def lRegisters =List(AL(),BL(),CL(),DL())
  def hRegisters =List(AH(),BH(),CH(),DH())
  def xRegisters =List(AX(),BX(),CX(),DX())
  def inputOutput= List(IN(),OUT())
  def jump= List(JMP(),CALL())++conditionalJump
  def conditionalJump=List(JC(),JNC(),JZ(),JNZ(),JO(),JNO(),JS(),JNS())
  
//  def brackets = List(OPENBRACKET(),CLOSEBRACKET())
 
}



sealed trait Token extends Positional with Product with Serializable

case class UNKNOWN() extends Token

sealed trait Value extends Token
sealed trait Mutable extends Value

sealed trait Literal extends Token
case class LABEL(str: String) extends Literal 
case class IDENTIFIER(str: String) extends Literal with Mutable
case class INDIRECTBX() extends Literal with Mutable
case class LITERALSTRING(str: String) extends Literal with Value
case class LITERALINTEGER(v: Int) extends Literal with Value

sealed trait IO extends Token
case class IN() extends IO
case class OUT() extends IO

sealed trait Special extends Token
case class COMMA() extends Special 
case class NEWLINE() extends Special 
case class EMPTY() extends Special
//case class OPENBRACKET() extends Special
//case class CLOSEBRACKET() extends Special

trait InstructionToken extends Token
case class RET() extends InstructionToken
case class MOV() extends InstructionToken

case class NOP() extends InstructionToken
case class END() extends InstructionToken
case class ORG() extends InstructionToken
case class HLT() extends InstructionToken

trait ArithmeticOp extends Token

trait BinaryArithmeticOp extends ArithmeticOp
case class ADD() extends BinaryArithmeticOp
case class ADC() extends BinaryArithmeticOp
case class SUB() extends BinaryArithmeticOp
case class SBB() extends BinaryArithmeticOp
case class NOR() extends BinaryArithmeticOp
case class AND() extends BinaryArithmeticOp
case class OR() extends BinaryArithmeticOp
case class XOR() extends BinaryArithmeticOp
case class CMP() extends BinaryArithmeticOp
trait UnaryArithmeticOp extends ArithmeticOp
case class DEC() extends UnaryArithmeticOp
case class INC() extends UnaryArithmeticOp
case class NOT() extends UnaryArithmeticOp

trait JumpInstructionToken extends InstructionToken
case class JMP() extends JumpInstructionToken
case class CALL() extends JumpInstructionToken

trait ConditionalJumpToken extends JumpInstructionToken 
case class JC() extends ConditionalJumpToken
case class JNC() extends ConditionalJumpToken
case class JS() extends ConditionalJumpToken
case class JNS() extends ConditionalJumpToken
case class JO() extends ConditionalJumpToken
case class JNO() extends ConditionalJumpToken
case class JZ() extends ConditionalJumpToken
case class JNZ() extends ConditionalJumpToken


trait RegisterToken extends Token with Mutable with Value
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




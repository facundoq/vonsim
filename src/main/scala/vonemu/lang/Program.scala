package vonemu.lang

//import enumeratum._

/* TODO
add EQU
 Implement operand parsing
Implement unary operator parsing
implement variable definitions
 Separate lang definition, parsing, semantic checks
 Implement semantic checks
 Test parsing
 Implement full language parsing
 test full language parsing
 implement language simulation
 implement UI
*/
class Program(val definitions:Array[VariableDefinition],val instructions:Array[Instruction],val labels:Array[JumpLabel])
object VariableType extends Enumeration {
  type VariableType = Value
  val DB, DW= Value
}
import VariableType._
class VariableDefinition(val line:Int, val id:String,val t:VariableType,values:Array[Int])
class JumpLabel(val line:Int,val id:String)

object RegisterID extends Enumeration {
  type RegisterID = Value
  val a, b, c, d = Value
}

object RegisterPart extends Enumeration {
  type RegisterPart = Value
  val L, H, X = Value // low high all
}
import RegisterID._
import RegisterPart._

sealed abstract class Operand
case class Inmediate(val value:Int) extends Operand
case class IndirectMemory(val id: Register) extends Operand
case class Register(val id: RegisterID,val  part: RegisterPart) extends Operand
//case class MemoryAddress(adress: Int) extends Operand
case class Label(val id:String) extends Operand
case class Offset(val label:Label) extends Operand

trait Operator
trait ZeroaryOperator extends Operator
case object Ret extends ZeroaryOperator
case object IRet extends ZeroaryOperator
case object Cli extends ZeroaryOperator
case object Sti extends ZeroaryOperator
case object Hlt extends ZeroaryOperator
case object End extends ZeroaryOperator
case object Nop extends ZeroaryOperator
case object PushF extends ZeroaryOperator
case object PopF extends ZeroaryOperator


trait UnaryOperator extends Operator
case object Org extends UnaryOperator
case object IntN extends UnaryOperator
case object Call extends UnaryOperator

trait UnaryArithmeticOperator extends UnaryOperator
case object Not extends UnaryOperator
case object Neg extends UnaryOperator
case object Inc extends UnaryOperator
case object Dec extends UnaryOperator


trait Stack extends UnaryOperator
case object Push extends Stack 
case object Pop extends Stack 

trait Jump extends UnaryOperator
case object Jmp extends Jump
case object Jc extends Jump
case object Jnc extends Jump
case object Jz extends Jump
case object Jnz extends Jump
case object Jo extends Jump
case object Jno extends Jump
case object Js extends Jump
case object Jns extends Jump


trait BinaryOperator extends Operator
case object Add extends BinaryOperator
case object Sub extends BinaryOperator
case object Mov extends BinaryOperator
case object Cmp extends BinaryOperator
case object Adc extends BinaryOperator
case object Sbb extends BinaryOperator
case object And extends BinaryOperator
case object Or extends BinaryOperator
case object Xor extends BinaryOperator
case object In extends BinaryOperator
case object Out extends BinaryOperator

abstract class Instruction(val line: Int)
class BinaryInstruction(line: Int, val o: BinaryOperator, val a: Operand, val b: Operand) extends Instruction(line)
class UnaryInstruction(line: Int, val o: UnaryOperator, val a: Operand) extends Instruction(line)
class ZeroaryInstruction(line: Int, val o: ZeroaryOperator) extends Instruction(line)





class Line(val line: Int,val tokens: Array[String])



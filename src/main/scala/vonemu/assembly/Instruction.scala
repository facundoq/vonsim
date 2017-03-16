package vonemu.assembly

import scala.util.parsing.input.Positional

trait Instruction extends Positional with Product with Serializable

case class LabeledInstruction(label:String,i:Instruction) extends Instruction

case class Org(dir:Int) extends Instruction

trait ZeroAry extends Instruction

case class End() extends ZeroAry 
case class Nop() extends ZeroAry
case class Hlt() extends ZeroAry
case class Ret() extends ZeroAry with Jump
case class Mov(m:Mutable,v:Value) extends Instruction

trait Arithmetic extends Instruction
case class BinaryArithmetic(op:BinaryArithmeticOp,m:Mutable,v:Value) extends Arithmetic
case class UnaryArithmetic(op:UnaryArithmeticOp,m:Mutable) extends Arithmetic
case class Cmp(op:CMP,m:Value,v:Value) extends Arithmetic

trait Jump extends Instruction
case class UnconditionalJump(label:String) extends Jump
case class Call(label:String) extends Jump
case class ConditionalJump(op:ConditionalJumpToken,label:String) extends Jump







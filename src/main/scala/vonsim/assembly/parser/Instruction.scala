package vonsim.assembly.parser

import scala.util.parsing.input.Positional
import vonsim.assembly.lexer._

 
trait LabelDefinition{
  def label:String
}

abstract class Instruction extends Positional with Product with Serializable{

}


trait NonAddressableInstruction extends Instruction
trait AddressableInstruction extends Instruction
trait ExecutableInstruction extends AddressableInstruction

case class LabeledInstruction(label:String,i:Instruction) extends ExecutableInstruction with LabelDefinition

case class Org(dir:Int) extends NonAddressableInstruction
case class IntN(n:Int) extends ExecutableInstruction 

case class Stack(i:StackInstruction,r:FullRegisterToken) extends ExecutableInstruction  

trait ZeroAry

case class End() extends NonAddressableInstruction with ZeroAry   

case class Nop() extends ExecutableInstruction with ZeroAry 
case class Hlt() extends ExecutableInstruction with ZeroAry
case class Cli() extends ExecutableInstruction with ZeroAry
case class Sti() extends ExecutableInstruction with ZeroAry
case class Pushf() extends ExecutableInstruction with ZeroAry
case class Popf() extends ExecutableInstruction with ZeroAry
case class IRet() extends ExecutableInstruction with ZeroAry with IpModifyingInstruction
case class Ret() extends ExecutableInstruction with ZeroAry with IpModifyingInstruction

case class Mov(m:Operand,v:Operand) extends ExecutableInstruction

trait Arithmetic extends ExecutableInstruction
case class BinaryArithmetic(op:BinaryArithmeticOp,m:Operand,v:Operand) extends Arithmetic
case class UnaryArithmetic(op:UnaryArithmeticOp,m:Operand) extends Arithmetic
case class Cmp(m:Operand,v:Operand) extends Arithmetic

case class VarDef(label:String,t:VarType,values:List[Int]) extends Instruction with LabelDefinition
case class EQUDef(label:String,value:Expression) extends Instruction with NonAddressableInstruction with LabelDefinition



trait IpModifyingInstruction extends ExecutableInstruction

trait Jump extends IpModifyingInstruction{
  def label:String
}
case class UnconditionalJump(label:String) extends Jump 
case class Call(label:String) extends Jump 
case class ConditionalJump(op:ConditionalJumpToken,label:String) extends Jump 

case class IO(op:IOToken,r:IORegister,add:IOAddress) extends Instruction

trait Expression extends Operand{
  def memoryAddressExpression=false
  def memoryAddressReferences=0
  def valueExpression= memoryAddressExpression==false  
}

case class BinaryExpression(val op:ExpressionOperation,l:Expression,r:Expression) extends Expression{
}

case class ConstantExpression(val v:Integer) extends Expression
case class LabelExpression(val l:String) extends Expression{
  override def memoryAddressExpression=true
  override def memoryAddressReferences=1
}
case class OffsetLabelExpression(val l:String) extends Expression

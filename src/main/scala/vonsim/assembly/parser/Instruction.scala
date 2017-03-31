package vonsim.assembly.parser

import scala.util.parsing.input.Positional
import vonsim.assembly.lexer._

trait Instruction extends Positional with Product with Serializable

case class LabeledInstruction(label:String,i:Instruction) extends Instruction

case class Org(dir:Int) extends Instruction
case class IntN(n:Int) extends Instruction

case class Stack(i:StackInstruction,r:FullRegisterToken) extends Instruction 

trait ZeroAry extends Instruction

case class End() extends ZeroAry 
case class Nop() extends ZeroAry
case class Hlt() extends ZeroAry
case class Cli() extends ZeroAry
case class Sti() extends ZeroAry
case class Pushf() extends ZeroAry
case class Popf() extends ZeroAry
case class IRet() extends ZeroAry with Jump
case class Ret() extends ZeroAry with Jump

case class Mov(m:Mutable,v:Value) extends Instruction

trait Arithmetic extends Instruction
case class BinaryArithmetic(op:BinaryArithmeticOp,m:Mutable,v:Value) extends Arithmetic
case class UnaryArithmetic(op:UnaryArithmeticOp,m:Mutable) extends Arithmetic
case class Cmp(m:Value,v:Value) extends Arithmetic

case class VarDef(label:String,t:VarType,values:List[Int]) extends Instruction

trait Jump extends Instruction
case class UnconditionalJump(label:String) extends Jump
case class Call(label:String) extends Jump
case class ConditionalJump(op:ConditionalJumpToken,label:String) extends Jump

case class IO(op:IOToken,r:IORegister,add:IOAddress) extends Jump





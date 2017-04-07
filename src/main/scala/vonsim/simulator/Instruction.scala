package vonsim.simulator


abstract class Instruction
case object Hlt extends Instruction
case object Nop extends Instruction


case class Mov(binaryOperands:BinaryOperands) extends Instruction
case class ALUBinary(op:ALUOpBinary,binaryOperands:BinaryOperands) extends Instruction
case class ALUUnary(op:ALUOpUnary,unaryOperands:UnaryOperandUpdatable) extends Instruction


class StackInstruction extends Instruction
case class Push(r:FullRegister) extends StackInstruction
case class Pop(r:FullRegister) extends StackInstruction
case object Popf extends StackInstruction
case object Pushf extends StackInstruction

class InterruptInstruction extends Instruction
case object Sti extends InterruptInstruction
case object Cli extends InterruptInstruction
case object Iret extends InterruptInstruction with IpModifyingInstruction
case class  IntN(v:InmediateOperand) extends InterruptInstruction

trait IpModifyingInstruction 

abstract class JumpInstruction extends Instruction with IpModifyingInstruction{
  def m:Int
}

case object Ret extends Instruction with IpModifyingInstruction
case class Call(m:Int) extends JumpInstruction 
case class Jump(m:Int) extends JumpInstruction

class Condition

case object JC extends Condition
case object JNC extends Condition
case object JS extends Condition
case object JNS extends Condition
case object JZ extends Condition
case object JNZ extends Condition
case object JO extends Condition
case object JNO extends Condition
case class ConditionalJump(m:Int,c:Condition) extends JumpInstruction

class IOInstruction extends Instruction


case class In(r:IORegister,a:Simulator.IOMemoryAddress) extends IOInstruction
case class Out(r:IORegister,a:Simulator.IOMemoryAddress) extends IOInstruction

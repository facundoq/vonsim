package vonsim.simulator


abstract class Instruction

abstract class ExecutableInstruction extends Instruction

case object Hlt extends ExecutableInstruction 
case object Nop extends ExecutableInstruction 
case object End extends Instruction

case class Org(address:Int) extends Instruction

abstract class EquInstruction

case class EquWord(label:String,value:Word) extends EquInstruction
case class EquDWord(label:String,value:DWord) extends EquInstruction

abstract class VarDefInstruction extends Instruction{
  def label:String
  def address:Int
  def bytes:Int
  def values:List[ComputerWord]
}

case class WordDef(label:String,address:Int,val values:List[Word]) extends VarDefInstruction{
  def bytes()={
    values.length*values.last.bytes  
  }
}
case class DWordDef(label:String,address:Int,values:List[DWord]) extends VarDefInstruction{
  def bytes()={
    values.length*values.last.bytes  
  }
}

case class Mov(binaryOperands:BinaryOperands) extends ExecutableInstruction 
case class ALUBinary(op:ALUOpBinary,binaryOperands:BinaryOperands) extends ExecutableInstruction 
case class ALUUnary(op:ALUOpUnary,unaryOperands:UnaryOperandUpdatable) extends ExecutableInstruction 


class StackInstruction extends ExecutableInstruction 
case class Push(r:FullRegister) extends StackInstruction
case class Pop(r:FullRegister) extends StackInstruction
case object Popf extends StackInstruction
case object Pushf extends StackInstruction

class InterruptInstruction extends ExecutableInstruction 
case object Sti extends InterruptInstruction
case object Cli extends InterruptInstruction
case object Iret extends InterruptInstruction with IpModifyingInstruction
case class  IntN(v:InmediateOperand) extends InterruptInstruction

trait IpModifyingInstruction 

abstract class JumpInstruction extends ExecutableInstruction  with IpModifyingInstruction  { 
  def m:Int
}

case object Ret extends ExecutableInstruction  with IpModifyingInstruction
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

class IOInstruction extends ExecutableInstruction 

case class In(r:IORegister,a:Simulator.IOMemoryAddress) extends IOInstruction
case class Out(r:IORegister,a:Simulator.IOMemoryAddress) extends IOInstruction

package vonsim.simulator

import scala.collection.mutable.ListBuffer
import scala.util.Random

object Simulator{

  type IOMemoryAddress = Byte
  def maxMemorySize=0x4000 // in bytes
  def maxInstructions=1000000 // max number of instructions to execute
  def instructionSize=2 //in bytes // TODO or 1? check instruction encoding
  
  def Empty()={
		  new Simulator(new CPU(),new Memory(),Map[Int,InstructionInfo]())
  }

}

trait UnaryOperand

trait UnaryOperandUpdatable extends UnaryOperand

trait MemoryOperand extends UnaryOperandUpdatable 

case class MemoryAddress(address:Int) extends MemoryOperand
case class IndirectMemoryAddress(r:IndirectRegister) extends MemoryOperand

trait DirectOperand extends UnaryOperand
case class Value(v:Int) extends DirectOperand


trait Register extends UnaryOperandUpdatable

trait FullRegister extends Register
trait HalfRegister extends Register
trait HighRegister extends HalfRegister
trait LowRegister extends HalfRegister

trait IORegister
trait IndirectRegister 

case object AX extends FullRegister with IORegister
case object BX extends FullRegister with IndirectRegister 
case object CX extends FullRegister 
case object DX extends FullRegister 

case object AH extends HighRegister
case object BH extends HighRegister 
case object CH extends HighRegister 
case object DH extends HighRegister

case object AL extends LowRegister with IORegister
case object BL extends LowRegister 
case object CL extends LowRegister 
case object DL extends LowRegister

trait ALUOp
trait ALUOpBinary extends ALUOp
trait ALUOpUnary extends ALUOp
trait ALUOpCompare extends ALUOp

case object CMP extends ALUOpCompare
trait ArithmeticOpBinary extends ALUOpBinary
case object ADD extends ArithmeticOpBinary
case object ADC extends ArithmeticOpBinary
case object SBB extends ArithmeticOpBinary

trait ArithmeticOpUnary extends ALUOpUnary
case object INC extends ArithmeticOpUnary
case object DEC extends ArithmeticOpUnary

trait LogicalOpBinary extends ALUOpBinary
case object XOR extends LogicalOpBinary
case object OR extends LogicalOpBinary
case object AND extends LogicalOpBinary

trait LogicalOpUnary extends ALUOpUnary
case object NOT extends LogicalOpUnary
case object NEG extends LogicalOpUnary



trait BinaryOperands{
  def o1:UnaryOperand
  def o2:UnaryOperand
}
case class FullRegisterRegister(o1:FullRegister,o2:FullRegister) extends BinaryOperands
case class HalfRegisterRegister(o1:HalfRegister,o2:HalfRegister) extends BinaryOperands

case class HalfRegisterMemory(o1:HalfRegister,o2:MemoryOperand) extends BinaryOperands
case class MemoryHalfRegister(o1:MemoryOperand,o2:HalfRegister) extends BinaryOperands
case class FullRegisterMemory(o1:FullRegister,o2:MemoryOperand) extends BinaryOperands
case class MemoryFullRegister(o1:MemoryOperand,o2:FullRegister) extends BinaryOperands

case class HalfRegisterDirect(o1:HalfRegister,o2:DirectOperand) extends BinaryOperands
case class FullRegisterDirect(o1:FullRegister,o2:DirectOperand) extends BinaryOperands
case class MemoryDirect(o1:MemoryOperand,o2:DirectOperand) extends BinaryOperands

trait Instruction
case object Hlt extends Instruction
case object Nop extends Instruction


case class Mov(binaryOperands:BinaryOperands) extends Instruction
case class ALUBinary(op:ALUOpBinary,binaryOperands:BinaryOperands) extends Instruction
case class ALUCmp(op:ALUOpCompare,opbinaryOperands:BinaryOperands) extends Instruction
case class ALUUnary(op:ALUOpUnary,unaryOperands:UnaryOperandUpdatable) extends Instruction


trait StackInstruction extends Instruction
case class Push(r:FullRegister) extends StackInstruction
case class Pop(r:FullRegister) extends StackInstruction
case object Popf extends StackInstruction
case object Pushf extends StackInstruction

trait InterruptInstruction extends Instruction
case object Sti extends InterruptInstruction
case object Cli extends InterruptInstruction
case object Iret extends InterruptInstruction
case class  IntN(v:DirectOperand) extends InterruptInstruction


trait JumpInstruction extends Instruction{
  def m:MemoryAddress
}

case class Call(m:MemoryAddress) extends JumpInstruction
case class Jump(m:MemoryAddress) extends JumpInstruction

trait Condition

case object JC extends Condition
case object JNC extends Condition
case object JS extends Condition
case object JNS extends Condition
case object JZ extends Condition
case object JNZ extends Condition
case object JO extends Condition
case object JNO extends Condition
case class ConditionalJump(m:MemoryAddress,c:Condition) extends JumpInstruction

trait IOInstruction extends Instruction


case class In(r:IORegister,a:Simulator.IOMemoryAddress) extends IOInstruction
case class Out(r:IORegister,a:Simulator.IOMemoryAddress) extends IOInstruction


class ALU{
  var o1=0
  var o2=0
  var op:ALUOp=CMP
}

class CPU{
  //gp registers
  var sp=Simulator.maxMemorySize
  var ip=0x2000
  val alu=new ALU()
      
}

class Memory{
  
  val values=Array.ofDim[Byte](Simulator.maxMemorySize)
  new Random().nextBytes(values)
}

class InstructionInfo(val line:Int,val instruction:Instruction){
   
}

class Simulator(val cpu:CPU, val memory:Memory, val instructions:Map[Int,InstructionInfo]) {
   

   def currentInstruction()={
     if (instructions.keySet.contains(cpu.ip)){
       val instruction = instructions(cpu.ip)
       Right(instruction)
     }else{
       Left("Error: Attempting to interpretate a random memory cell as an instruction. Check that your program contains all the HLT instructions necessary.")
     }
   }
   
   def run()={
     stepN(Simulator.maxInstructions)
   }
   
   def stepN(n:Int){
     val instruction=step()
     var instructions=ListBuffer(instruction)
     var counter=0
     
     while (counter<n && instruction.isRight && instruction.right.get.instruction != Hlt){
       val instruction=step()
       instructions+=instruction
     }
   }
   
   def step()={
     val instruction=currentInstruction()
     if (instruction.isRight){
       execute(instruction.right.get.instruction)
       cpu.ip+=Simulator.instructionSize 
     }
     instruction
   }
   
   def execute(i:Instruction){
     
   }
   
  
}
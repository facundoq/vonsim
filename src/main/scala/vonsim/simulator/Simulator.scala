package vonsim.simulator

import scala.collection.mutable.ListBuffer
import scala.collection.mutable
import scala.util.Random
import com.sun.org.apache.bcel.internal.generic.ArithmeticInstruction
import Simulator._
import ComputerWord._
import vonsim.assembly.Compiler.SuccessfulCompilation
import vonsim.assembly.Compiler.SuccessfulCompilation

object Simulator {

  type IOMemoryAddress = Byte
  def maxMemorySize = 0x4000 // in bytes
  def maxInstructions = 1000000 // max number of instructions to execute
  //def instructionSize = 2 //in bytes // TODO or 1? check instruction encoding
  
  def instructionSize(instruction:Instruction)={
    instruction match {
      
      case i:JumpInstruction => 3
      case i:IOInstruction => 3
      case i:Zeroary => 1
      case i:Mov => 2+operandSizes(i.binaryOperands)
      case i:ALUBinary=> 2+operandSizes(i.binaryOperands)
      case i:ALUUnary=> 2+operandSize(i.unaryOperands)
      case i:IntN => 2
      case i:Push => 2
      case i:Pop => 2
      
      case _ => 0
    }
  }
  def operandSizes(o:BinaryOperands)={
    o match{
      case w:WordBinaryOperands =>  operandSize(w.o1)+operandSize(w.o2)
      case w:DWordBinaryOperands => operandSize(w.o1)+operandSize(w.o2)
    }
    
  }
  def operandSize(o:UnaryOperand)={
    o match{
      case r:Register=> 1
      case d:DirectMemoryAddressOperand => 2
      case i:IndirectMemoryAddressOperand => 1
      case w:WordValue => 1
      case dw:DWordValue => 2
    }
  }
  
  def Empty() = {
    new Simulator(new CPU(), Memory(Simulator.maxMemorySize), Map[Int, InstructionInfo]())
  }
  def apply(c:SuccessfulCompilation)={
    val cpu= new CPU()
    val memory=Memory(c.memory,Simulator.maxMemorySize)
    
    new Simulator(cpu,memory,c.addressToInstruction)
  }

}


class InstructionInfo(val line: Int, val instruction: Instruction) {
  
  override def toString()={
    s"Line $line: $instruction"
  }
  
}


class Simulator(val cpu: CPU, val memory: Memory, var instructions: Map[Int, InstructionInfo]) {
  
  def load(c:SuccessfulCompilation){
    cpu.reset()
    memory.update(c.memory)
    instructions=c.addressToInstruction
  }
  
  def currentInstruction() = {
    if (instructions.keySet.contains(cpu.ip)) {
      val instruction = instructions(cpu.ip)
      Right(instruction)
    } else {
      Left("Error: Attempting to interpretate a random memory cell as an instruction. Check that your program contains all the HLT instructions necessary.")
    }
  }

  def runInstructions() = {
    stepNInstructions(Simulator.maxInstructions)
  }

  def stepNInstructions(n: Int) ={
    val instruction = stepInstruction()
    var instructions = ListBuffer(instruction)
    var counter = 0

    while (counter < n && instruction.isRight && instruction.right.get.instruction != Hlt && !cpu.halted) {
      val instruction = stepInstruction()
      instructions += instruction
    }
    instructions
  }

  def stepInstruction() = {
    val instructionInfo = currentInstruction()
    //println("Executing instruction: "+instructionInfo)
    if (instructionInfo.isRight) {
      val instruction = instructionInfo.right.get.instruction
      cpu.ip += Simulator.instructionSize(instruction)
      execute(instruction)
      
    }else{
      cpu.halted=true
    }
    instructionInfo
  }

  def execute(i: Instruction){
    i match {
      case Nop           => {}
      case Hlt           => { cpu.halted = true }
      case Jump(address) => { cpu.ip = address }
      case ConditionalJump(address,condition) => {
        if (cpu.alu.flags.satisfy(condition)){
          cpu.ip = address 
        }
      }
      case Call(address) => {
//        println(s"Calling $address, returning to ${cpu.ip}")
        val ra = push(cpu.ip)
//        println(memory.getBytes(cpu.sp))
        cpu.ip = address
      }
      case Ret => {
        val ra = pop()
//        println(s"Retting to $ra (${ra.toInt}) from ${cpu.ip}")
        cpu.ip = ra.toInt
      }
      case Push(register) => {
        push(cpu.get(register))
      }
      case Pop(register) => {
        cpu.set(register,pop())
      }
      case Pushf => {
        push(cpu.alu.flags.toDWord)
      }
      case Popf => {
        val f=Flags(pop())
        cpu.alu.flags=f
      }
      
      case Mov(os: WordBinaryOperands) => {
        update(os.o1, get(os.o2))
      }
      case Mov(os: DWordBinaryOperands) => {
        update(os.o1, get(os.o2))
      }
      case ALUBinary(CMP, os: WordBinaryOperands) => {
        cpu.alu.applyOp(SUB, get(os.o1), get(os.o2))
      }
      case ALUBinary(CMP, os: DWordBinaryOperands) => {
        cpu.alu.applyOp(SUB, get(os.o1), get(os.o2))
      }
      case ALUBinary(op, os: WordBinaryOperands) => {
        update(os.o1, cpu.alu.applyOp(op, get(os.o1), get(os.o2)))
      }
      case ALUBinary(op, os: DWordBinaryOperands) => {
        update(os.o1, cpu.alu.applyOp(op, get(os.o1), get(os.o2)))
      }
      case ALUUnary(op, o: WordOperand) => {
        update(o, cpu.alu.applyOp(op, get(o)))
      }
      case ALUUnary(op, o: DWordOperand) => {
        val v=get(o)
        
        val a=cpu.alu.applyOp(op, v)
        update(o, a)
      }
      case Sti=>{
         error("not implemented")
      }
      case Cli=>{
         error("not implemented")
      }
      case In(reg,v) =>{
         error("not implemented")
      }
      case Out(reg,v) =>{
         error("not implemented")
      }
      case IntN(n) =>{
         error("not implemented")
      }
      case _ => {
        error("unknown instruction")
      }
    }

  }

  def push(v: Int) {
    cpu.sp -= 2
    memory.setBytes(cpu.sp, DWord(v))
  }
  def push(v: DWord) {
    cpu.sp -= 2
    memory.setBytes(cpu.sp, v)
  }
  def pop() = {
    val v = memory.getBytes(cpu.sp)
    cpu.sp += 2
    v
  }

  def get(o: DWordOperand): DWord = {
    o match {
      case DWordMemoryAddress(address) => memory.getBytes(address)
      case r: FullRegister             => cpu.get(r)
      case v: DWordValue               => DWord(v.v)
      case DWordIndirectMemoryAddress  => memory.getBytes(cpu.get(BX).toInt)
    }
  }

  def update(o: DWordOperand, v: DWord) {
    o match {
      case DWordMemoryAddress(address) => memory.setBytes(address, v)
      case r: FullRegister             => cpu.set(r, v)
      case DWordIndirectMemoryAddress  => memory.setBytes(cpu.get(BX).toInt, v)
    }
  }

  def get(o: WordOperand): Word = {
    o match {
      case WordMemoryAddress(address) => memory.getByte(address)
      case r: HalfRegister            => cpu.get(r)
      case v: WordValue              => Word(v.v)
      case WordIndirectMemoryAddress  => memory.getByte(cpu.get(BX).toInt)
    }
  }
  def update(o: WordOperand, v: Word) {
    o match {
      case WordMemoryAddress(address) => memory.setByte(address, v)
      case r: HalfRegister            => cpu.set(r, v)
      case WordIndirectMemoryAddress  => memory.setByte(cpu.get(BX).toInt, v)
    }
  }

}
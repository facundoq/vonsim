package vonsim.simulator

import scala.collection.mutable.ListBuffer
import scala.collection.mutable
import scala.util.Random
import com.sun.org.apache.bcel.internal.generic.ArithmeticInstruction
import Simulator._
import ComputerWord._

object Simulator {

  type IOMemoryAddress = Byte
  def maxMemorySize = 0x4000 // in bytes
  def maxInstructions = 1000000 // max number of instructions to execute
  def instructionSize = 2 //in bytes // TODO or 1? check instruction encoding

  def Empty() = {
    new Simulator(new CPU(), new Memory(), Map[Int, InstructionInfo]())
  }
  implicit class WordInt(i: Int) {
    def asDWord: DWord = DWord( (i % 256).toByte, ((i / 256) % 256).toByte)
    def asWord: Word = (i % 256).toByte
  }

}


class InstructionInfo(val line: Int, val instruction: Instruction) {}

class Simulator(val cpu: CPU, val memory: Memory, val instructions: Map[Int, InstructionInfo]) {

  def currentInstruction() = {
    if (instructions.keySet.contains(cpu.ip)) {
      val instruction = instructions(cpu.ip)
      Right(instruction)
    } else {
      Left("Error: Attempting to interpretate a random memory cell as an instruction. Check that your program contains all the HLT instructions necessary.")
    }
  }

  def run() = {
    stepN(Simulator.maxInstructions)
  }

  def stepN(n: Int) {
    val instruction = step()
    var instructions = ListBuffer(instruction)
    var counter = 0

    while (counter < n && instruction.isRight && instruction.right.get.instruction != Hlt) {
      val instruction = step()
      instructions += instruction
    }
  }

  def step() = {
    val instructionInfo = currentInstruction()
    if (instructionInfo.isRight) {
      val instruction = instructionInfo.right.get.instruction
      execute(instruction)
      if (!instruction.isInstanceOf[IpModifyingInstruction]) {
        cpu.ip += Simulator.instructionSize
      }
    }
    instructionInfo
  }

  def execute(i: Instruction) {
    i match {
      case Nop           => {}
      case Hlt           => { cpu.halted = true }
      case Jump(address) => { cpu.ip = address }
      case Call(address) => {
        val ra = push(cpu.ip + Simulator.instructionSize)
        cpu.ip = address
      }
      case Ret => {
        val ra = pop()
        cpu.ip = ra.toInt
      }
      case Mov(os: WordBinaryOperands) => {
        update(os.o1, get(os.o2))
      }
      case Mov(os: DWordBinaryOperands) => {
        update(os.o1, get(os.o2))
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
        update(o, cpu.alu.applyOp(op, v))
      }
      case _ => {
        error("fuck")
      }
    }

  }

  def push(v: Int) {
    cpu.sp -= 2
    memory.setBytes(cpu.sp, v.asDWord)
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
      case v: DWordValue               => v.v.asDWord
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
      case v: DWordValue              => v.v.asWord
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
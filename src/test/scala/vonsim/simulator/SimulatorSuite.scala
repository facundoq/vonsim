package vonsim.simulator

import org.scalatest.FunSuite
import Simulator._
import ComputerWord._
import com.sun.org.apache.bcel.internal.generic.ArithmeticInstruction


class SimulatorSuite extends FunSuite {
  
  def instructionsToProgram(is:List[Instruction],baseIP:Int)= 
    is.zipWithIndex.map(a => (new InstructionInfo(a._2,a._1),a._2*2+baseIP).swap).toMap  
   
  
   
  def simulator(instructions:List[Instruction])={
   val cpu=new CPU()
   cpu.ip=0x2000
   val memory=new Memory()
   new Simulator(cpu,memory,instructionsToProgram(instructions,cpu.ip))
  }
  
  test("3+2=5 register") {
    val instructions=List(
       new Mov(new DWordRegisterDirect(AX,new DWordValue(3)))
       ,new ALUBinary(ADD,new DWordRegisterDirect(AX,new DWordValue(2)))
       ,Hlt
       )
    val s=simulator(instructions)
    s.step()
    assertResult(3)(s.cpu.get(AX).toInt)
    s.step()
    assertResult(5)(s.cpu.get(AX).toInt)
    s.step()
    assert(s.cpu.halted)
  }
  test("3+2=5 memory") {
    val address=20
    val instructions=List(
       new Mov(new DWordMemoryDirect(DWordMemoryAddress(address),new DWordValue(3)))
       ,new ALUBinary(ADD,new DWordMemoryDirect(DWordMemoryAddress(address),new DWordValue(2)))
       ,Hlt
       )
    val s=simulator(instructions)
    s.step()
    assertResult(3)(s.memory.getBytes(address).toInt)
    s.step()
    assertResult(5)(s.memory.getBytes(address).toInt)
    s.step()
    assert(s.cpu.halted)
  }
  
  test("3+3=6 memory/register") {
    val address=20
    val instructions=List(
       new Mov(new DWordRegisterDirect(AX,new DWordValue(3)))
       ,new Mov(new DWordMemoryRegister(DWordMemoryAddress(address),AX))
       ,new ALUBinary(ADD,new DWordMemoryRegister(DWordMemoryAddress(address),AX))
       ,Hlt
       )
    val s=simulator(instructions)
    s.step()
    assertResult(3)(s.cpu.get(AX).toInt)
    s.step()
    assertResult(3)(s.memory.getBytes(address).toInt)
    s.step()
    assertResult(6)(s.memory.getBytes(address).toInt)
    s.step()
    assert(s.cpu.halted)
  } 
   
   
   
}

package vonsim.simulator

import org.scalatest.FunSuite
import Simulator._
import ComputerWord._
import com.sun.org.apache.bcel.internal.generic.ArithmeticInstruction


class SimulatorSuite extends FunSuite {
  
  def instructionsToProgram(is:List[Instruction],baseIP:Int)= 
    is.zipWithIndex.map(a => (new InstructionInfo(a._2,a._1),a._2*2+baseIP).swap).toMap  
   
  
   
  def simulator(instructions:List[Instruction],baseAddress:Int=0x2000)={
   val cpu=new CPU()
   cpu.ip=baseAddress
   val memory=Memory(Simulator.maxMemorySize)
   new Simulator(cpu,memory,instructionsToProgram(instructions,cpu.ip))
  }
  
  test("3+2=5 register") {
    val instructions=List(
       new Mov(new DWordRegisterValue(AX,new DWordValue(3)))
       ,new ALUBinary(ADD,new DWordRegisterValue(AX,new DWordValue(2)))
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
       new Mov(new DWordMemoryValue(DWordMemoryAddress(address),new DWordValue(3)))
       ,new ALUBinary(ADD,new DWordMemoryValue(DWordMemoryAddress(address),new DWordValue(2)))
       ,Hlt
       )
    val s=simulator(instructions)
    s.step()
    assertResult(3)(s.memory.getBytes(address).toInt)
    s.step()
    assertResult(5)(s.memory.getBytes(address).toInt)
    assertResult(new Flags(false,false,false,false))(s.cpu.alu.flags)
    s.step()
    assert(s.cpu.halted)
  }
  
  test("3+3=6 memory/register") {
    val address=20
    val instructions=List(
       new Mov(new DWordRegisterValue(AX,new DWordValue(3)))
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
    assertResult(new Flags(false,false,false,false))(s.cpu.alu.flags)
    s.step()
    assert(s.cpu.halted)
  }
  
   test("3+1=4") {
    val address=0x1000
    val instructions=List(
       new Mov(new DWordMemoryValue(DWordMemoryAddress(address),new DWordValue(3)))
       ,new ALUUnary(INC,new DWordMemoryAddress(address))
       ,Hlt
       )
    val s=simulator(instructions)
    s.step()
    assertResult(3)(s.memory.getBytes(address).toInt)
    s.step()
    assertResult(new Flags(false,false,false,false))(s.cpu.alu.flags)
    assertResult(4)(s.memory.getBytes(address).toInt)
    s.step()
    assert(s.cpu.halted)
  }
   
  test("3-1=2") {
    val instructions=List(
       new Mov(new DWordRegisterValue(AX,new DWordValue(3)))
       ,new ALUUnary(DEC,AX)
       ,Hlt
       )
    val s=simulator(instructions)
    s.step()
    assertResult(3)(s.cpu.get(AX).toInt)
    s.step()
    assertResult(new Flags(false,false,false,false))(s.cpu.alu.flags)
    assertResult(2)(s.cpu.get(AX).toInt)
    s.step()
    assert(s.cpu.halted)
  }
    
    
   test("push pop") {

    val instructions=List(
       new Mov(new DWordRegisterValue(AX,new DWordValue(3)))
       ,new Push(AX)
       ,new Mov(new DWordRegisterValue(AX,new DWordValue(5)))
       ,new Push(AX)
       ,new Pop(DX)
       ,new Pop(CX)
       ,Hlt
       )
    val s=simulator(instructions)
    s.step()
    assertResult(3)(s.cpu.get(AX).toInt)
    val sp=s.cpu.sp
    s.step()
    assertResult(sp-2)(s.cpu.sp)
    assertResult(3)(s.memory.getBytes(sp-2).toInt)
    s.step()
    assertResult(5)(s.cpu.get(AX).toInt)
    s.step()
    assertResult(sp-4)(s.cpu.sp)
    assertResult(5)(s.memory.getBytes(sp-4).toInt)
    s.step()
    assertResult(5)(s.cpu.get(DX).toInt)
    assertResult(sp-2)(s.cpu.sp)
    s.step()
    assertResult(3)(s.cpu.get(CX).toInt)
    assertResult(sp)(s.cpu.sp)
    s.step()
    assert(s.cpu.halted)
  } 
  
   test("cmp") {
     val v0=Word(10)
    //or
    val v1=Word(5)
    val v2=Word(15)
    val instructions=List(
       new Mov(new WordRegisterValue(AL,new WordValue(v0.toInt)))
       , new ALUBinary(CMP,new WordRegisterValue(AL,new WordValue(v0.toInt)))
       , new ALUBinary(CMP,new WordRegisterValue(AL,new WordValue(v1.toInt)))
       , new ALUBinary(CMP,new WordRegisterValue(AL,new WordValue(v2.toInt)))
       ,Hlt
       )
    val flagsWithSandC=new Flags(true,true,false,false)
    val flagsWithZ=new Flags()
    flagsWithZ.z=true
    val nullFlags=new Flags()
    val s=simulator(instructions)
    s.step()
    assertResult(v0)(s.cpu.get(AL))
    s.step()
    assertResult(v0)(s.cpu.get(AL))
    assertResult(flagsWithZ)(s.cpu.alu.flags)
    s.step()
    assertResult(v0)(s.cpu.get(AL))
    assertResult(nullFlags)(s.cpu.alu.flags)
    s.step()
    assertResult(v0)(s.cpu.get(AL))
    assertResult(flagsWithSandC)(s.cpu.alu.flags)
    s.step()
    assert(s.cpu.halted)
   }
   test("logical") {

    val v0=Word("10001010")
    //or
    val v1=Word("10101000")
    val r1=Word("10101010")
    //and
    val v2=Word("10100101")
    val r2=Word("10100000")
    //xor
    val v3=Word("10000101")
    val r3=Word("00100101")
    //not
    val r4=Word("11011010")
    
    val instructions=List(
       new Mov(new WordRegisterValue(AL,new WordValue(v0.toInt)))
       , new ALUBinary(OR,new WordRegisterValue(AL,new WordValue(v1.toInt)))
       , new ALUBinary(AND,new WordRegisterValue(AL,new WordValue(v2.toInt)))
       , new ALUBinary(XOR,new WordRegisterValue(AL,new WordValue(v3.toInt)))
       , new ALUUnary(NOT,AL)
       ,Hlt
       )
    val flagsWithS=new Flags()
    flagsWithS.s=true
    val nullFlags=new Flags()
    val s=simulator(instructions)
    s.step()
    assertResult(v0)(s.cpu.get(AL))
    s.step()
    assertResult(r1)(s.cpu.get(AL))
    assertResult(flagsWithS)(s.cpu.alu.flags)
    s.step()
    assertResult(r2)(s.cpu.get(AL))
    assertResult(flagsWithS)(s.cpu.alu.flags)
    s.step()
    assertResult(r3)(s.cpu.get(AL))
    assertResult(nullFlags)(s.cpu.alu.flags)
    s.step()
    assertResult(r4)(s.cpu.get(AL))
    assertResult(flagsWithS)(s.cpu.alu.flags)
    s.step()
    assert(s.cpu.halted)
  }
   
  test("nop") {
    
    val instructions=List(
        Nop
       ,Hlt
       )
    
    val s=simulator(instructions)
    val f=s.cpu.alu.flags
    val ip=s.cpu.ip
    val sp=s.cpu.sp
    s.step()
    assertResult(ip+Simulator.instructionSize)(s.cpu.ip)
    assertResult(sp)(s.cpu.sp)
    assertResult(f)(s.cpu.alu.flags)
    s.step()
    assert(s.cpu.halted)
  }
  
  test("hlt ends") {
    
    val instructions=List(
       Hlt
       ,Nop
       )
    
    val s=simulator(instructions)
    s.step()
    assert(s.cpu.halted)
  }
  test("jump to next is like nop") {
    val baseAddress=0x2000
    val jumpAddress=baseAddress+Simulator.instructionSize
    val instructions=List(
       new Jump(jumpAddress)
       ,new Mov(new WordRegisterValue(AL,new WordValue(2)))
       ,Hlt
       )
       
    val flagsWithS=new Flags()
    flagsWithS.s=true
    val nullFlags=new Flags()
    
    val s=simulator(instructions,baseAddress)
    s.step()
    assertResult(jumpAddress)(s.cpu.ip)
    s.step()
    assertResult(Word(2))(s.cpu.get(AL))
    s.step()
    assert(s.cpu.halted)
  }
  
  test("jump 2") {
    val baseAddress=8192
    val jumpAddress=baseAddress+ (Simulator.instructionSize*3)
    val instructions=List(
       new Jump(jumpAddress)
       ,new Mov(new WordRegisterValue(AL,new WordValue(1)))
       ,new Mov(new WordRegisterValue(AL,new WordValue(2)))
       ,new Mov(new WordRegisterValue(AL,new WordValue(3)))
       ,Hlt
       )
       
    val flagsWithS=new Flags()
    flagsWithS.s=true
    val nullFlags=new Flags()
    
    val s=simulator(instructions,baseAddress)
    assertResult(baseAddress)(s.cpu.ip)
    s.step()
    assertResult(jumpAddress)(s.cpu.ip)
    s.step()
    assertResult(Word(3))(s.cpu.get(AL))
    s.step()
    assert(s.cpu.halted)
  }
  
  test("jump conditional") {
    val baseAddress=8192
    val jumpAddressNZ=baseAddress+ (Simulator.instructionSize*4)
    val jumpAddressZ=baseAddress+ (Simulator.instructionSize*6)
    val jumpAddressEnd=baseAddress+ (Simulator.instructionSize*9)
    val instructions=List(
        new Mov(new WordRegisterValue(AL,new WordValue(1)))
       ,new ALUBinary(CMP,new WordRegisterValue(AL,new WordValue(1)))
       ,new ConditionalJump(jumpAddressNZ,JNZ)
       ,new ConditionalJump(jumpAddressZ,JZ)
        ,new Mov(new WordRegisterValue(AL,new WordValue(2)))
       ,new Jump(jumpAddressEnd)
        ,new Mov(new WordRegisterValue(AL,new WordValue(3)))
        ,new Jump(jumpAddressEnd)
       ,new Mov(new WordRegisterValue(AL,new WordValue(4)))
       ,Hlt
       )
       
    val flagsWithZ=new Flags()
    flagsWithZ.z=true
    
    val s=simulator(instructions,baseAddress)
    assertResult(baseAddress)(s.cpu.ip)
    s.step()
    assertResult(Word(1))(s.cpu.get(AL))
    s.step()
    assertResult(flagsWithZ)(s.cpu.alu.flags)
    val ip=s.cpu.ip
    s.step()
    assertResult(ip+Simulator.instructionSize)(s.cpu.ip)
    s.step()
    assertResult(jumpAddressZ)(s.cpu.ip)
    s.step()
    assertResult(Word(3))(s.cpu.get(AL))
    s.step()
    assertResult(jumpAddressEnd)(s.cpu.ip)
    s.step()
    assert(s.cpu.halted)
  }
  
  test("call") {
    val baseAddress=0x2000
    val jumpAddress=baseAddress+Simulator.instructionSize*6
    val returnAddress=baseAddress+Simulator.instructionSize*2
    val instructions=List(
        new Mov(new WordRegisterValue(AL,new WordValue(1)))
       ,new Call(jumpAddress)
       ,new Mov(new WordRegisterValue(AL,new WordValue(2)))
       ,Hlt
       ,Nop
       ,Nop
       ,new Mov(new WordRegisterValue(AL,new WordValue(3)))
       ,Nop
       ,Ret
       )
       
    val flagsWithS=new Flags()
    flagsWithS.s=true
    val nullFlags=new Flags()
    
    val s=simulator(instructions,baseAddress)
    val sp=s.cpu.sp
    s.step()
    assertResult(Word(1))(s.cpu.get(AL))
    s.step()
    assertResult(jumpAddress)(s.cpu.ip)
    assertResult(sp-2)(s.cpu.sp)
    s.step()
    assertResult(Word(3))(s.cpu.get(AL))
    s.step()
    s.step()
    assertResult(returnAddress)(s.cpu.ip)
    assertResult(sp)(s.cpu.sp)
    s.step()
    assertResult(Word(2))(s.cpu.get(AL))
    s.step()
    assert(s.cpu.halted)
  }
   
   
   
}


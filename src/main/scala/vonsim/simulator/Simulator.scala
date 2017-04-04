package vonsim.simulator

import scala.collection.mutable.ListBuffer
import scala.collection.mutable
import scala.util.Random
import com.sun.org.apache.bcel.internal.generic.ArithmeticInstruction
import Simulator._

object Simulator{

  type Word = Byte
  type DWord = (Byte,Byte)
  type IOMemoryAddress = Byte
  def maxMemorySize=0x4000 // in bytes
  def maxInstructions=1000000 // max number of instructions to execute
  def instructionSize=2 //in bytes // TODO or 1? check instruction encoding
  
  def Empty()={
		  new Simulator(new CPU(),new Memory(),Map[Int,InstructionInfo]())
  }
  implicit class WordInt(i: Int) {
    def asDWord:DWord = (( (i / 256) % 256).toByte, (i % 256).toByte)
    def asWord:Word=(i%256).toByte
    
  }
  
  implicit class BetterWord(w: DWord) {
    def toInt:Int= w._1+w._2*256
    
    
  }

}

class ALU{
  var o1=0
  var o2=0
  var op:ALUOp=CMP
}

class CPU{
  
  //gp registers
  var sp=Simulator.maxMemorySize
  var ip=0x2000
  var halted=false
  val alu=new ALU()
  var registers=mutable.Map[FullRegister,DWord](AX -> (0,0),BX ->(0,0),CX -> (0,0),DX -> (0,0))
  
  def reset(){
    ip=0x2000
    sp=Simulator.maxMemorySize
    halted=false
    registers=mutable.Map[FullRegister,DWord](AX -> (0,0),BX ->(0,0),CX -> (0,0),DX -> (0,0))
  }
  
  
  def get(r:FullRegister):DWord={
    registers(r)  
  }
  def set(r:FullRegister,v:DWord){
    registers(r)=v
  }
 
  def get(r:HalfRegister):Word={
    r match{
      case r:LowRegister => get(r)
      case r:HighRegister => get(r)
    }
  }
  def get(r:LowRegister):Word=get(r.full)._1
  def get(r:HighRegister):Word=get(r.full)._2
  
  def set(r:LowRegister,v:Word) { set(r.full,(v,get(r.high))) }
  def set(r:HighRegister,v:Word){ set(r.full,(get(r.low),v)) }
      
}

class Memory{  
  val values=Array.ofDim[Word](Simulator.maxMemorySize)
  new Random().nextBytes(values)
  
  def getByte(address:Int)={
    values(address)
  }
  def getBytes(address:Int):DWord={
    (values(address),values(address+1))
  }
  def setByte(address:Int,v:Word){
    values(address)=v
  }
  def setBytes(address:Int,v:DWord){
    values(address)=v._1
    values(address+1)=v._2
  }
  
  
  
}

class InstructionInfo(val line:Int,val instruction:Instruction){}

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
     val instructionInfo=currentInstruction()
     if (instructionInfo.isRight){
       val instruction= instructionInfo.right.get.instruction
       execute(instruction)
       if (!instruction.isInstanceOf[IpModifyingInstruction]){
         cpu.ip+=Simulator.instructionSize
       }
     }
     instructionInfo
   }
   
   def execute(i:Instruction){
     i match{
       case Nop => {}
       case Hlt => {cpu.halted=true}
       case Jump(m) => { cpu.ip=m.address }
       case Call(m) => { 
           val ra=push(cpu.ip+Simulator.instructionSize)
           cpu.ip=m.address 
       }
       case Ret => { 
           val ra=pop()
           cpu.ip=ra.toInt 
       }
       case Mov(os) =>{
           update(os.o1,get(os.o2))
       }
       
       case _ => {
         error("fuck")
       }
     }
     
   }
   
   def push(v:Int){
     cpu.sp-=2
     memory.setBytes(cpu.sp, v.asDWord)
   }
   def push(v:DWord){
     cpu.sp-=2
     memory.setBytes(cpu.sp, v)
   }
   def pop()={
     val v=memory.getBytes(cpu.sp)
     cpu.sp+=2
     v
   }
   
   def get(o:FullRegisterDirect)={
     (getDWord(o.o1),getDWord(o.o2))     
   }
   def update(o:FullRegisterDirect,v:DWord){
     cpu.set(o.o1,v)
   }
   
  
   def getDWord(o:FullRegister)=cpu.get(o)
   def getDWord(o:Value)=o.v.asDWord
   
   def getWord(o:HalfRegister):Word=cpu.get(o)
   def getWord(o:Value):Word=o.v.asWord
   
  
}
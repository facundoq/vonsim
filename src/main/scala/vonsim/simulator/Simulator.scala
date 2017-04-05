package vonsim.simulator

import scala.collection.mutable.ListBuffer
import scala.collection.mutable
import scala.util.Random
import com.sun.org.apache.bcel.internal.generic.ArithmeticInstruction
import Simulator._

object Simulator{

  type Word = Byte
  type DWord = (Byte,Byte) // ._1=low ._2= high
  type IOMemoryAddress = Byte
  def maxMemorySize=0x4000 // in bytes
  def maxInstructions=1000000 // max number of instructions to execute
  def instructionSize=2 //in bytes // TODO or 1? check instruction encoding
  
  def Empty()={
		  new Simulator(new CPU(),new Memory(),Map[Int,InstructionInfo]())
  }
  implicit class WordInt(i: Int) {
    def asDWord:DWord = ((i % 256).toByte, ( (i / 256) % 256).toByte)
    def asWord:Word=(i%256).toByte
    
  }
  
  implicit class BetterDWord(w: DWord) {
    def toInt:Int= w._1+w._2*256
    def sign:Boolean= (toInt / 32768) >0
    def h:Word=w._2
    def l:Word=w._1
    def +(w:DWord)={
      
      (w,new Flags())
    }
  }
  implicit class BetterWord(w: Word) {
    def sign:Boolean= w<0
    
    def add(v:Word)={
     val f=new Flags()
     
     var res=v+w
     if (res < -128){
       f.c=true
       res+=256
     }
     if (res > 127){
       f.c=true
       res-=256
     }
     f.s=res.toByte.sign  
     
     f.z= res==0
     f.o= (v.sign == w.sign) && (v.sign!=res>0)
         
     (res.toByte,f)
    }
  }

}




class Flags(var c:Boolean=false,var s:Boolean=false,var o:Boolean=false, var z:Boolean=false){

  def reset(){
     c=false
     s=false
     z=false
     o=false
  }
}

class ALU{
  var o1=0
  var o2=0
  var res=0
  var op:ALUOp=CMP
  
  var flags= new Flags()
  
  def reset(){flags.reset()}
  
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
      case r:LowRegister => get(r.full)._1
      case r:HighRegister => get(r.full)._2
    }
  }
  
  
  def set(r:HalfRegister,v:Word){
    r match{
      case r:LowRegister => set(r.full,(v,get(r.high))) 
      case r:HighRegister => set(r.full,(get(r.low),v)) 
    }
  }
      
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
       case Jump(address) => { cpu.ip=address }
       case Call(address) => { 
           val ra=push(cpu.ip+Simulator.instructionSize)
           cpu.ip=address 
       }
       case Ret => { 
           val ra=pop()
           cpu.ip=ra.toInt 
       }
       case Mov(os:WordBinaryOperands) =>{
            update(os.o1,get(os.o2))
       }
       case Mov(os:DWordBinaryOperands) =>{
            update(os.o1,get(os.o2))
       }
       case ALUBinary(op,os:WordBinaryOperands) =>{
            update(os.o1,applyOp(op,get(os.o1),get(os.o2)))
       }
       
       case _ => {
         error("fuck")
       }
     }
     
   }
   def applyOp(op:ALUOpBinary,v1:Word,v2:Word)={
     cpu.alu.o1=v1.toInt
     cpu.alu.o2=v2.toInt
     cpu.alu.op=op
     var res:Int=0
     cpu.alu.reset() 
     
     op match {
       case ADD => { 
          
                                   
       }
     }
     cpu.alu.res=res
     res.asWord
     
     
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
  
   
   def get(o:DWordOperand):DWord={
     o match{
       case DWordMemoryAddress(address) => memory.getBytes(address)
       case r:FullRegister => cpu.get(r)
       case v:DWordValue => v.v.asDWord
       case DWordIndirectMemoryAddress => memory.getBytes( cpu.get(BX).toInt)
     }
   }
   
   def update(o:DWordOperand,v:DWord){
     o match{
       case DWordMemoryAddress(address) => memory.setBytes(address,v)
       case r:FullRegister => cpu.set(r,v)
       case DWordIndirectMemoryAddress => memory.setBytes( cpu.get(BX).toInt,v)
     }
   }
     
   def get(o:WordOperand):Word={
     o match{
       case WordMemoryAddress(address) => memory.getByte(address)
       case r:HalfRegister => cpu.get(r)
       case v:DWordValue => v.v.asWord
       case WordIndirectMemoryAddress => memory.getByte( cpu.get(BX).toInt)
     }
   }
   def update(o:WordOperand,v:Word){
     o match{
       case WordMemoryAddress(address) => memory.setByte(address,v)
       case r:HalfRegister => cpu.set(r,v)
       case WordIndirectMemoryAddress => memory.setByte( cpu.get(BX).toInt,v)
     }
   }
  
  
}
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
  
  def encode(instruction:Instruction)={
    
    instruction match{
      case ei:ExecutableInstruction =>
        val encoding=ListBuffer(encodeOpcode(ei))
        ei match{
          case i:JumpInstruction => encoding++=DWord(i.m).toByteList()
          case i:Zeroary => 
          case i:IOInstruction => {
            encoding++= encodeUnaryOperand(i.r)++List(Word(i.a))
          }
          case i:Mov =>{
            encoding+= encodeBinaryAddressingMode(i.binaryOperands)
            encoding++=encodeBinaryOperands(i.binaryOperands)
          }
          case i:ALUBinary=> {
            encoding+= encodeBinaryAddressingMode(i.binaryOperands)
            encoding++=encodeBinaryOperands(i.binaryOperands)
          }
          case i:ALUUnary=>{
            encoding+= encodeUnaryAddressingMode(i.unaryOperands)
            encoding++=encodeUnaryOperand(i.unaryOperands)
          }
          case i:IntN => encoding++=encodeImmediate(i.v)
          case i:Push => encoding+=encodeRegister(i.r)
          case i:Pop => encoding+=encodeRegister(i.r)
        }
        encoding.toList
      case other => List() 
    }

  }
  def encodeBinaryAddressingMode(o:BinaryOperands)={
    
    o match {
      case x:WordRegisterRegister => Word("00000000")
      case x:DWordRegisterRegister => Word("10000000")
      case x:WordRegisterValue => Word("00000001")
      case x:DWordRegisterValue => Word("10000001")
      case x:WordRegisterMemory => Word("00000010")
      case x:DWordRegisterMemory => Word("10000010")
      case x:WordRegisterIndirectMemory=> Word("00000011")
      case x:DWordRegisterIndirectMemory => Word("10000011")
      
      case x:WordMemoryRegister => Word("00000100")
      case x:DWordMemoryRegister => Word("10000100")
      case x:WordMemoryValue => Word("00000101")
      case x:DWordMemoryValue => Word("10000101")
      
      case x:WordIndirectMemoryRegister=> Word("00000110")
      case x:DWordIndirectMemoryRegister => Word("10000110")
      case x:WordIndirectMemoryValue => Word("00000111")
      case x:DWordIndirectMemoryValue => Word("10000111")  
    }
  }
  
  def encodeBinaryOperands(o:BinaryOperands)={
    o match {
      case x:DWordBinaryOperands => encodeUnaryOperand(x.o1)++encodeUnaryOperand(x.o2)
      case x:WordBinaryOperands =>  encodeUnaryOperand(x.o1)++encodeUnaryOperand(x.o2)
    }
  }
  
  def encodeUnaryAddressingMode(i:UnaryOperandUpdatable)={
    i match{
      case x:HalfRegister => Word("00000000")
      case x:FullRegister => Word("10000000")
      case x:WordMemoryAddress => Word("00000001")
      case x:DWordMemoryAddress => Word("10000001")
      case WordIndirectMemoryAddress => Word("00000010")
      case DWordIndirectMemoryAddress => Word("10000010")
        
    }
  }
  def encodeUnaryOperand(i:UnaryOperand)={
    i match{
      case r:Register=> List(encodeRegister(r))
      case x:WordMemoryAddress => DWord(x.address).toByteList()
      case x:DWordMemoryAddress =>DWord(x.address).toByteList()
      case WordIndirectMemoryAddress =>  List()
      case DWordIndirectMemoryAddress => List()
      case x:ImmediateOperand =>  encodeImmediate(x)
    }
  }
  
  def encodeImmediate(i:ImmediateOperand)={
    i match{
      case dw:DWordValue => DWord(dw.v).toByteList()
      case dw:WordValue => List(Word(dw.v))
    }
  }
  def encodeRegister(r:Register)={
    Word(List(AX,BX,CX,DX,AL,BL,CL,DL,AH,BH,CH,DH).indexOf(r))
  }
  
  def encodeOpcode(instruction:ExecutableInstruction)={
    instruction match{
    case i:ALUBinary=>
        Word(List(ADD,ADC,SUB,SBB,OR,AND,XOR,CMP).indexOf(i.op))
    case i:Mov => Word("00001000")
    case i:In =>  Word("00001001")
    case i:Out => Word("00001010")
    case i:ALUUnary=> Word(16+List(INC,DEC,NOT,NEG).indexOf(i.op)) 
    case i:IntN => Word("00100001")
    case i:Push => Word("00100000")
    case i:Pop =>  Word("00100001")
    
    case ji:JumpInstruction =>
      ji match{
        case cj:ConditionalJump =>
          Word(32+16+List(JC,JNC,JZ,JNZ,JO,JNO,JS,JNS).indexOf(cj.c))
        case x:Call => Word("00111001")
        case x:Jump => Word("00111000")
      }
    case i:Zeroary => Word(List(Pushf,Popf,Ret,Iret,Nop,Hlt,Cli,Sti).indexOf(i)+64)
    }
    
  }
  def instructionSize(instruction:Instruction)={
      val encoding=encode(instruction)
      encoding.length
//    instruction match {
//      
//      case i:JumpInstruction => 3
//      case i:IOInstruction => 3
//      case i:Zeroary => 1
//      case i:Mov => 2+operandSizes(i.binaryOperands)
//      case i:ALUBinary=> 2+operandSizes(i.binaryOperands)
//      case i:ALUUnary=> 2+operandSize(i.unaryOperands)
//      case i:IntN => 2
//      case i:Push => 2
//      case i:Pop => 2
//      
//      case _ => 0
//    }
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
class SimulatorState

class SimulatorStoppedState extends SimulatorState
case object SimulatorNoProgramLoaded extends SimulatorStoppedState
case object SimulatorProgramLoaded extends SimulatorStoppedState
case object SimulatorProgramExecuting extends SimulatorState
case class SimulatorExecutionError(val message:String) extends SimulatorStoppedState
case object SimulatorExecutionFinished extends SimulatorStoppedState

class Simulator(val cpu: CPU, val memory: Memory, var instructions: Map[Int, InstructionInfo]) {
  var state:SimulatorState=SimulatorNoProgramLoaded
  
  def reset(){
    cpu.reset()
    //memory.reset()
    state=SimulatorNoProgramLoaded  
  }
  def stop(){
    cpu.reset()
    //memory.reset()
    state=SimulatorExecutionFinished  
  }
  
  def load(c:SuccessfulCompilation){
    cpu.reset()
    memory.update(c.memory)
    instructions=c.addressToInstruction
    state=SimulatorProgramExecuting
  }
  
  def currentInstruction() = {
    if (instructions.keySet.contains(cpu.ip)) {
      val instruction = instructions(cpu.ip)
      Right(instruction)
    } else {
      val message="Attempting to interpretate a random memory cell as an instruction. Check that your program contains all the HLT instructions necessary."
      Left(message)
    }
  }

  def runInstructions() = {
    stepNInstructions(Simulator.maxInstructions)
  }

  def stepNInstructions(n: Int) ={
    val instruction = stepInstruction()
    var instructions = ListBuffer(instruction)
    var counter = 0

    while (counter < n && instruction.isRight && !cpu.halted) {
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
      state=SimulatorProgramExecuting
      execute(instruction)
    }else{
      stopExecutionForError(instructionInfo.left.get)
    }
    instructionInfo
  }
  def finishExecution(){
    cpu.halted=true
    state=SimulatorExecutionFinished
  }
  def stopExecutionForError(message:String){
    cpu.halted=true
    state=SimulatorExecutionError(message)
  }
  
  def execute(i: Instruction){
    i match {
      case Nop           => {}
      case Hlt           => { finishExecution() }
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
         stopExecutionForError("Sti not implemented.")
      }
      case Cli=>{
         stopExecutionForError("Cli not implemented.")
      }
      case In(reg,v) =>{
         stopExecutionForError("In not implemented.")
      }
      case Out(reg,v) =>{
         stopExecutionForError("Out not implemented.")
      }
      case IntN(n) =>{
         stopExecutionForError("Int N not implemented.")
      }
      case _ => {
        stopExecutionForError("Unknown instruction")
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
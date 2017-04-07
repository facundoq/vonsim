package vonsim.simulator

import scala.collection.mutable.ListBuffer
import scala.collection.mutable
import scala.util.Random
import com.sun.org.apache.bcel.internal.generic.ArithmeticInstruction
import Simulator._
import ComputerWord._

class Flags(var c: Boolean = false, var s: Boolean = false, var o: Boolean = false, var z: Boolean = false) {

  def reset() {
    c = false
    s = false
    z = false
    o = false
  }

}

class ALU {
  var o1 = DWord()
  var o2 = DWord()
  var res = DWord()
  var op: ALUOp = CMP

  var flags = new Flags()

  def reset() { flags.reset() }
  
  def setOps(op:ALUOp,o1:Word,o2:Word){
    this.op=op
    this.o1=o1.toDWord()
    this.o2=o2.toDWord()
  }
  def setOps(op:ALUOp,o:Word){
    this.op=op
    this.o1=o.toDWord()
    this.o2=DWord()
  }
  def setOps(op:ALUOp,o1:DWord,o2:DWord){
    this.op=op
    this.o1=o1
    this.o2=o2
  }
  def setOps(op:ALUOp,o:DWord){
    this.op=op
    this.o1=o
    this.o2=DWord()
  }
  def applyOp(op: ALUOpUnary, o: DWord):DWord= {
    setOps(op, o)

    val (result, newFlags) = op match {
      case au: ArithmeticOpUnary => { 
        //TODO
        (DWord(),new Flags())
      }
      case lu: LogicalOpUnary    => { 
        (DWord(),new Flags())
      }
    }
    this.res = result
    this.flags = newFlags
    result
  }
  
  def applyOp(op: ALUOpBinary, o1: DWord, o2: DWord):DWord= {
    //TODO
    setOps(op, o1,o2)
    
    DWord(1)
  }
  
  def applyOp(op: ALUOpBinary, o1: Word, o2: Word):Word = {
    setOps(op, o1,o2)

    val (result, newFlags) = op match {
      case ab: ArithmeticOpBinary => { arithmetic(ab,o1, o2, flags.c.toInt) }
      case lb: LogicalOpBinary    => { logical(lb, o1, o2) }
    }
    res = result.toDWord()
    flags = newFlags
    result
  }

  def applyOp(op: ALUOpUnary, o: Word):Word = {
    setOps(op, o)

    val (result, newFlags) = op match {
      case au: ArithmeticOpUnary => { arithmetic(au,o) }
      case lu: LogicalOpUnary    => { logical(lu,o) }
    }
    this.res = result.toDWord()
    this.flags = newFlags
    result
  }
  
  def applyOp(op: ArithmeticOpBinary, v: Int, w: Int, carry: Int) = {
      op match {
        case ADD => v + w
        case ADC => v + w + carry
        case SUB => v - w
        case SBB => v - w - carry
        case CMP => v - w
      }
    }
    def logical(op: LogicalOpUnary,w:Word): (Word, Flags) = {
      val result = op match {
        case NOT => (~w).toByte
        case NEG => (-w).toByte
      }
      (result, logicalFlags(result))
    }
    def logicalFlags(result: Word) = {
      val f = new Flags()
      f.o = false
      f.c = false
      f.z = result == 0
      f.s = result.sign
      f

    }
    def applyLogical(op:LogicalOpBinary,b1: Int, b2: Int) = op match {
        case OR  => b1 | b2
        case AND => b1 & b2
        case XOR => b1 ^ b2
    }
    
    def logical(op: LogicalOpBinary,w:Word, v: Word): (Word, Flags) = {

      val result = (w.bits.zip(v.bits()) map { case (b1, b2) => applyLogical(op,b1, b2) }).toByte

      (result, logicalFlags(result))
    }

    def arithmetic(op: ArithmeticOpBinary,w:Word, v: Word, carry: Int = 0): (Word, Flags) = {
      val f = new Flags()

      var res = applyOp(op, w.toInt, v.toInt, carry)
      var unsignedRes = applyOp(op, signedToUnsignedByte(w.toInt), signedToUnsignedByte(v.toInt), carry)
      //     println(s"$unsignedRes $res $v $w")

      if (res < -128) {
        f.o = true
        res += 256
      }
      if (res > 127) {
        f.o = true
        res -= 256
      }

      if (unsignedRes > 255 || unsignedRes < 0) {
        f.c = true
      }

      val byteRes = res.toByte
      f.s = byteRes.sign
      f.z = byteRes == 0
      (byteRes, f)
    }
    def arithmeticDWord(op: ArithmeticOpBinary,w:DWord, v:DWord, carry: Int = 0): (DWord, Flags) = {
      //TODO
      (DWord(),new Flags())  
    }

    def arithmetic(op: ArithmeticOpUnary,w:Word): (Word, Flags) = {
      op match {
        case INC => arithmetic(ADD,w,Word(1))
        case DEC => arithmetic(SUB,w,Word(1))
      }
    }
    def arithmetic(op: ArithmeticOpUnary,w:DWord): (DWord, Flags) = {
      op match {
        case INC => arithmeticDWord(ADD,w,DWord(1))
        case DEC => arithmeticDWord(SUB,w,DWord(1))
      }
    }


}

class CPU {

  //gp registers
  var sp = Simulator.maxMemorySize
  var ip = 0x2000
  var halted = false
  val alu = new ALU()
  var registers = mutable.Map[FullRegister, DWord](AX -> DWord(), BX -> DWord(), CX -> DWord(), DX -> DWord())

  def reset() {
    ip = 0x2000
    sp = Simulator.maxMemorySize
    halted = false
    registers = mutable.Map[FullRegister, DWord](AX -> DWord(), BX -> DWord(), CX -> DWord(), DX -> DWord())
  }

  def get(r: FullRegister): DWord = {
    registers(r)
  }
  def set(r: FullRegister, v: DWord) {
    registers(r) = v
  }

  def get(r: HalfRegister): Word = {
    r match {
      case r: LowRegister  => get(r.full).l
      case r: HighRegister => get(r.full).h
    }
  }

  def set(r: HalfRegister, v: Word) {
    r match {
      case r: LowRegister  => set(r.full, DWord(v, get(r.high)))
      case r: HighRegister => set(r.full, DWord(get(r.low), v))
    }
  }

}


class Memory {
  val values=randomBytes().map(Word(_))
  
  def randomBytes()={
    val values = Array.ofDim[Byte](Simulator.maxMemorySize)
    new Random().nextBytes(values)  
    values
  }
  def getByte(address: Int) = {
    values(address)
  }
  def getBytes(address: Int): DWord = {
    DWord(values(address), values(address + 1))
  }
  def setByte(address: Int, v: Word) {
    values(address) = v
  }
  def setBytes(address: Int, v: DWord) {
    values(address) = v.l
    values(address + 1) = v.h
  }

}


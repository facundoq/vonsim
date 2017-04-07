package vonsim.simulator

sealed class UnaryOperand

sealed trait WordOperand
sealed trait DWordOperand

class UnaryOperandUpdatable extends UnaryOperand 


class MemoryOperand extends UnaryOperandUpdatable 

case class DWordMemoryAddress(address:Int) extends MemoryOperand with DWordOperand 
case class WordMemoryAddress(address:Int) extends MemoryOperand with WordOperand

case object DWordIndirectMemoryAddress extends MemoryOperand  with DWordOperand 
case object WordIndirectMemoryAddress extends MemoryOperand with WordOperand

class InmediateOperand extends UnaryOperand
case class DWordValue(v:Int) extends InmediateOperand with DWordOperand
case class WordValue(v:Int) extends InmediateOperand with WordOperand

class Register extends UnaryOperandUpdatable

class FullRegister extends Register with DWordOperand

class HalfRegister extends Register with WordOperand{
   
  def full={
    this match{
      case AH => AX
      case BH => BX
      case CH => CX
      case DH => DX
      case AL => AX
      case BL => BX
      case CL => CX
      case DL => DX
    }
  }
   
  
}
class HighRegister extends HalfRegister{
  def low={
    this match{
      case AH => AL
      case BH => BL
      case CH => CL
      case DH => DL
    }
  }
}
class LowRegister extends HalfRegister{
  def high={
    this match{
      case AL => AH
      case BL => BH
      case CL => CH
      case DL => DH
    }
  }
  
}

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



abstract class BinaryOperands{
  
}

abstract class WordBinaryOperands extends BinaryOperands{
  def o1:WordOperand
  def o2:WordOperand
}

abstract class DWordBinaryOperands extends BinaryOperands{
  def o1:DWordOperand
  def o2:DWordOperand
}

case class DWordRegisterRegister(o1:FullRegister,o2:FullRegister) extends DWordBinaryOperands
case class WordRegisterRegister(o1:HalfRegister,o2:HalfRegister) extends WordBinaryOperands

case class WordRegisterMemory(o1:HalfRegister,o2:WordMemoryAddress) extends WordBinaryOperands
case class WordMemoryRegister(o1:WordMemoryAddress,o2:HalfRegister) extends WordBinaryOperands
case class DWordRegisterMemory(o1:FullRegister,o2:DWordMemoryAddress) extends DWordBinaryOperands
case class DWordMemoryRegister(o1:DWordMemoryAddress,o2:FullRegister) extends DWordBinaryOperands

case class WordRegisterDirect(o1:HalfRegister,o2:WordValue) extends WordBinaryOperands
case class DWordRegisterDirect(o1:FullRegister,o2:DWordValue) extends DWordBinaryOperands
case class WordMemoryDirect(o1:WordMemoryAddress,o2:WordValue) extends WordBinaryOperands
case class DWordMemoryDirect(o1:DWordMemoryAddress,o2:DWordValue) extends DWordBinaryOperands




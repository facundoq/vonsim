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
trait GeneralPurposeRegister

case object AX extends FullRegister with IORegister with GeneralPurposeRegister
case object BX extends FullRegister with IndirectRegister with GeneralPurposeRegister  
case object CX extends FullRegister with GeneralPurposeRegister
case object DX extends FullRegister with GeneralPurposeRegister

case object SP extends FullRegister
case object IP extends FullRegister

case object AH extends HighRegister with GeneralPurposeRegister
case object BH extends HighRegister with GeneralPurposeRegister
case object CH extends HighRegister with GeneralPurposeRegister
case object DH extends HighRegister 

case object AL extends LowRegister with IORegister with GeneralPurposeRegister
case object BL extends LowRegister with GeneralPurposeRegister
case object CL extends LowRegister with GeneralPurposeRegister
case object DL extends LowRegister with GeneralPurposeRegister



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
case class DWordRegisterIndirectMemory(o1:FullRegister,o2:DWordIndirectMemoryAddress.type) extends DWordBinaryOperands
case class WordRegisterIndirectMemory(o2:HalfRegister,o1:WordIndirectMemoryAddress.type) extends WordBinaryOperands
case class DWordRegisterMemory(o1:FullRegister,o2:DWordMemoryAddress) extends DWordBinaryOperands
case class WordRegisterMemory(o1:HalfRegister,o2:WordMemoryAddress) extends WordBinaryOperands
case class DWordRegisterValue(o1:FullRegister,o2:DWordValue) extends DWordBinaryOperands
case class WordRegisterValue(o1:HalfRegister,o2:WordValue) extends WordBinaryOperands

case class DWordMemoryRegister(o1:DWordMemoryAddress,o2:FullRegister) extends DWordBinaryOperands
case class WordMemoryRegister(o1:WordMemoryAddress,o2:HalfRegister) extends WordBinaryOperands
case class DWordMemoryValue(o1:DWordMemoryAddress,o2:DWordValue) extends DWordBinaryOperands
case class WordMemoryValue(o1:WordMemoryAddress,o2:WordValue) extends WordBinaryOperands


case class DWordIndirectMemoryRegister(o1:DWordIndirectMemoryAddress.type,o2:FullRegister) extends DWordBinaryOperands
case class WordIndirectMemoryRegister(o1:WordIndirectMemoryAddress.type,o2:HalfRegister) extends WordBinaryOperands
case class DWordIndirectMemoryValue(o1:DWordIndirectMemoryAddress.type,o2:DWordValue) extends DWordBinaryOperands
case class WordIndirectMemoryValue(o1:WordIndirectMemoryAddress.type,o2:WordValue) extends WordBinaryOperands





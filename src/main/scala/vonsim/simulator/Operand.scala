package vonsim.simulator

class UnaryOperand

trait WordOperand
trait DWordOperand

class UnaryOperandUpdatable extends UnaryOperand

class MemoryOperand extends UnaryOperandUpdatable 

case class MemoryAddress(address:Int) extends MemoryOperand with DWordOperand with WordOperand
case class IndirectMemoryAddress(r:IndirectRegister) extends MemoryOperand with DWordOperand with WordOperand

class DirectOperand extends UnaryOperand
case class DWordValue(v:Int) extends DirectOperand
case class WordValue(v:Int) extends DirectOperand

class Register extends UnaryOperandUpdatable

class FullRegister extends Register

class HalfRegister extends Register{
   
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

class ALUOp
class ALUOpBinary extends ALUOp
class ALUOpUnary extends ALUOp
class ALUOpCompare extends ALUOp

case object CMP extends ALUOpCompare
class ArithmeticOpBinary extends ALUOpBinary
case object ADD extends ArithmeticOpBinary
case object ADC extends ArithmeticOpBinary
case object SBB extends ArithmeticOpBinary

class ArithmeticOpUnary extends ALUOpUnary
case object INC extends ArithmeticOpUnary
case object DEC extends ArithmeticOpUnary

class LogicalOpBinary extends ALUOpBinary
case object XOR extends LogicalOpBinary
case object OR extends LogicalOpBinary
case object AND extends LogicalOpBinary

class LogicalOpUnary extends ALUOpUnary
case object NOT extends LogicalOpUnary
case object NEG extends LogicalOpUnary



abstract class BinaryOperands{
  
}

abstract class WordBinaryOperands{
  def o1:WordOperand
  def o2:WordOperand
}

abstract class DWordBinaryOperands{
  def o1:DWordOperand
  def o2:DWordOperand
}

case class DWordRegisterRegister(o1:FullRegister,o2:FullRegister) extends DWordBinaryOperands
case class WordRegisterRegister(o1:HalfRegister,o2:HalfRegister) extends WordBinaryOperands

case class WordRegisterMemory(o1:HalfRegister,o2:MemoryOperand) extends BinaryOperands
case class WordMemoryRegister(o1:MemoryOperand,o2:HalfRegister) extends BinaryOperands
case class DWordRegisterMemory(o1:FullRegister,o2:MemoryOperand) extends DWordBinaryOperands
case class DWordMemoryRegister(o1:MemoryOperand,o2:FullRegister) extends DWordBinaryOperands

case class WordRegisterDirect(o1:HalfRegister,o2:WordValue) extends WordBinaryOperands
case class DWordRegisterDirect(o1:FullRegister,o2:DWordValue) extends DWordBinaryOperands
case class WordMemoryDirect(o1:MemoryOperand,o2:WordValue) extends WordBinaryOperands
case class DWordMemoryDirect(o1:MemoryOperand,o2:DWordValue) extends DWordBinaryOperands




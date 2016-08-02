package vonemu.lang

import enumeratum._

sealed trait Register extends EnumEntry

object Register extends Enum[Register] {
 
  case object AX extends Register
  case object BX extends Register
  case object CX extends Register
  case object DX extends Register
  case object AH extends Register
  case object BH extends Register
  case object CH extends Register
  case object DH extends Register
  case object AL extends Register
  case object BL extends Register
  case object CL extends Register
  case object DL extends Register
  val values = findValues
 
}


object Language {

  
  type MemoryAdress = Int
   sealed abstract class Instruction(val line:Int) {

  }

  case class AddRR(override val line:Int,val a: Register,val b: Register) extends Instruction(line)
  case class SubRR(override val line:Int,val a: Register,val b: Register) extends Instruction(line)
  case class AddRM(override val line:Int,val a: Register,val b: MemoryAdress) extends Instruction(line)
  case class SubRM(override val line:Int,val a: Register,val b: MemoryAdress) extends Instruction(line)
  case class AddMR(override val line:Int,val a: MemoryAdress,val b: Register) extends Instruction(line)
  case class SubMR(override val line:Int,val a: MemoryAdress,val b: Register) extends Instruction(line)
  
}
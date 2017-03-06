package vonemu.lang

//import enumeratum.Enum
//import enumeratum.EnumEntry
//
// trait Token 
// 
// case class Label(val label:String) extends Token
// case class Number(val number:Int) extends Token
// 
// trait EnumToken extends EnumEntry with  Token
//sealed trait Register extends EnumToken
//object Register extends Enum[Register] {
//
//  case object AX extends Register
//  case object BX extends Register
//  case object CX extends Register
//  case object DX extends Register
//  case object AH extends Register
//  case object BH extends Register
//  case object CH extends Register
//  case object DH extends Register
//  case object AL extends Register
//  case object BL extends Register
//  case object CL extends Register
//  case object DL extends Register
//  val values = findValues
//}
//
//sealed trait Operation extends EnumToken
//
//sealed trait BinaryOperation extends Operation
//object BinaryOperation extends Enum[BinaryOperation] {
//
//  case object ADD extends BinaryOperation
//  case object SUB extends BinaryOperation
//  case object MOV extends BinaryOperation
//
//  val values = findValues
//}
//
//sealed trait UnaryOperation extends Operation
//object UnaryOperation extends Enum[UnaryOperation] {
//
//  case object NOT extends UnaryOperation
//  case object JMP extends UnaryOperation
//  case object JNO extends UnaryOperation
//  case object ORG extends UnaryOperation
//
//  val values = findValues
//}
//sealed trait ZeroArityOperation extends Operation
//object ZeroArityOperation extends Enum[ZeroArityOperation] {
//
//  case object RET extends ZeroArityOperation
//  case object END extends ZeroArityOperation
//
//  val values = findValues
//}

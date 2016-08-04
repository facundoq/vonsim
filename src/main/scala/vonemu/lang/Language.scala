package vonemu.lang

import enumeratum._




  object RegisterID extends Enumeration {
    type RegisterID = Value
    val a, b, c, d = Value
  }

  object RegisterPart extends Enumeration {
     type RegisterPart = Value
    val L, H, X = Value // low high all
  }
  import RegisterID._
  import RegisterPart._
   
  sealed abstract class Operand

  case class Register(id: RegisterID, part: RegisterPart) extends Operand
  case class MemoryAdress(adress:Int) extends Operand  
  
  trait Operator
  trait ZeroaryOperator extends Operator
  case object Ret extends ZeroaryOperator
  case object Hlt extends ZeroaryOperator
  case object End extends ZeroaryOperator
 
  trait UnaryOperator extends Operator
  case object Org extends UnaryOperator
  case object Not extends UnaryOperator
  case object Jmp extends UnaryOperator

 trait BinaryOperator extends Operator
  case object Add extends BinaryOperator
  case object Sub extends BinaryOperator
  case object Mov extends BinaryOperator

  abstract class Instruction(val line: Int) 
  class BinaryInstruction(line:Int,val o:BinaryOperator,val a:Operand,val b:Operand) extends Instruction(line)
  class UnaryInstruction(line:Int,val o:UnaryOperator,val a:Operand) extends Instruction(line)
  class ZeroaryInstruction(line:Int,val o:ZeroaryOperator) extends Instruction(line)
  

  class Line(line: Int, tokens: Array[String])

  class ParseError(val line: Int, val reason: String)
class Parser{
  def parse(code: String): Either[Array[ParseError], Array[Instruction]] = {
    parse(code.split('\n'))
  }
  def parse(code: Array[String]): Either[Array[ParseError], Array[Instruction]] = {
    val tokensPerLine = code.map(line => line.split(' '))
    val lines = tokensPerLine.zipWithIndex.map { case (tokens, line) => new Line(line, tokens) }
    val parsedLines = lines.map(parseLine)
    val errors = parsedLines.filter(p => p.isLeft).map(_.left.get)
    if (!errors.isEmpty) {
      return Left(errors)
    }
    val correctlyParsedLines = parsedLines.filter(p => p.isRight).map(_.right.get)
    return semanticalCheck(correctlyParsedLines)

  }
  def semanticalCheck(instructions: Array[Instruction]): Either[Array[ParseError], Array[Instruction]] = {

    return Right(instructions)
  }

  def parseLine(line: Line): Either[ParseError, Instruction] = {

    return null
  }
}

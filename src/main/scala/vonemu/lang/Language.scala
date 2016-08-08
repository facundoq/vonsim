package vonemu.lang

import enumeratum._

/* TODO
 Implement operator parsing
 Implement unary operator parsing
 Separate lang definition, parsing, semantic checks
 Implement semantic checks
 Test parsing
 Implement full language parsing
 test full language parsing
 implement language simulation
 implement UI
*/

class Program(val definitions:Array[VariableDefinition],val instructions:Array[Instruction],val labels:Array[JumpLabel])


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
case class Inmediate(value:Int) extends Operand
case class IndirectMemory(id: RegisterID) extends Operand
case class Register(id: RegisterID, part: RegisterPart) extends Operand
//case class MemoryAdress(adress: Int) extends Operand
case class Label(id:String) extends Operand

trait Operator
trait ZeroaryOperator extends Operator
case object Ret extends ZeroaryOperator
case object IRet extends ZeroaryOperator
case object Cli extends ZeroaryOperator
case object Sti extends ZeroaryOperator
case object Hlt extends ZeroaryOperator
case object End extends ZeroaryOperator
case object Nop extends ZeroaryOperator
case object PushF extends ZeroaryOperator
case object PopF extends ZeroaryOperator


trait UnaryOperator extends Operator
case object Org extends UnaryOperator
case object IntN extends UnaryOperator
case object Call extends UnaryOperator

trait UnaryArithmeticOperator extends UnaryOperator
case object Not extends UnaryOperator
case object Neg extends UnaryOperator
case object Inc extends UnaryOperator
case object Dec extends UnaryOperator


trait Stack extends UnaryArithmeticOperator
case object Push extends Stack 
case object Pop extends Stack 

trait Jump extends UnaryOperator
case object Jmp extends Jump
case object Jc extends Jump
case object Jnc extends Jump
case object Jz extends Jump
case object Jnz extends Jump
case object Jo extends Jump
case object Jno extends Jump
case object Js extends Jump
case object Jns extends Jump


trait BinaryOperator extends Operator
case object Add extends BinaryOperator
case object Sub extends BinaryOperator
case object Mov extends BinaryOperator
case object Cmp extends BinaryOperator
case object Adc extends BinaryOperator
case object Sbb extends BinaryOperator
case object And extends BinaryOperator
case object Or extends BinaryOperator
case object Xor extends BinaryOperator
case object In extends BinaryOperator
case object Out extends BinaryOperator

abstract class Instruction(val line: Int)
class BinaryInstruction(line: Int, val o: BinaryOperator, val a: Operand, val b: Operand) extends Instruction(line)
class UnaryInstruction(line: Int, val o: UnaryOperator, val a: Operand) extends Instruction(line)
class ZeroaryInstruction(line: Int, val o: ZeroaryOperator) extends Instruction(line)

object VariableType extends Enumeration {
  type VariableType = Value
  val DB, DW= Value
}
import VariableType._
class VariableDefinition(val line:Int, val id:String,val t:VariableType,values:Array[Int])

class JumpLabel(val line:Int,val id:String)


class Line(val line: Int,val tokens: Array[String])

object ParseErrorType extends Enumeration {
  type ParseErrorType = Value
  val VariableDefinitionWithLabel,InvalidLabelSyntax, InvalidNumberOfOperands,FirstOperandHasNoComma = Value
}

import ParseErrorType._

class ParseError(val line: Int, val reason: ParseErrorType)

class Parser {
  def parse(code: String): Either[Array[ParseError], Program] = {
    parse(code.split('\n'))
  }
  def parse(code: Array[String]): Either[Array[ParseError], Program] = {
    val result=syntacticCheck(code)
    return if (result.isLeft) result else return semanticCheck(result.right.get)
  }
  
  def syntacticCheck(code: Array[String]): Either[Array[ParseError], Program] = {
    val tokensPerLine = code.map(line => line.split(' ').map(_.trim()))
    val nonemptyTokensPerLine= tokensPerLine.filter(_.isEmpty)
    val lines = nonemptyTokensPerLine.zipWithIndex.map { case (tokens, line) => new Line(line, tokens) }
    val parsedLines = lines.map(parseLine)
    val errors = parsedLines.filter(p => p.isLeft).map(_.left.get)
    if (!errors.isEmpty) {
      return Left(errors)
    }
    val correctlyParsedLinesResults = parsedLines.filter(p => p.isRight).map(_.right.get)
    val instructions= correctlyParsedLinesResults.flatMap{ case x:InstructionResult => Some(x.instruction) }
    val definitions = correctlyParsedLinesResults.flatMap{ case x:VariableDefinitionResult=> Some(x.definition) }
    
    val labels=correctlyParsedLinesResults.flatMap {  
      case x:LabeledInstructionResult => Some( new JumpLabel(x.instruction.line,x.label.id)) }
    
    val p=new Program(definitions,instructions,labels)
    
    return Right(p)
  }
  
  def semanticCheck(p:Program): Either[Array[ParseError], Program] = {
    val labelErrors=sematicCheckJumps(p)
    
    return Right(p)
  }
  def sematicCheckJumps(p:Program){
    p.instructions.filter { i => i.isInstanceOf[BinaryInstruction] } 
    
    for (jump <- p.instructions){
      
    }
    
  }
  
  def semanticCheck(instructions: Array[Instruction]): Either[Array[ParseError], Array[Instruction]] = {
    
    return Right(instructions)
  }
  
   def semanticCheck(instruction: Instruction): Either[ParseError, Instruction] = {
    
    return Right(instruction)
  }

  abstract class ParseLineResult()
  abstract class InstructionResult(val instruction:Instruction) extends ParseLineResult
  case class SimpleInstructionResult(i:Instruction) extends InstructionResult(i)
  case class LabeledInstructionResult(i:Instruction,val label:Label) extends InstructionResult(i)
  case class VariableDefinitionResult(val definition:VariableDefinition) extends ParseLineResult
  
  def labelSyntax(label:String):Option[ParseErrorType]={
    val pattern="^[a-zA-z][\\w]*$".r
    if (pattern.findFirstIn(label).isDefined) return None else return Some(InvalidLabelSyntax)
  }
  
  def parseLabel(line:Line):Either[ParseError,Option[Label]]={
    val tokens=line.tokens
    val token=tokens(0)
    if (token.endsWith(":")){
      tokens.drop(1)
      val labelString=token.take(token.length-1)
      labelSyntax(labelString) match{
        case None=> return Right(Some(new Label(labelString)))
        case Some(error) => return Left(new ParseError(line.line,error))
      }
    }
    return Right(None)
  }
  
  def parseOperand(operandLabel:String,line:Int):Either[ParseError,Operand]={
    return null
  }
  
  def parseInstruction(line:Line):Option[Either[ParseError,Instruction]]={
    val tokens=line.tokens
    val operatorLabel=tokens(0).toLowerCase()
    val error= (e:ParseErrorType) => Some(Left(new ParseError(line.line,e)))
    
    
    val zeroaryOperators= Map("ret" -> Ret,"end"->End,"hlt"->Hlt,"popf"->PopF,"pushf"->PushF)
    if (zeroaryOperators.keys.exists { x => x==operatorLabel}){
      if (tokens.length!=1) return error(InvalidNumberOfOperands)
      return Some(Right(new ZeroaryInstruction(line.line,zeroaryOperators(operatorLabel))))
    }
    
    val binaryOperators= Map("add" -> Add,"sub"->Sub,"mov"->Mov,"cmp"->Cmp)
    if (binaryOperators.keys.exists { x => x==operatorLabel}){
      val operator=binaryOperators(operatorLabel)
      if (tokens.length!=3) return error(InvalidNumberOfOperands)
      var operand1Label=tokens(1).toLowerCase()
      if (!operand1Label.endsWith(",")) return error(FirstOperandHasNoComma)
      operand1Label=operand1Label.take(operand1Label.length-1)
      val operand2Label=tokens(2).toLowerCase()
      
      val operand1=parseOperand(operand1Label,line.line)
      val operand2=parseOperand(operand2Label,line.line)
      operand1 match{
        case Left(e) => return Some(Left(e))
        case Right(o1) => operand2 match{
          case Left(e2) => return Some(Left(e2))
          case Right(o2) => return Some(Right(new BinaryInstruction(line.line,operator,o1,o2)))  
        }
      }
    }
    
    return None
  }
  
  
  
  def parseLine(line: Line): Either[ParseError, ParseLineResult] = {
    //label
    val possibleLabel=parseLabel(line)
    var label:Option[Label]=null
    possibleLabel match{
      case Left(error) => return Left(error)
      case Right(l)=> label=l
    }
    // check if instruction or variable declaration
    
    // Instruction 
    val instruction:Instruction=null
    
    return Right(label match{
      case None => new SimpleInstructionResult(instruction)
      case Some(l) => new LabeledInstructionResult(instruction,l)
    })
    
    // variable declaration
    val definition:VariableDefinition=null
    return label match{
      case None => Right(new VariableDefinitionResult(definition))
      case Some(l) => Left(new ParseError(line.line,VariableDefinitionWithLabel))
    }
  }
  
  
  
}

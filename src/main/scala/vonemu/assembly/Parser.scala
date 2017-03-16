package vonemu.assembly

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Reader}

object Parser extends Parsers {
  override type Elem = Token
  
  def apply(tokens: Seq[Token]): Either[ParserError, Instruction] = {
    val reader = new TokenReader(tokens)
    program(reader) match {
      case NoSuccess(msg, next) => Left(ParserError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, next) => Right(result)
    }
  }
  
  class TokenReader(tokens: Seq[Token]) extends Reader[Token] {
    override def first: Token = tokens.head
    override def atEnd: Boolean = tokens.isEmpty
    override def pos: Position = tokens.headOption.map(_.pos).getOrElse(NoPosition)
    override def rest: Reader[Token] = new TokenReader(tokens.tail)
  }

  def program = positioned {
     labeledInstruction | instruction  
  }
  def labeledInstruction =positioned {
    (label ~ instruction) ^^{case LABEL(l) ~ (o:Instruction) => LabeledInstruction(l,o)}
  }
  def instruction = positioned{
    zeroary | org | mov | jump  | arithmetic
  }
  def arithmetic: Parser[Instruction] = positioned {
    binaryArithmetic | unaryArithmetic 
  }
  def binaryArithmetic: Parser[Instruction] = positioned {
    (ADD() ~ mutable ~ COMMA() ~ value) ^^ { case ( (o:BinaryArithmeticOp) ~ (m:Mutable) ~ _ ~ (v:Value)) => BinaryArithmetic(o,m,v)}
  }
  def unaryArithmetic: Parser[Instruction] = positioned {
    (NOT() ~ mutable) ^^ { case ( (o:UnaryArithmeticOp) ~ (m:Mutable)) => UnaryArithmetic(o,m)}      
  }
  def org= positioned {
    (ORG() ~ literalInteger ) ^^ {case o ~ LITERALINTEGER(v) => Org(v)}
  }
  
  def mov= positioned {
    (MOV() ~ mutable ~ COMMA() ~ value) ^^ { case ( MOV() ~ (m:Mutable) ~ _ ~ (v:Value)) => Mov(m,v)}
  }
  
  def zeroary= positioned {
    val end =END() ^^ (_ => End())
    val ret = RET() ^^ (_ => Ret())
    val nop = NOP() ^^ (_ => Nop())
    val hlt = HLT() ^^ (_ => Hlt())
    end | ret | nop | hlt
  }
  
  def jump = jmp | conditionalJump | call
  def jmp = positioned {
    (JMP() ~ identifier ) ^^ {case JMP()  ~ IDENTIFIER(i) => UnconditionalJump(i)}
  }
  
  def call = positioned {
    (CALL() ~ identifier ) ^^ {case CALL() ~ IDENTIFIER(i) => Call(i)}
  }
  def conditionalJump = positioned {
    (conditionalJumpTokens  ~ identifier ) ^^ {case (o:ConditionalJumpToken) ~ IDENTIFIER(i) => ConditionalJump(o,i)}
  }
  
  private def literalString = positioned {
    accept("string literal", { case lit @ LITERALSTRING(v) => lit })
  }
  private def identifier = positioned {
    accept("identifier", { case lit @ IDENTIFIER(v) => lit })
  }
   private def label = positioned {
    accept("label", { case lit @ LABEL(v) => lit })
  }
  private def literalInteger = positioned {
    accept("integer literal", { case lit @ LITERALINTEGER(v) => lit })
  } 
  
  def conditionalJumpTokens = positioned{
    (Token.conditionalJump map  tokenAsParser) reduceLeft(_ | _)
  }
  def tokenAsParser(t:Token)= t ^^^ t
  
   
  private def mutable = positioned{
    ((Token.registers map tokenAsParser) reduceLeft(_ | _) ) | identifier | INDIRECTBX()
  }
   private def value = positioned{
    mutable | literalInteger
  }
  
  
}
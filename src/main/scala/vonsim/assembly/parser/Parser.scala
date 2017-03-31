package vonsim.assembly.parser

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Reader}
import vonsim.assembly.lexer._
import scala.Left
import scala.Right

import vonsim.assembly.ParserError
import vonsim.assembly.Location


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
    zeroary | org | mov | jump  | arithmetic | cmp | io | intn | stack | vardef 
  }
  def arithmetic= positioned {
    binaryArithmetic | unaryArithmetic 
  }
  
  def binaryArithmetic: Parser[Instruction] = positioned {
    (binary ~ mutable ~ COMMA() ~ value) ^^ { case ( (o:BinaryArithmeticOp) ~ (m:Mutable) ~ _ ~ (v:Value)) => BinaryArithmetic(o,m,v)}
  }
  def binary= (Token.binaryArithmetic map tokenAsParser) reduceLeft(_ | _)
  
  def unaryArithmetic: Parser[Instruction] = positioned {
    (unary ~ mutable) ^^ { case ( (o:UnaryArithmeticOp) ~ (m:Mutable)) => UnaryArithmetic(o,m)}      
  }
  def unary = (Token.unaryArithmetic map tokenAsParser) reduceLeft(_ | _)
  
  
  def io = positioned {
    ((IN() | OUT()) ~ (AL() | AX()) ~ COMMA() ~ (ioaddress)) ^^ { case ( (o:IOToken) ~ (m:IORegister) ~ _ ~ (a:IOAddress)) => IO(o,m,a)} 
  }
  
  def cmp = positioned {
    (CMP() ~ (value) ~ COMMA() ~ (value)) ^^ { case ( CMP() ~ (v1:Value) ~ _ ~ (v2:Value)) => Cmp(v1,v2)} 
  }
  
  
  def intn= positioned {
    (INT() ~ literalInteger ) ^^ {case o ~ LITERALINTEGER(v) => IntN(v)}
  }
  def org= positioned {
    (ORG() ~ literalInteger ) ^^ {case o ~ LITERALINTEGER(v) => Org(v)}
  }
  def stack= positioned {
    ((PUSH() | POP()) ~ fullRegister ) ^^ {case (o:StackInstruction) ~ (t:FullRegisterToken) => Stack(o,t)}
  }
  
  def mov= positioned {
    (MOV() ~ mutable ~ COMMA() ~ value) ^^ { case ( MOV() ~ (m:Mutable) ~ _ ~ (v:Value)) => Mov(m,v)}
  }
  
  def zeroary= positioned {
    val end =END() ^^ (_ => End())
    val ret = RET() ^^ (_ => Ret())
    val nop = NOP() ^^ (_ => Nop())
    val hlt = HLT() ^^ (_ => Hlt())
    val cli = CLI() ^^ (_ => Cli())
    val sti = STI() ^^ (_ => Sti())
    val iret = IRET() ^^ (_ => IRet())
    val pushf = PUSHF() ^^ (_ => Pushf())
    val popf = POPF() ^^ (_ => Popf())
    end | ret | nop | hlt | cli | sti | iret | pushf | popf
  }
  
  def jump = jmp | conditionalJump | call
  def jmp = positioned {
    (JMP() ~ identifier ) ^^ {case JMP()  ~ IDENTIFIER(i) => UnconditionalJump(i)}
  }
  
  def call = positioned {
    (CALL() ~ identifier ) ^^ {case CALL() ~ IDENTIFIER(i) => Call(i)}
  }
  def vardef = positioned {
    val ints= (label ~ (DB() | DW() ) ~ varDefInts  )  ^^ {
      case LABEL(id)~ (t:VarType) ~ (v:List[LITERALINTEGER]) => VarDef(id,t,v.map(_.v))}
    val str = (label ~ DB() ~ literalString ) ^^ {case LABEL(id)~ DB() ~ LITERALSTRING(s) => VarDef(id,DB(),stringToIntList(s))}
    val empty = (label ~ (DB() | DW() ) ~ UNINITIALIZED()) ^^ {case LABEL(id)~ (t:VarType) ~ UNINITIALIZED() => VarDef(id,t,List())}
    str | ints | empty
  }
  
  def stringToIntList(s:String)=s.map( (c:Char) => c.charValue().toInt).toList
  
  def varDefInts = rep1sep(literalInteger, COMMA())
  
  
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
  
  private def fullRegister = positioned{
    (Token.xRegisters map tokenAsParser) reduceLeft(_ | _)
  }
  private def ioaddress = positioned{
    identifier | literalInteger | DX()
  }
  
  
}
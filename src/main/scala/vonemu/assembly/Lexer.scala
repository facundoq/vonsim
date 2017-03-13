package vonemu.assembly

import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.Positional
import scala.util.parsing.input.Position

class VonemuPosition(var line:Int, var column:Int,val lineContents:String) extends Position  {
  
}

object Lexer extends RegexParsers {
  override def skipWhitespace = true
  override val whiteSpace = "[ \t]+".r
  
  def apply(code: String): Either[LexerError, List[Token]] = {
    if (code.trim().isEmpty()){
       Right(List(EMPTY()))
    }else{
      parse(tokens, code) match {
        case NoSuccess(msg, next) => Left(LexerError(Location(next.pos.line, next.pos.column), msg))
        case Success(result, next) => Right(result)
      }
    }
  }
  
  def tokens: Parser[List[Token]] = {
    phrase(rep1(comma | end | add | nop | ret 
        | org | mov | literalnumber | literalstring | ax | bx | cx | dx | al | bl | cl | dl | 
        ah | bh | ch | dh | identifier )) ^^ { rawTokens =>
      rawTokens
    }
  }
  
def empty: Parser[EMPTY] = positioned {
    //"""^\s*$""".r ^^ { _ => EMPTY() }
    "".r ^^ { _ => EMPTY() }
}

def identifier: Parser[IDENTIFIER] = positioned {
    "[a-zA-Z][a-zA-Z0-9_]*".r ^^ { str => IDENTIFIER(str) }
  }
def literalnumber: Parser[LITERALINTEGER] = positioned {
    literalhex | literalbyte  | literaldec
}
def literaldec: Parser[LITERALINTEGER] = positioned {
  "(-)?[0-9]+".r ^^ { str => LITERALINTEGER(str.toInt)  }
}

def literalhex: Parser[LITERALINTEGER] = positioned {
  "(-)?[0-9][a-fA-F0-9]*[Hh]".r ^^ { str => LITERALINTEGER(Integer.parseInt(str.substring(0,str.length-1), 16)) }
}

def literalbyte: Parser[LITERALINTEGER] = positioned {
  "[0-1]{8}B".r ^^ { str => LITERALINTEGER(Integer.parseInt(str.substring(0,str.length-1), 2))  }
}

def literalstring: Parser[LITERALSTRING] = positioned {
    """"[^"]*"""".r ^^ { str =>
      val content = str.substring(1, str.length - 1)
      LITERALSTRING(content)
    }
}

def ax = positioned { "(?i)ax".r ^^ (_ => AX()) }
def bx = positioned { "(?i)bx".r ^^ (_ => BX()) }
def cx = positioned { "(?i)cx".r ^^ (_ => CX()) }
def dx = positioned { "(?i)dx".r ^^ (_ => DX()) }
def al = positioned { "(?i)al".r ^^ (_ => AL()) }
def bl = positioned { "(?i)bl".r ^^ (_ => BL()) }
def cl = positioned { "(?i)cl".r ^^ (_ => CL()) }
def dl = positioned { "(?i)dl".r ^^ (_ => DL()) }
def ah = positioned { "(?i)ah".r ^^ (_ => AH()) }
def bh = positioned { "(?i)bh".r ^^ (_ => BH()) }
def ch = positioned { "(?i)ch".r ^^ (_ => CH()) }
def dh = positioned { "(?i)dh".r ^^ (_ => DH()) }

def end = positioned { "(?i)end".r ^^ (_ => END()) }
def add = positioned { "(?i)add ".r ^^ (_ => ADD()) }
def nop = positioned { "(?i)nop".r ^^ (_ => NOP()) }
def ret = positioned { "(?i)ret".r ^^ (_ => RET()) }
def org = positioned { "(?i)org ".r ^^ (_ => ORG()) }
def mov = positioned { "(?i)mov ".r ^^ (_ => MOV()) }
  
def comma = positioned { "," ^^ (_ => COMMA()) }
  
  
}
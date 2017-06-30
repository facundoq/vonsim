package vonsim.assembly.lexer

import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.Position

//import Parsers.{Parsers => Lexer}


import vonsim.assembly.lexer._
import scala.Left
import scala.Right
import vonsim.assembly.LexerError
import vonsim.assembly.Location
import scala.collection.mutable.ListBuffer

class VonemuPosition(var line:Int, var column:Int,val lineContents:String) extends Position  {
  
}

object Lexer extends RegexParsers {
  override def skipWhitespace = false
  override val whiteSpace = "[ \t]+".r
  
  def apply(codeParameter: String): Either[LexerError, List[Token]] = {
    var code=codeParameter
    val commentStart=code.indexOf(";")
    if (commentStart != -1){
      code=code.substring(0,commentStart)
    }
    if (code.trim().isEmpty()){
       Right(List(EMPTY()))
    }else{
      
      parse(tokens, code+"\n") match {
        case NoSuccess(msg, next) => Left(LexerError(Location(next.pos.line, next.pos.column), msg))
        case Success(result, next) => {
//          println("Tokens:\n"+result)
          val filteredResult=result.filter(r => !r.isInstanceOf[WHITESPACE])
          println("Tokens:\n"+filteredResult)
          Right(filteredResult)
        }
      }
    }
  }
  def indicesWhere(s:String,t:Char)={
    s.zipWithIndex.collect { case (elem, idx) if elem==t => idx } 
  }
  def addSeparator(s:Seq[String],t:Char)={
    val l=s.length
    if (l==0){
      List[String]()
    }else{
      val r=ListBuffer(s(0))
      for ( i <- 1 to l  ){
        r+=t.toString()
        r+=s(i)
      }
      r.toList
    }
    
  }
  def splitRetainingDelimiter(s:String,t:Char)={
    val parts = s.split(t)
    addSeparator(parts, t) 
  }
    
  def separate(code:String)={
    val parts=code.split("\\t\\s")
    val tokens=parts.flatMap(s => {
      splitRetainingDelimiter(s, ',')
    })
    tokens
  }
  
  def tokens: Parser[List[Token]] = {
    phrase( rep1(whitespace |comma | uninitialized | offsetLabel | indirectbx | varType | indirect |  label | flagsStack | stack | keyword | literal 
        | ops | io | interrupt |  register | jumps | expression |  identifier | newline )) ^^ { rawTokens =>
      rawTokens
    }
  }
  
//def empty: Parser[EMPTY] = positioned {
//    //"""^\s*$""".r ^^ { _ => EMPTY() }
//    "".r ^^ { _ => EMPTY() }
//}


def whitespace=positioned {"[\\t ]+".r ^^ { str => WHITESPACE()}}
def register=orall (Token.registers map tokenParser2) 
def keyword=orall (Token.keyword map tokenParser2)
def ops=orall (Token.ops map tokenParserSpace)
def jumps=orall (Token.jump map tokenParserSpace)
def io=orall (Token.inputOutput map tokenParserSpace)
def interrupt=orall (Token.interrupt map tokenParserSpace)
def stack = orall (Token.stack map tokenParserSpace)
def flagsStack = orall (Token.flagsStack map tokenParserSpace)
def varType = orall (Token.varType map tokenParserSpace)

  
def tokenParser(t:Token,literal:String) = positioned {s"(?i)${literal.toLowerCase}".r ^^^ t}
def tokenParser2(t:Token) = tokenParser(t,t.toString)
def tokenParserSpace(t:Token) = positioned {s"(?i)${t.toString.toLowerCase}\\s".r ^^^ t} 
def orall(x: List[Lexer.Parser[Token]]) = x.reduceLeft( _ | _)

  
def identifier = positioned {
    "[a-zA-Z][a-zA-Z0-9_]*".r ^^ { str => IDENTIFIER(str) }
  }

def label= positioned {
    "[a-zA-Z][a-zA-Z0-9_]*:".r ^^ { str => LABEL(str.substring(0, str.length()-1)) }
  }
def literal = literalnumber | literalstring 
def literalnumber= positioned {
    literalhex | literalbyte  | literaldec
}
def literaldec= positioned {
  "(-)?[0-9]{1,7}".r ^^ { str => {

    LITERALINTEGER(str.toInt) 
    } 
  }
}

def literalhex= positioned {
  "(-)?[0-9][a-fA-F0-9]*[Hh]".r ^^ { str => LITERALINTEGER(Integer.parseInt(str.substring(0,str.length-1), 16)) }
}

def literalbyte= positioned {
  "[0-1]{1,16}B".r ^^ { str => {
    val byteString=str.substring(0,str.length-1)
    
    LITERALINTEGER(Integer.parseInt(byteString, 2))  
   }
  }
}


def literalstring = positioned {
    """"[^"]*"""".r ^^ { str =>
      val content = str.substring(1, str.length - 1)
      LITERALSTRING(content)
    }
}

//def unknown = positioned{
//  """(.){0,30}""".r ^^^ UNKNOWN()
//}
def newline = positioned { """(\r?\n)+""".r ^^^ NEWLINE() }

def comma = positioned { "," ^^^ COMMA() }
def uninitialized = positioned { "?" ^^^ UNINITIALIZED() }

def indirectbx = positioned{"""(?i)\[bx\]""".r ^^^ INDIRECTBX()}

def indirectWord =tokenParser2(BYTE()) 
def indirectDWord = tokenParser2(WORD())
def indirectPTR = tokenParser2(PTR())
def indirect = (indirectWord | indirectDWord | indirectPTR | indirectbx)


def symbolParser(token:Token,symbol:String) = positioned {s"[$symbol]".r ^^^ token}
def expression = ( symbolParser(PlusOp(),"+") 
                   | symbolParser(MinusOp(),"-")
                   | symbolParser(MultOp(),"*")
                   | symbolParser(OpenParen(),"(")
                   | symbolParser(CloseParen(),")")
                  )
def offsetLabel= positioned {
    """(?i)OFFSET [a-zA-Z][a-zA-Z0-9_]*""".r ^^ { str =>
      val content = str.substring(7, str.length)
      OFFSETLABEL(content)
    }
}
 

def fixLineNumbers(a: Array[Either[LexerError, List[Token]]])={
    a.indices.map(i => {
      var eitherList = a(i)
      if (eitherList.isRight) {
        var list = eitherList.right.get
        var fixedList = list.map(token => {
          var p = new VonemuPosition(i+1, token.pos.column, "")
          token.pos=p
          token
        })
        Right(fixedList)
      }else{
        eitherList
      }
    }).toArray
  }
  
}
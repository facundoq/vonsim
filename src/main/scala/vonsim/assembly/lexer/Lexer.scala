package vonsim.assembly.lexer

import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.Position

//import Parsers.{Parsers => Lexer}


import vonsim.assembly.lexer._
import scala.Left
import scala.Right
import vonsim.assembly.LexerError
import vonsim.assembly.Location

class VonemuPosition(var line:Int, var column:Int,val lineContents:String) extends Position  {
  
}

object Lexer extends RegexParsers {
  override def skipWhitespace = true
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
        case Success(result, next) => Right(result)
      }
    }
  }
  
  def tokens: Parser[List[Token]] = {
    phrase( rep1(comma | uninitialized | indirectbx | varType |  label | flagsStack | stack | keyword | literal 
        | ops | io | interrupt |  register | jumps | identifier | newline )) ^^ { rawTokens =>
      rawTokens
    }
  }
  
//def empty: Parser[EMPTY] = positioned {
//    //"""^\s*$""".r ^^ { _ => EMPTY() }
//    "".r ^^ { _ => EMPTY() }
//}



def register=orall (Token.registers map tokenParser2) 
def keyword=orall (Token.keyword map tokenParser2)
def ops=orall (Token.ops map tokenParserSpace)
def jumps=orall (Token.jump map tokenParserSpace)
def io=orall (Token.inputOutput map tokenParserSpace)
def interrupt=orall (Token.interrupt map tokenParser2)
def stack = orall (Token.stack map tokenParserSpace)
def flagsStack = orall (Token.flagsStack map tokenParser2)
def varType = orall (Token.varType map tokenParserSpace)

def tokenParser(t:Token,literal:String) = positioned {s"(?i)${literal.toLowerCase}".r ^^^ t}
def tokenParser2(t:Token) = tokenParser(t,t.toString)
def tokenParserSpace(t:Token) = tokenParser(t,t.toString+" ")
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
//def indirectbx = positioned{"""(?i)\Q[BX]\E""".r ^^^ INDIRECTBX()}
//def brackets = openBracket | closeBracket 
//def openBracket = positioned { "[" ^^^ OPENBRACKET() }
//def closeBracket = positioned { "]" ^^^ CLOSEBRACKET() }
 

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
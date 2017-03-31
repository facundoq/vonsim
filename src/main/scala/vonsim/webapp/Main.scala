package vonsim.webapp

// tutorial https://www.scala-js.org/tutorial/basic/
// canvas https://github.com/vmunier/scalajs-simple-canvas-game/blob/master/src/main/scala/simplegame/SimpleCanvasGame.scala

import scala.scalajs.js.JSApp
import org.scalajs.dom
import org.scalajs.dom.Element

import dom.document
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all._
import org.scalajs.dom.html._
import org.scalajs.dom.raw.HTMLElement

import java.awt.Event
import scala.util.parsing.input.Position
import scala.scalajs.js.timers._



object Main extends JSApp {

  def main(): Unit = {
    val ui = new MainUI(defaultCode)
    document.body.appendChild(ui.root)
    ui.editorUI.editor.resize(true)
    setTimeout(2000)({ui.editorUI.editor.resize(true)
      })
    
    
  }

  def gencode() = {
    val r = "a b c d".split(" ").toList
    val rx = r.map(_ + "x")
    val rh = r.map(_ + "l")
    val rl = r.map(_ + "h")
    val ins = "end add nop ret org mov".split(" ").toList
    val all = rx ++ rh ++ rl ++ ins

    val lexerdefs = all map { r => s"""def $r = positioned { "$r" ^^ (_ => ${r.toUpperCase}()) }""" }
    val lexerdefsstr = lexerdefs.foldLeft("")((a, b) => a + "\n" + b)
    val lexeritems = all.foldLeft("")((a, b) => a + " | " + b) + "\n"

    lexeritems ++ lexerdefsstr
  }
  
  def defaultCode =
    """
org 1000h
asd: db "hola"
zzz: db "chau"
intlist: db 1,2,3,4
intlist2: dw 1,2,3,4
complex: db 10000000B,2,34h,4
uninitialized: db ?
uninitialized2: dW ?


org 2000h
mov ax, bx
MOV ax, bx
mov  AX, bX
   mov  ax, bx
mov  ax, bx   
mov     ax, bx
mov  ax   , bx
mov  ax,bx
hola: mov ax, bx
mov [bx],ax
mov ax, 2
mov ax, -25
mov ax, 25AH
mov ax, 25Ah
mov ax, 10001111B
not ax
add ax, bx
add ax, 3
add ax, 26h
adc ax, 26h
xor ax, 26h
cmp ax, 26h
mov ax,sp
in al,PIC
in al,123
out ax,dx
jc hola
org 1000


JMP HOLA
JC HOLA
CALL HOLA
RET
NOP
HLT
END
CLI
StI
ret
pushf
popf
push Ax
pop bx
pop CX
int 4
"""

}





  
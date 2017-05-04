package vonsim.simulator

import org.scalatest.FunSuite
import Simulator._
import ComputerWord._
import com.sun.org.apache.bcel.internal.generic.ArithmeticInstruction
import vonsim.assembly.Compiler
import vonsim.assembly.Compiler.SuccessfulCompilation
import scala.io.Source


class ProgramsSuite extends FunSuite {
  
  
  
  def simulator(program:String)={
    val compilation= Compiler(program)
    assert(compilation.isRight)
    val c=compilation.right.get
    Simulator(c)
  }
  
  
   test("vardef") {
     val program = 
"""
  org 1000h
  vardb: db 1
  vardw: dw 20
  
  org 1500h
  vardb2: db 2
  vardw2: dw 24
  
  org 2000h
  hlt  
  end
"""
    val s=simulator(program)
    assertResult(1)(s.memory.getByte(0x1000).toInt)
    assertResult(20)(s.memory.getByte(0x1001).toInt)
    assertResult(0)(s.memory.getByte(0x1002).toInt)
    assertResult(2)(s.memory.getByte(0x1500).toInt)
    assertResult(24)(s.memory.getByte(0x1501).toInt)
    assertResult(0)(s.memory.getByte(0x1502).toInt)
    s.step()
    assert(s.cpu.halted)
  }
      test("push pop") {

     val program = 
"""
  org 2000h
  mov ax,3
  push ax
  pop bx
  hlt  
  end
"""
    val s=simulator(program)
    s.step()
    assertResult(3)(s.cpu.get(AX).toInt)
    val sp=s.cpu.sp
    s.step()
    assertResult(sp-2)(s.cpu.sp)
    s.step()
    assertResult(3)(s.cpu.get(BX).toInt)
    assertResult(sp)(s.cpu.sp)
    s.step()
    assert(s.cpu.halted)
  }
  
   test("3+2=5 register") {
    val program= 
"""org 2000h
mov ax,3
add ax,2
hlt
end"""
    
    val s=simulator(program)
    s.step()
    assertResult(3)(s.cpu.get(AX).toInt)
    s.step()
    assertResult(5)(s.cpu.get(AX).toInt)
    s.step()
    assert(s.cpu.halted)
  }
  
   test("all syntax supported") {
     val program = 
"""
  
org 1000h
asd: db "hola"
zzz: db "chau"
intlist: db 1,2,3,4
intlist2: dw 1,2,3,4
complex: db 10000000B,2,34h,4
uninitialized: db ?
uninitialized2: dW ?

org 1500h
vardb: db 1
vardw: dw 2


    org 2000h
mov ax, bx
    MOV ax, bx
    mov  AX, bX
    mov  CX, Dx
    mov  ax, bx
    mov  ax, bx   
    mov     ax, bx
    mov  ax   , bx
    mov  ax,bx
hola: mov ax, bx
    mov [bx],ax
    mov ax, 2
    mov ax, -25
HOLA:    mov ax, 25AH
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
    mov vardb,1
    mov al,vardb
    mov ax,vardw
    mov vardw,4
;    in al,PIC ; not implemented yet!
;    in al,123 ; not implemented yet!
;    out ax,dx ; not implemented yet!
    jc hola
    
org 1000


JMP HOLA
JC HOLA
CALL HOLA
RET
NOP
HLT
CLI
StI
ret
pushf
popf
push ax
pop bx
pop cx
int 4
hlt

END
"""
    //val r=Source.fromURL(getClass.getResource("/assembly/all_syntax.asm")).bufferedReader()
    
    
    val s=simulator(program)
    
  }
   
  test("3+2=5 mem reg") {
    val program= 
"""
org 1000h
var: dw 2  
  org 2000h
mov ax,3
add ax,var
hlt
end"""
    
    val s=simulator(program)
    assertResult(2)(s.memory.getBytes(0x1000).toInt)
    s.step()
    assertResult(3)(s.cpu.get(AX).toInt)
    s.step()
    assertResult(5)(s.cpu.get(AX).toInt)
    s.step()
    assert(s.cpu.halted)
  }
  
   test("3+2=5 memory") {
     val program = 
"""
  org 1000h
  var: db 1
  
  org 2000h
  mov var,3
  add var,2
  hlt  
  end
"""
    val s=simulator(program)
    assertResult(1)(s.memory.getByte(0x1000).toInt)
    s.step()
    assertResult(3)(s.memory.getByte(0x1000).toInt)
    s.step()
    assertResult(5)(s.memory.getByte(0x1000).toInt)
    assertResult(new Flags(false,false,false,false))(s.cpu.alu.flags)
    s.step()
    assert(s.cpu.halted)
  }
   
  
  test("no org should throw error") {

     val program = 
"""
  mov ax,3
  end
"""
    val compilation= Compiler(program)
    assert(compilation.isLeft)
    
  }

   
   
}

  
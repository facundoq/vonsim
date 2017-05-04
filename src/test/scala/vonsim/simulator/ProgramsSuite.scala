package vonsim.simulator

import org.scalatest.FunSuite
import Simulator._
import ComputerWord._
import com.sun.org.apache.bcel.internal.generic.ArithmeticInstruction
import vonsim.assembly.Compiler
import vonsim.assembly.Compiler.SuccessfulCompilation


class ProgramsSuite extends FunSuite {
  
  
  
   
  
  
  test("3+2=5 register") {
    val program= 
"""org 2000h
mov ax,3
add ax,2
hlt
end"""
    val compilation= Compiler(program)
    //println(instructions)
    assert(compilation.isRight)
    val c=compilation.right.get
    
    val s=Simulator(c)
    println(s.cpu.ip)
    println(c.addressToInstruction)
    s.step()
    assertResult(3)(s.cpu.get(AX).toInt)
    s.step()
    assertResult(5)(s.cpu.get(AX).toInt)
    s.step()
    assert(s.cpu.halted)
  }
   
   
   
}


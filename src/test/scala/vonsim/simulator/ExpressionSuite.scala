package vonsim.simulator
import vonsim.utils.CollectionUtils._
import org.scalatest.FunSuite
import Simulator._
import ComputerWord._
import com.sun.org.apache.bcel.internal.generic.ArithmeticInstruction
import vonsim.assembly.Compiler
import vonsim.assembly.Compiler.SuccessfulCompilation
import scala.io.Source


class ExpressionSuite extends FunSuite {
  
  
  
  def simulator(program:String)={
    val compilation= Compiler(program)
    assert(compilation.isRight)
    val c=compilation.right.get
    Simulator(c)
  }
  
      
        test("integer expressions") {
     val program = 
"""
  org 1000h
 hola: dw 20
  org 2000h
  mov ax,2
  mov ax, 2 + 4
  mov ax, 2+3+4
  mov ax, 2+3*4
  mov ax, (2+3)*4
  mov ax, 2+ hola
  mov ax, hola + 2
  mov ax, hola
  mov hola,ax
  
  hlt
  end
"""
    val compilation= Compiler(program)
    println("Compilation output:"+compilation)
    assert(compilation.isRight)
    
}   

   
}

  
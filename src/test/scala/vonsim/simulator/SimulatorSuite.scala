package vonsim.simulator

import org.scalatest.FunSuite
import Simulator._

class SimulatorSuite extends FunSuite {

  test("Flags off") {
    val w=25.toByte
    val v=29.toByte
    val (r1,flags)= v.add(w)
    assert(r1==54)
    assert(!flags.z)
    assert(!flags.s)
    assert(!flags.o)
    assert(!flags.c)
    
  }
  test("0+0, z=1") {
    val w=0.toByte
    val v=0.toByte
    val (r1,flags)= v.add(w)
    assert(r1==0)
    assert(flags.z)
    assert(!flags.s)
    assert(!flags.o)
    assert(!flags.c)
    
  }

  
}
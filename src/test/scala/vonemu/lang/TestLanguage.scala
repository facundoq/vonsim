package vonemu.lang


import org.scalatest._
import org.scalatest.FunSuite
import vonemu.lang._

    
class LanguageSpec extends FlatSpec with Matchers{

  (2+2) should  equal (4)
  (2+2) should not equal 5
  (2+2) should === (4)
  (2+2) should be (4)
  (2+2) should not be 5
  
    val add=BinaryOperation.ADD
    val add2=BinaryOperation.ADD
    val sub=BinaryOperation.SUB
    val jmp=UnaryOperation.JMP
    add should not be sub
    add shouldBe add2
    add should  not be jmp  
}


class LanguageSuite extends FunSuite {
  
//    val a = new Language.AddRR(2,Register.AX,Register.BX)
    test("one plus one is two")(assert(1 + 1 == 2))
        val add=BinaryOperation.ADD
     val x:Token=add
  
     val b=x match {
      case x:Register => "Reg"
      case x:BinaryOperation => "BOP"
      case x:UnaryOperation => "UOP"
    }
    println(b)
}
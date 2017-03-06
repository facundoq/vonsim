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
  

}


class LanguageSuite extends FunSuite {
  
//    val a = new Language.AddRR(2,Register.AX,Register.BX)
    test("one plus one is two")(assert(1 + 1 == 2))
  
}
package vonsim.webapp

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.scalajs.js.annotation.JSName
import com.scalawarrior.scalajs.ace.Editor
import com.scalawarrior.scalajs.ace.Annotation
  
  @ScalaJSDefined
  @JSName("Ace")
  trait MyAce extends js.Object {
    def edit(): Editor
  }
  import js.Dynamic.global


object Annotation {
  def apply(row:Double,column:Double,text:String,`type`:String): Annotation = 
    js.Dynamic.literal(row = row, column= column,text = text, `type`=`type`).asInstanceOf[Annotation]  
}

package object webapp {
  lazy val myace: MyAce = global.ace.asInstanceOf[MyAce]
}
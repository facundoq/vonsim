package vonsim.webapp.tutorials

import vonsim.webapp.VonSimState
import vonsim.webapp.UIConfig




object TutorialStep{
  def apply(title:String,content:String,config:UIConfig=UIConfig())={
    new TutorialStep(title,content,config)
  }
}
class TutorialStep(val title:String,val content:String,val config:UIConfig){
  def canForward(s:VonSimState)=true
  def canBackward(s:VonSimState)=true
}

object Tutorial{
  val bt=new BasicTutorial()
  val tutorials=Map(bt.id -> bt)
}
abstract class Tutorial{
  def steps:List[TutorialStep]
  def title:String
  def id:String
  var step=0
  def hasNext=step<steps.length-1

  def canForward(s:VonSimState)={
    steps(step).canForward(s) && hasNext
  }
  def canBackward(s:VonSimState)={
    steps(step).canBackward(s) && hasPrevious
  }
  def hasPrevious=step>0
  def next{step+=1}
  def previous{step-=1}
  def current=steps(step)
  
  
}
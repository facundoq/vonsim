package vonsim.webapp.tutorials

import vonsim.webapp.VonSimState


object TutorialStepConfig{
  def apply(disableEditor:Boolean=false,disableSimulator:Boolean=false,disableControls:Boolean=false)={
    new TutorialStepConfig(disableEditor,disableSimulator,disableControls)
  }
}
class TutorialStepConfig(val disableEditor:Boolean,val disableSimulator:Boolean, val disableControls:Boolean){
  
}

object TutorialStep{
  def apply(title:String,content:String,config:TutorialStepConfig=TutorialStepConfig())={
    new TutorialStep(title,content,config)
  }
}
class TutorialStep(val title:String,val content:String,val config:TutorialStepConfig){
  def canAdvance(s:VonSimState)=true
}

abstract class Tutorial{
  def steps:List[TutorialStep]
  def title:String
  def id:String
  var step=0
  def hasNext=step<steps.length-1
  def canAdvance(s:VonSimState)={
    steps(step).canAdvance(s)
  }
  def hasPrevious=step>0
  def next{step+=1}
  def current=steps(step)
  
  
}
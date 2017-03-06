package vonemu.parsercombinators.parser

import scala.util.parsing.input.Positional

sealed trait WorkflowAST extends Positional
case class AndThen(step1: WorkflowAST, step2: WorkflowAST) extends WorkflowAST
case class ReadInput(inputs: Seq[String]) extends WorkflowAST
case class CallService(serviceName: String) extends WorkflowAST
case class Choice(alternatives: Seq[ConditionThen]) extends WorkflowAST
case object Exit extends WorkflowAST

sealed trait ConditionThen extends Positional { def thenBlock: WorkflowAST }
case class IfThen(predicate: Condition, thenBlock: WorkflowAST) extends ConditionThen
case class OtherwiseThen(thenBlock: WorkflowAST) extends ConditionThen

sealed trait Condition extends Positional
case class Equals(factName: String, factValue: String) extends Condition

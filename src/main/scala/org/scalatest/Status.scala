package org.scalatest
import org.scalatest.tools.SuiteRunner

sealed trait Status {
  def isCompleted: Boolean
  def isSucceeded: Boolean
  def waitUntilCompleted() {
    while (!isCompleted) 
      Thread.sleep(10)
  }
}

final class SimpleStatus(complete: Boolean = false, succeed: Boolean = false) extends Status {
  private var completed = complete
  private var succeeded = succeed
  
  def isCompleted = completed
  def isSucceeded = succeeded
  
  def complete() {
    completed = true
  }
  
  def succeed() {
    succeeded = true
  }
}

final class CompositeStatus(statusSeq: IndexedSeq[Status]) extends Status {
  def isCompleted: Boolean = statusSeq.forall(_.isCompleted)
  def isSucceeded: Boolean = statusSeq.forall(_.isSucceeded)
}
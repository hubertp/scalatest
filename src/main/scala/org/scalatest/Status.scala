package org.scalatest
import org.scalatest.tools.SuiteRunner
import java.util.concurrent.CountDownLatch

trait Status {
  def succeeds(): Boolean
}

final class SucceededStatus extends Status {
  def succeeds() = true
}

final class FailedStatus extends Status {
  def succeeds() = false
}

final class StatefulStatus extends Status {
  @volatile private var latch = new CountDownLatch(1)
  @volatile private var succeed = true
  
  def succeeds() = {
    latch.await()
    succeed
  }
  
  private[scalatest] def fails() {
    succeed = false
  }
  
  private[scalatest] def completes() {
    latch.countDown()
  }
}

final class CompositeStatus(statusSeq: IndexedSeq[Status]) extends Status {
  def succeeds() = statusSeq.forall(_.succeeds())
}
package org.scalatest

import org.scalatest.prop.Tables
import scala.collection.mutable.ListBuffer
import org.scalatest.events.Event
import org.scalatest.prop.TableDrivenPropertyChecks

class ParallelTestExecutionProp extends FunSuite 
  with TableDrivenPropertyChecks with SharedHelpers  
  with ParallelTestExecutionOrderExamples 
  with ParallelTestExecutionInfoExamples {
  
  class ControlledOrderDistributor extends Distributor {
    val buf = ListBuffer.empty[(Suite, Args)]
    def apply(suite: Suite, args: Args) {
      buf += ((suite, args))
    }
    def executeInOrder() {
      for ((suite, args) <- buf) {
        suite.run(None, args)
      }
    }
    def executeInReverseOrder() {
      for ((suite, args) <- buf.reverse) {
        suite.run(None, args)
      }
    }

    def apply(suite: Suite, tracker: Tracker) {
      throw new UnsupportedOperationException("Hey, we're not supposed to be calling this anymore!")
    }
  }
  
  def withDistributor(suite:Suite, fun: ControlledOrderDistributor => Unit) = {

    val recordingReporter = new EventRecordingReporter
    val outOfOrderDistributor = new ControlledOrderDistributor
    suite.run(None, Args(recordingReporter, distributor = Some(outOfOrderDistributor)))
    fun(outOfOrderDistributor)

    recordingReporter.eventsReceived
  }
  
  test("ParallelTestExecution should have the events reported in correct order when tests are executed in parallel") {
    forAll(orderExamples) { example =>
      val inOrderEvents = withDistributor(example, _.executeInOrder)
      example.assertOrderTest(inOrderEvents)
      val reverseOrderEvents = withDistributor(example, _.executeInReverseOrder)
      example.assertOrderTest(reverseOrderEvents)
    }
  }
  
  test("ParallelTestExecution should have InfoProvided fired from before and after block in correct order when tests are executed in parallel") {
    forAll(infoExamples) { example =>
      val inOrderEvents = withDistributor(example, _.executeInOrder)
      example.assertBeforeAfterInfo(inOrderEvents)
      val reverseOrderEvents = withDistributor(example, _.executeInReverseOrder)
      example.assertBeforeAfterInfo(reverseOrderEvents)
    }
  }
  
  test("ParallelTestExecution should have the blocking test's events fired without waiting when timeout reaches, and when the missing event finally reach later, it should just get fired") {
    
  }
}
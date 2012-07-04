package org.scalatest

import org.scalatest.prop.Tables
import scala.collection.mutable.ListBuffer
import org.scalatest.events.Event
import org.scalatest.prop.TableDrivenPropertyChecks
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Future
import org.scalatest.tools.SuiteRunner
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

class ParallelTestExecutionProp extends FunSuite 
  with TableDrivenPropertyChecks with SharedHelpers  
  with ParallelTestExecutionOrderExamples 
  with ParallelTestExecutionInfoExamples 
  with ParallelTestExecutionTestTimeoutExamples {
  
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
  
  class ControlledOrderConcurrentDistributor(poolSize: Int) extends Distributor {
      private val futureQueue = new LinkedBlockingQueue[Future[T] forSome { type T }]
      
      val buf = ListBuffer.empty[SuiteRunner]
      val execSvc: ExecutorService = Executors.newFixedThreadPool(2)
      def apply(suite: Suite, args: Args) {
        buf += new SuiteRunner(suite, args)
      }
      def executeInOrder() {
        for (suiteRunner <- buf) {
          val future: Future[_] = execSvc.submit(suiteRunner)
          futureQueue.put(future)
        }
        while (futureQueue.peek != null) 
          futureQueue.poll().get()
      }
      def executeInReverseOrder() {
        for (suiteRunner <- buf.reverse) {
          val future: Future[_] = execSvc.submit(suiteRunner)
          futureQueue.put(future)
        }
        while (futureQueue.peek != null)
          futureQueue.poll().get()
      }

      def apply(suite: Suite, tracker: Tracker) {
        throw new UnsupportedOperationException("Hey, we're not supposed to be calling this anymore!")
      }
    }
  
  def withDistributor(suite: Suite, fun: ControlledOrderDistributor => Unit) = {
    val recordingReporter = new EventRecordingReporter
    val outOfOrderDistributor = new ControlledOrderDistributor
    suite.run(None, Args(recordingReporter, distributor = Some(outOfOrderDistributor)))
    fun(outOfOrderDistributor)

    recordingReporter.eventsReceived
  }
  
  def withConcurrentDistributor(suite: Suite, fun: ControlledOrderConcurrentDistributor => Unit) = {
    val recordingReporter = new EventRecordingReporter
    val args = Args(recordingReporter)
    val outOfOrderConcurrentDistributor = new ControlledOrderConcurrentDistributor(2)
    suite.run(None, Args(recordingReporter, distributor = Some(outOfOrderConcurrentDistributor)))
    fun(outOfOrderConcurrentDistributor)

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
    forAll(testTimeoutExamples) { example => 
      val inOrderEvents = withConcurrentDistributor(example, _.executeInOrder)
      example.assertTestTimeoutTest(inOrderEvents)
      val reverseOrderEvents = withConcurrentDistributor(example, _.executeInReverseOrder)
      example.assertTestTimeoutTest(reverseOrderEvents)
    }
  }
}
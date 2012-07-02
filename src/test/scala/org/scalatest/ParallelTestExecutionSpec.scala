package org.scalatest

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.SharedHelpers.EventRecordingReporter
import collection.mutable.ListBuffer
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import org.scalatest.tools.SuiteRunner
import org.scalatest.tools.SuiteSortingReporter
import org.scalatest.events.SuiteStarting
import org.scalatest.events.SuiteCompleted

class ParallelTestExecutionSpec extends FunSpec with ShouldMatchers with EventHelpers {
  /*
  Need 3 tests at least
  1. should have the events reported in correct order when tests are executed in parallel
     For that one, pass in a Distributor that runs with just one thread and orders things
     in a predefined, out of order order.

  2. DistributedSuiteSorter should wait for completedTests instead of moving on when it
     gets a SuiteCompleted.

  3. Both of these should time out. So we need a test for each that shows the timeout
     happened. I.e., it will move on when waiting for something.
   */

  describe("ParallelTestExecution") {

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
      val buf = ListBuffer.empty[SuiteRunner]
      val execSvc: ExecutorService = Executors.newFixedThreadPool(2)
      def apply(suite: Suite, args: Args) {
        buf += new SuiteRunner(suite, args)
      }
      def executeInOrder() {
        for (suiteRunner <- buf) {
          execSvc.submit(suiteRunner)
        }
      }
      def executeInReverseOrder() {
        for (suiteRunner <- buf.reverse) {
          execSvc.submit(suiteRunner)
        }
      }

      def apply(suite: Suite, tracker: Tracker) {
        throw new UnsupportedOperationException("Hey, we're not supposed to be calling this anymore!")
      }
    }

    it("should have the events reported in correct order when tests are executed in parallel") {

      def withDistributor(fun: ControlledOrderDistributor => Unit) {

        val recordingReporter = new EventRecordingReporter
        val outOfOrderDistributor = new ControlledOrderDistributor
        (new ExampleParallelSpec).run(None, Args(recordingReporter, distributor = Some(outOfOrderDistributor)))
        fun(outOfOrderDistributor)

        val eventRecorded = recordingReporter.eventsReceived

        checkScopeOpened(eventRecorded(0), "Subject 1")
        checkTestStarting(eventRecorded(1), "Subject 1 should have behavior 1a")
        checkTestSucceeded(eventRecorded(2), "Subject 1 should have behavior 1a")
        checkTestStarting(eventRecorded(3), "Subject 1 should have behavior 1b")
        checkTestSucceeded(eventRecorded(4), "Subject 1 should have behavior 1b")
        checkTestStarting(eventRecorded(5), "Subject 1 should have behavior 1c")
        checkTestSucceeded(eventRecorded(6), "Subject 1 should have behavior 1c")
        checkScopeClosed(eventRecorded(7), "Subject 1")

        checkScopeOpened(eventRecorded(8), "Subject 2")
        checkTestStarting(eventRecorded(9), "Subject 2 should have behavior 2a")
        checkTestSucceeded(eventRecorded(10), "Subject 2 should have behavior 2a")
        checkTestStarting(eventRecorded(11), "Subject 2 should have behavior 2b")
        checkTestSucceeded(eventRecorded(12), "Subject 2 should have behavior 2b")
        checkTestStarting(eventRecorded(13), "Subject 2 should have behavior 2c")
        checkTestSucceeded(eventRecorded(14), "Subject 2 should have behavior 2c")
        checkScopeClosed(eventRecorded(15), "Subject 2")
      }
      withDistributor(_.executeInOrder())
      withDistributor(_.executeInReverseOrder())
    }
    
    it("should have InfoProvided fired from before and after block in correct order when tests are executed in parallel") {
      
      def withDistributor(fun: ControlledOrderDistributor => Unit) {

        val recordingReporter = new EventRecordingReporter
        val outOfOrderDistributor = new ControlledOrderDistributor
        (new ExampleBeforeAfterParallelSpec).run(None, Args(recordingReporter, distributor = Some(outOfOrderDistributor)))
        fun(outOfOrderDistributor)

        val eventRecorded = recordingReporter.eventsReceived
        assert(eventRecorded.size === 28)

        checkScopeOpened(eventRecorded(0), "Thing 1")
        checkInfoProvided(eventRecorded(1), "In Before")
        checkTestStarting(eventRecorded(2), "Thing 1 do thing 1a")
        checkTestSucceeded(eventRecorded(3), "Thing 1 do thing 1a")
        checkInfoProvided(eventRecorded(4), "In After")
        checkInfoProvided(eventRecorded(5), "In Before")
        checkTestStarting(eventRecorded(6), "Thing 1 do thing 1b")
        checkTestSucceeded(eventRecorded(7), "Thing 1 do thing 1b")
        checkInfoProvided(eventRecorded(8), "In After")
        checkInfoProvided(eventRecorded(9), "In Before")
        checkTestStarting(eventRecorded(10), "Thing 1 do thing 1c")
        checkTestSucceeded(eventRecorded(11), "Thing 1 do thing 1c")
        checkInfoProvided(eventRecorded(12), "In After")
        checkScopeClosed(eventRecorded(13), "Thing 1")
        
        checkScopeOpened(eventRecorded(14), "Thing 2")
        checkInfoProvided(eventRecorded(15), "In Before")
        checkTestStarting(eventRecorded(16), "Thing 2 do thing 2a")
        checkTestSucceeded(eventRecorded(17), "Thing 2 do thing 2a")
        checkInfoProvided(eventRecorded(18), "In After")
        checkInfoProvided(eventRecorded(19), "In Before")
        checkTestStarting(eventRecorded(20), "Thing 2 do thing 2b")
        checkTestSucceeded(eventRecorded(21), "Thing 2 do thing 2b")
        checkInfoProvided(eventRecorded(22), "In After")
        checkInfoProvided(eventRecorded(23), "In Before")
        checkTestStarting(eventRecorded(24), "Thing 2 do thing 2c")
        checkTestSucceeded(eventRecorded(25), "Thing 2 do thing 2c")
        checkInfoProvided(eventRecorded(26), "In After")
        checkScopeClosed(eventRecorded(27), "Thing 2")
      }
      withDistributor(_.executeInOrder())
      withDistributor(_.executeInReverseOrder())
    }
    
    it("should have the blocking event fired without waiting when timeout reaches, and when the missing event finally reach later, it should just get fired") {
      def withDistributor(fun: ControlledOrderConcurrentDistributor => Unit) {
        val recordingReporter = new EventRecordingReporter
        val args = Args(recordingReporter)
        val outOfOrderConcurrentDistributor = new ControlledOrderConcurrentDistributor(2)
        (new ExampleTimeoutParallelSpec).run(None, Args(recordingReporter, distributor = Some(outOfOrderConcurrentDistributor)))
        fun(outOfOrderConcurrentDistributor)
        Thread.sleep(3000)  // Get enough time for the timeout to reach, and the missing event to fire.

        val eventRecorded = recordingReporter.eventsReceived
        assert(eventRecorded.size === 16)

        checkScopeOpened(eventRecorded(0), "Thing 1")
        checkTestStarting(eventRecorded(1), "Thing 1 do thing 1a")
        checkTestSucceeded(eventRecorded(2), "Thing 1 do thing 1a")
        checkTestStarting(eventRecorded(3), "Thing 1 do thing 1b")        
        checkTestStarting(eventRecorded(4), "Thing 1 do thing 1c")
        checkTestSucceeded(eventRecorded(5), "Thing 1 do thing 1c")
        checkScopeClosed(eventRecorded(6), "Thing 1")
        
        checkScopeOpened(eventRecorded(7), "Thing 2")
        checkTestStarting(eventRecorded(8), "Thing 2 do thing 2a")
        checkTestSucceeded(eventRecorded(9), "Thing 2 do thing 2a")
        checkTestStarting(eventRecorded(10), "Thing 2 do thing 2b")
        checkTestSucceeded(eventRecorded(11), "Thing 2 do thing 2b")
        checkTestStarting(eventRecorded(12), "Thing 2 do thing 2c")
        checkTestSucceeded(eventRecorded(13), "Thing 2 do thing 2c")
        checkScopeClosed(eventRecorded(14), "Thing 2")
        
        // Now the missing one.
        checkTestSucceeded(eventRecorded(15), "Thing 1 do thing 1b")
      }

      withDistributor(_.executeInOrder())
      withDistributor(_.executeInReverseOrder())
    }

    // TODO: Check with Chee Seng. I'm not sure what this is supposed to be testing, and it fails.
    ignore("should have the events reported in correct order when multiple suite's tests are executed in parallel") {
      def withDistributor(fun: ControlledOrderConcurrentDistributor => Unit) = {
        val recordingReporter = new EventRecordingReporter
        val outOfOrderConcurrentDistributor = new ControlledOrderConcurrentDistributor(2)
        val suiteSortingReporter = new SuiteSortingReporter(recordingReporter)
        val spec1 = new ExampleParallelSpec()
        val spec2 = new ExampleBeforeAfterParallelSpec()
        
        val tracker = new Tracker()
        suiteSortingReporter(SuiteStarting(tracker.nextOrdinal, spec1.suiteName, spec1.suiteId, Some(spec1.getClass.getName), None))
        suiteSortingReporter(SuiteStarting(tracker.nextOrdinal, spec2.suiteName, spec2.suiteId, Some(spec2.getClass.getName), None))
        
        spec1.run(None, Args(suiteSortingReporter, distributor = Some(outOfOrderConcurrentDistributor), distributedSuiteSorter = Some(suiteSortingReporter)))
        spec2.run(None, Args(suiteSortingReporter, distributor = Some(outOfOrderConcurrentDistributor), distributedSuiteSorter = Some(suiteSortingReporter)))
        
        suiteSortingReporter(SuiteCompleted(tracker.nextOrdinal, spec1.suiteName, spec1.suiteId, Some(spec1.getClass.getName), None))
        suiteSortingReporter(SuiteCompleted(tracker.nextOrdinal, spec2.suiteName, spec2.suiteId, Some(spec2.getClass.getName), None))
        
        fun(outOfOrderConcurrentDistributor)
        
        recordingReporter.eventsReceived
      }
      
      val spec1SuiteId = new ExampleParallelSpec().suiteId
      val spec2SuiteId = new ExampleBeforeAfterParallelSpec().suiteId
      
      val inOrderEvents = withDistributor(_.executeInOrder)
      
      assert(inOrderEvents.size === 48)
      
      checkSuiteStarting(inOrderEvents(0), spec1SuiteId)
      checkScopeOpened(inOrderEvents(1), "Subject 1")
      checkTestStarting(inOrderEvents(2), "Subject 1 should have behavior 1a")
      checkTestSucceeded(inOrderEvents(3), "Subject 1 should have behavior 1a")
      checkTestStarting(inOrderEvents(4), "Subject 1 should have behavior 1b")
      checkTestSucceeded(inOrderEvents(5), "Subject 1 should have behavior 1b")
      checkTestStarting(inOrderEvents(6), "Subject 1 should have behavior 1c")
      checkTestSucceeded(inOrderEvents(7), "Subject 1 should have behavior 1c")
      checkScopeClosed(inOrderEvents(8), "Subject 1")

      checkScopeOpened(inOrderEvents(9), "Subject 2")
      checkTestStarting(inOrderEvents(10), "Subject 2 should have behavior 2a")
      checkTestSucceeded(inOrderEvents(11), "Subject 2 should have behavior 2a")
      checkTestStarting(inOrderEvents(12), "Subject 2 should have behavior 2b")
      checkTestSucceeded(inOrderEvents(13), "Subject 2 should have behavior 2b")
      checkTestStarting(inOrderEvents(14), "Subject 2 should have behavior 2c")
      checkTestSucceeded(inOrderEvents(15), "Subject 2 should have behavior 2c")
      checkScopeClosed(inOrderEvents(16), "Subject 2")
      checkSuiteCompleted(inOrderEvents(17), spec1SuiteId)
      
      checkSuiteStarting(inOrderEvents(18), spec2SuiteId)
      checkScopeOpened(inOrderEvents(19), "Thing 1")
      checkInfoProvided(inOrderEvents(20), "In Before")
      checkTestStarting(inOrderEvents(21), "Thing 1 do thing 1a")
      checkTestSucceeded(inOrderEvents(22), "Thing 1 do thing 1a")
      checkInfoProvided(inOrderEvents(23), "In After")
      checkInfoProvided(inOrderEvents(24), "In Before")
      checkTestStarting(inOrderEvents(25), "Thing 1 do thing 1b")
      checkTestSucceeded(inOrderEvents(26), "Thing 1 do thing 1b")
      checkInfoProvided(inOrderEvents(27), "In After")
      checkInfoProvided(inOrderEvents(28), "In Before")
      checkTestStarting(inOrderEvents(29), "Thing 1 do thing 1c")
      checkTestSucceeded(inOrderEvents(30), "Thing 1 do thing 1c")
      checkInfoProvided(inOrderEvents(31), "In After")
      checkScopeClosed(inOrderEvents(32), "Thing 1")
        
      checkScopeOpened(inOrderEvents(33), "Thing 2")
      checkInfoProvided(inOrderEvents(34), "In Before")
      checkTestStarting(inOrderEvents(35), "Thing 2 do thing 2a")
      checkTestSucceeded(inOrderEvents(36), "Thing 2 do thing 2a")
      checkInfoProvided(inOrderEvents(37), "In After")
      checkInfoProvided(inOrderEvents(38), "In Before")
      checkTestStarting(inOrderEvents(39), "Thing 2 do thing 2b")
      checkTestSucceeded(inOrderEvents(40), "Thing 2 do thing 2b")
      checkInfoProvided(inOrderEvents(41), "In After")
      checkInfoProvided(inOrderEvents(42), "In Before")
      checkTestStarting(inOrderEvents(43), "Thing 2 do thing 2c")
      checkTestSucceeded(inOrderEvents(44), "Thing 2 do thing 2c")
      checkInfoProvided(inOrderEvents(45), "In After")
      checkScopeClosed(inOrderEvents(46), "Thing 2")
      checkSuiteCompleted(inOrderEvents(47), spec2SuiteId)
      
      val reverseOrderEvents = withDistributor(_.executeInReverseOrder)
      
      assert(reverseOrderEvents.size === 48)
      
      checkSuiteStarting(reverseOrderEvents(0), spec1SuiteId)
      checkScopeOpened(reverseOrderEvents(1), "Subject 1")
      checkTestStarting(reverseOrderEvents(2), "Subject 1 should have behavior 1a")
      checkTestSucceeded(reverseOrderEvents(3), "Subject 1 should have behavior 1a")
      checkTestStarting(reverseOrderEvents(4), "Subject 1 should have behavior 1b")
      checkTestSucceeded(reverseOrderEvents(5), "Subject 1 should have behavior 1b")
      checkTestStarting(reverseOrderEvents(6), "Subject 1 should have behavior 1c")
      checkTestSucceeded(reverseOrderEvents(7), "Subject 1 should have behavior 1c")
      checkScopeClosed(reverseOrderEvents(8), "Subject 1")

      checkScopeOpened(reverseOrderEvents(9), "Subject 2")
      checkTestStarting(reverseOrderEvents(10), "Subject 2 should have behavior 2a")
      checkTestSucceeded(reverseOrderEvents(11), "Subject 2 should have behavior 2a")
      checkTestStarting(reverseOrderEvents(12), "Subject 2 should have behavior 2b")
      checkTestSucceeded(reverseOrderEvents(13), "Subject 2 should have behavior 2b")
      checkTestStarting(reverseOrderEvents(14), "Subject 2 should have behavior 2c")
      checkTestSucceeded(reverseOrderEvents(15), "Subject 2 should have behavior 2c")
      checkScopeClosed(reverseOrderEvents(16), "Subject 2")
      checkSuiteCompleted(reverseOrderEvents(17), spec1SuiteId)
      
      checkSuiteStarting(reverseOrderEvents(18), spec2SuiteId)
      checkScopeOpened(reverseOrderEvents(19), "Thing 1")
      checkInfoProvided(reverseOrderEvents(20), "In Before")
      checkTestStarting(reverseOrderEvents(21), "Thing 1 do thing 1a")
      checkTestSucceeded(reverseOrderEvents(22), "Thing 1 do thing 1a")
      checkInfoProvided(reverseOrderEvents(23), "In After")
      checkInfoProvided(reverseOrderEvents(24), "In Before")
      checkTestStarting(reverseOrderEvents(25), "Thing 1 do thing 1b")
      checkTestSucceeded(reverseOrderEvents(26), "Thing 1 do thing 1b")
      checkInfoProvided(reverseOrderEvents(27), "In After")
      checkInfoProvided(reverseOrderEvents(28), "In Before")
      checkTestStarting(reverseOrderEvents(29), "Thing 1 do thing 1c")
      checkTestSucceeded(reverseOrderEvents(30), "Thing 1 do thing 1c")
      checkInfoProvided(reverseOrderEvents(31), "In After")
      checkScopeClosed(reverseOrderEvents(32), "Thing 1")
        
      checkScopeOpened(reverseOrderEvents(33), "Thing 2")
      checkInfoProvided(reverseOrderEvents(34), "In Before")
      checkTestStarting(reverseOrderEvents(35), "Thing 2 do thing 2a")
      checkTestSucceeded(reverseOrderEvents(36), "Thing 2 do thing 2a")
      checkInfoProvided(reverseOrderEvents(37), "In After")
      checkInfoProvided(reverseOrderEvents(38), "In Before")
      checkTestStarting(reverseOrderEvents(39), "Thing 2 do thing 2b")
      checkTestSucceeded(reverseOrderEvents(40), "Thing 2 do thing 2b")
      checkInfoProvided(reverseOrderEvents(41), "In After")
      checkInfoProvided(reverseOrderEvents(42), "In Before")
      checkTestStarting(reverseOrderEvents(43), "Thing 2 do thing 2c")
      checkTestSucceeded(reverseOrderEvents(44), "Thing 2 do thing 2c")
      checkInfoProvided(reverseOrderEvents(45), "In After")
      checkScopeClosed(reverseOrderEvents(46), "Thing 2")
      checkSuiteCompleted(reverseOrderEvents(47), spec2SuiteId)
    }
  }
}

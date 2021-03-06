/*
 * Copyright 2001-2008 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest

import org.scalatest.events._
import java.util.concurrent.Executors
import java.io.File
import scala.annotation.tailrec

trait SharedHelpers extends Assertions {

  object SilentReporter extends Reporter {
    def apply(event: Event) = ()  
  }

  class TestDurationReporter extends Reporter {
    var testSucceededWasFiredAndHadADuration = false
    var testFailedWasFiredAndHadADuration = false
    override def apply(event: Event) {
      event match {
        case event: TestSucceeded => testSucceededWasFiredAndHadADuration = event.duration.isDefined
        case event: TestFailed => testFailedWasFiredAndHadADuration = event.duration.isDefined
        case _ =>
      }
    }
  }

  class SuiteDurationReporter extends Reporter {
    var suiteCompletedWasFiredAndHadADuration = false
    var suiteAbortedWasFiredAndHadADuration = false
    override def apply(event: Event) {
      event match {
        case event: SuiteCompleted => suiteCompletedWasFiredAndHadADuration = event.duration.isDefined
        case event: SuiteAborted => suiteAbortedWasFiredAndHadADuration = event.duration.isDefined
        case _ =>
      }
    }
  }

  class PendingReporter extends Reporter {
    var testPendingWasFired = false
    override def apply(event: Event) {
      event match {
        case _: TestPending => testPendingWasFired = true
        case _ =>
      }
    }
  }

  class EventRecordingReporter extends Reporter {
    private var eventList: List[Event] = List()
    def eventsReceived = eventList.reverse
    def testSucceededEventsReceived: List[TestSucceeded] = {
      eventsReceived filter {
        case event: TestSucceeded => true
        case _ => false
      } map {
        case event: TestSucceeded => event
        case _ => throw new RuntimeException("should never happen")
      }
    }
    def testStartingEventsReceived: List[TestStarting] = {
      eventsReceived filter {
        case event: TestStarting => true
        case _ => false
      } map {
        case event: TestStarting => event
        case _ => throw new RuntimeException("should never happen")
      }
    }
    // Why doesn't this work:
    // for (event: TestSucceeded <- eventsReceived) yield event
    def infoProvidedEventsReceived: List[InfoProvided] = {
      eventsReceived filter {
        case event: InfoProvided => true
        case _ => false
      } map {
        case event: InfoProvided => event
        case _ => throw new RuntimeException("should never happen")
      }
    }
    def markupProvidedEventsReceived: List[MarkupProvided] = {
      eventsReceived filter {
        case event: MarkupProvided => true
        case _ => false
      } map {
        case event: MarkupProvided => event
        case _ => throw new RuntimeException("should never happen")
      }
    }
    def scopeOpenedEventsReceived: List[ScopeOpened] = {
      eventsReceived filter {
        case event: ScopeOpened => true
        case _ => false
      } map {
        case event: ScopeOpened => event
        case _ => throw new RuntimeException("should never happen")
      }
    }
    def scopeClosedEventsReceived: List[ScopeClosed] = {
      eventsReceived filter {
        case event: ScopeClosed => true
        case _ => false
      } map {
        case event: ScopeClosed => event
        case _ => throw new RuntimeException("should never happen")
      }
    }
    def testPendingEventsReceived: List[TestPending] = {
      eventsReceived filter {
        case event: TestPending => true
        case _ => false
      } map {
        case event: TestPending => event
        case _ => throw new RuntimeException("should never happen")
      }
    }
    def testCanceledEventsReceived: List[TestCanceled] = {
      eventsReceived filter {
        case event: TestCanceled => true
        case _ => false
      } map {
        case event: TestCanceled => event
        case _ => throw new RuntimeException("should never happen")
      }
    }
    def testFailedEventsReceived: List[TestFailed] = {
      eventsReceived filter {
        case event: TestFailed => true
        case _ => false
      } map {
        case event: TestFailed => event
        case _ => throw new RuntimeException("should never happen")
      }
    }
    def testIgnoredEventsReceived: List[TestIgnored] = {
      eventsReceived filter {
        case event: TestIgnored => true
        case _ => false
      } map {
        case event: TestIgnored => event
        case _ => throw new RuntimeException("should never happen")
      }
    }
    def suiteStartingEventsReceived: List[SuiteStarting] = {
      eventsReceived filter {
        case event: SuiteStarting => true
        case _ => false
      } map {
        case event: SuiteStarting => event
        case _ => throw new RuntimeException("should never happen")
      }
    }
    def apply(event: Event) {
      eventList ::= event
    }
  }
  
  def getIndexesForTestInformerEventOrderTests(suite: Suite, testName: String, infoMsg: String): (Int, Int) = {
    val myRep = new EventRecordingReporter
    suite.run(None, Args(myRep))

    val indexedList = myRep.eventsReceived.zipWithIndex

    val testStartingOption = indexedList.find(_._1.isInstanceOf[TestStarting])
    val testSucceededOption = indexedList.find(_._1.isInstanceOf[TestSucceeded])
    
    assert(testStartingOption.isDefined, "TestStarting for Suite='" + suite.suiteId + "', testName='" + testName + "' not defined.")
    assert(testSucceededOption.isDefined, "TestSucceeded for Suite='" + suite.suiteId + "', testName='" + testName + "' not defined.")
    
    val testStartingIndex = testStartingOption.get._2
    val testSucceededIndex = testSucceededOption.get._2
    
    val testStarting = testStartingOption.get._1.asInstanceOf[TestStarting]
    val testSucceeded = testSucceededOption.get._1.asInstanceOf[TestSucceeded]
    
    val recordedEvents = testSucceeded.recordedEvents
    
    val infoProvidedOption = recordedEvents.find {
      case event: InfoProvided => event.message == infoMsg
      case _ => false
    }
    assert(infoProvidedOption.isDefined, "InfoProvided for Suite='" + suite.suiteId + "', testName='" + testName + "' not defined.")
    
    (testStartingIndex, testSucceededIndex)
  }

  def getIndexesForInformerEventOrderTests(suite: Suite, testName: String, infoMsg: String): (Int, Int, Int) = {

    val myRep = new EventRecordingReporter
    suite.run(None, Args(myRep))

    val indexedList = myRep.eventsReceived.zipWithIndex

    val testStartingOption = indexedList.find(_._1.isInstanceOf[TestStarting])
    val infoProvidedOption = indexedList.find {
      case (event: InfoProvided, index) => event.message == infoMsg
      case _ => false
    }
    val testSucceededOption = indexedList.find(_._1.isInstanceOf[TestSucceeded])

    assert(testStartingOption.isDefined, "TestStarting for Suite='" + suite.suiteId + "', testName='" + testName + "' not defined.")
    assert(infoProvidedOption.isDefined, "InfoProvided for Suite='" + suite.suiteId + "', testName='" + testName + "' not defined.")
    assert(testSucceededOption.isDefined, "TestSucceeded for Suite='" + suite.suiteId + "', testName='" + testName + "' not defined.")

    val testStartingIndex = testStartingOption.get._2
    val infoProvidedIndex = infoProvidedOption.get._2
    val testSucceededIndex = testSucceededOption.get._2

    val testStarting = testStartingOption.get._1.asInstanceOf[TestStarting]
    val infoProvided = infoProvidedOption.get._1.asInstanceOf[InfoProvided]
    val testSucceeded = testSucceededOption.get._1.asInstanceOf[TestSucceeded]

    assert(testStarting.testName === testName, "TestStarting.testName expected to be '" + testName + "', but got '" + testStarting.testName + "'.")
    assert(infoProvided.message === infoMsg, "InfoProvide.message expected to be '" + infoMsg + "', but got '" + infoProvided.message + "'.")
    assert(testSucceeded.testName === testName, "TestSucceeded.testName expected to be '" + testName + "', but got '" + testSucceeded.testName + "'.")

    (infoProvidedIndex, testStartingIndex, testSucceededIndex)
  }

  def getIndentedTextFromInfoProvided(suite: Suite): IndentedText = {

    val myRep = new EventRecordingReporter
    suite.run(None, Args(myRep))

    val infoProvidedOption = myRep.eventsReceived.find(_.isInstanceOf[InfoProvided])

    infoProvidedOption match {
      case Some(infoProvided: InfoProvided) =>
        infoProvided.formatter match {
          case Some(indentedText: IndentedText) => indentedText
          case _ => fail("An InfoProvided was received that didn't include an IndentedText formatter: " + infoProvided.formatter)
        }
      case _ => fail("No InfoProvided was received by the Reporter during the run.")
    }
  }
  
  def getIndentedTextFromTestInfoProvided(suite: Suite): IndentedText = {
    val myRep = new EventRecordingReporter
    suite.run(None, Args(myRep))
    val recordedEvents: Seq[Event] = myRep.eventsReceived.find { e => 
      e match {
        case testSucceeded: TestSucceeded => 
          true
        case testFailed: TestFailed => 
          true
        case testPending: TestPending => 
          true
        case testCanceled: TestCanceled =>
          true
        case _ => 
          false
      }
    } match {
      case Some(testCompleted) =>
        testCompleted match {
          case testSucceeded: TestSucceeded => 
            testSucceeded.recordedEvents
          case testFailed: TestFailed => 
            testFailed.recordedEvents
          case testPending: TestPending => 
            testPending.recordedEvents
          case testCanceled: TestCanceled =>
            testCanceled.recordedEvents
          case _ => throw new RuntimeException("should never get here")
        }
      case None => 
        fail("Test completed event is expected but not found.")
    }
    assert(recordedEvents.size === 1)
    recordedEvents(0) match {
      case ip: InfoProvided => 
        ip.formatter match {
          case Some(indentedText: IndentedText) => indentedText
          case _ => fail("An InfoProvided was received that didn't include an IndentedText formatter: " + ip.formatter)
        }
      case _ => fail("No InfoProvided was received by the Reporter during the run.")
    }
  }

  def ensureTestFailedEventReceived(suite: Suite, testName: String) {
    val reporter = new EventRecordingReporter
    suite.run(None, Args(reporter))
    val testFailedEvent = reporter.eventsReceived.find(_.isInstanceOf[TestFailed])
    assert(testFailedEvent.isDefined)
    assert(testFailedEvent.get.asInstanceOf[TestFailed].testName === testName)
  }
  
  def thisLineNumber = {
    val st = Thread.currentThread.getStackTrace

    if (!st(2).getMethodName.contains("thisLineNumber"))
      st(2).getLineNumber
    else
      st(3).getLineNumber
  }

  class TestIgnoredTrackingReporter extends Reporter {
    var testIgnoredReceived = false
    var lastEvent: Option[TestIgnored] = None
    def apply(event: Event) {
      event match {
        case event: TestIgnored =>
          testIgnoredReceived = true
          lastEvent = Some(event)
        case _ =>
      }
    }
  }
  
  class TestConcurrentDistributor(poolSize: Int) extends tools.ConcurrentDistributor(Args(reporter = SilentReporter), Executors.newFixedThreadPool(poolSize)) {
     override def apply(suite: Suite, tracker: Tracker) {
       throw new UnsupportedOperationException("Please use apply with args.")
     }
  }
  
  private val TEMP_DIR_ATTEMPTS = 10000
  
  // This is based on createTempDir here (Apache License): http://code.google.com/p/guava-libraries/source/browse/guava/src/com/google/common/io/Files.java
  // java.nio.file.Files#createTempDirectory() exists in Java 7 should be preferred when we no longer support Java 5/6.
  def createTempDirectory(): File = {
    val baseDir = new File(System.getProperty("java.io.tmpdir"))
    val baseName = System.currentTimeMillis + "-"
    
    @tailrec
    def tryCreateTempDirectory(counter: Int): Option[File] = {
      val tempDir = new File(baseDir, baseName + counter)
      if (tempDir.mkdir())
        Some(tempDir)
      else if (counter < TEMP_DIR_ATTEMPTS)
        tryCreateTempDirectory(counter + 1)
      else
        None
    }
    
    tryCreateTempDirectory(0) match {
      case Some(tempDir) => tempDir
      case None => 
        throw new IllegalStateException(
            "Failed to create directory within " + 
            TEMP_DIR_ATTEMPTS + " attempts (tried " + 
            baseName + "0 to " + baseName +
            (TEMP_DIR_ATTEMPTS - 1) + ')');
    }
  }
}

// Selfless trait pattern
object SharedHelpers extends SharedHelpers


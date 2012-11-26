package org.scalatest.tools

import org.scalatest._
import org.scalatest.events.{SuiteCompleted, SuiteAborted, Ordinal, TestStarting}

class HtmlReporterSpec extends Spec with SharedHelpers {

  object `HtmlReporter ` {
    
    def `should throw IllegalStateException when SuiteCompleted is received without any suite events` {
      val tempDir = createTempDirectory()
      val htmlRep = new HtmlReporter(tempDir.getAbsolutePath, false, false, false, false, None, None)
      val e = intercept[IllegalStateException] {
        htmlRep(SuiteCompleted(new Ordinal(99), "TestSuite", "TestSuite", Some("TestSuite")))
      }
      assert(e.getMessage === "Expected SuiteStarting in the head of suite events, but we got no suite event at all") 
    }
    
    def `should throw IllegalStateException when SuiteCompleted is received without a SuiteStarting` {
      val tempDir = createTempDirectory()
      val htmlRep = new HtmlReporter(tempDir.getAbsolutePath, false, false, false, false, None, None)
      htmlRep(TestStarting(new Ordinal(99), "TestSuite", "TestSuite", Some("TestSuite"), "A Test", "A Test"))
      val e = intercept[IllegalStateException] {
        htmlRep(SuiteCompleted(new Ordinal(99), "TestSuite", "TestSuite", Some("TestSuite")))
      }
      assert(e.getMessage === "Expected SuiteStarting in the head of suite events, but we got: org.scalatest.events.TestStarting") 
    }
    
    def `should throw IllegalStateException when SuiteAborted is received without any suite events` {
      val tempDir = createTempDirectory()
      val htmlRep = new HtmlReporter(tempDir.getAbsolutePath, false, false, false, false, None, None)
      val e = intercept[IllegalStateException] {
        htmlRep(SuiteAborted(new Ordinal(99), "Error", "TestSuite", "TestSuite", Some("TestSuite")))
      }
      assert(e.getMessage === "Expected SuiteStarting in the head of suite events, but we got no suite event at all") 
    }
    
    def `should throw IllegalStateException when SuiteAborted is received without a SuiteStarting` {
      val tempDir = createTempDirectory()
      val htmlRep = new HtmlReporter(tempDir.getAbsolutePath, false, false, false, false, None, None)
      htmlRep(TestStarting(new Ordinal(99), "TestSuite", "TestSuite", Some("TestSuite"), "A Test", "A Test"))
      val e = intercept[IllegalStateException] {
        htmlRep(SuiteAborted(new Ordinal(99), "Error", "TestSuite", "TestSuite", Some("TestSuite")))
      }
      assert(e.getMessage === "Expected SuiteStarting in the head of suite events, but we got: org.scalatest.events.TestStarting") 
    }
  }
  
}
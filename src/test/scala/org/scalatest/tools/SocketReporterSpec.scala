package org.scalatest.tools
import org.scalatest.FunSpec
import java.net.ServerSocket
import java.io.BufferedReader
import java.io.InputStreamReader
import scala.xml.XML
import scala.xml.Elem
import org.xml.sax.SAXException
import java.net.Socket
import org.scalatest.Stopper
import org.scalatest.Filter
import org.scalatest.Tracker
import scala.collection.mutable.ListBuffer
import org.scalatest.SharedHelpers

class SocketReporterSpec extends FunSpec with SharedHelpers {
  
  class SocketEventRecorder(socket: ServerSocket) extends Runnable {
    @volatile
    var stopped: Boolean = false
    private var connection: Socket = null
    private var in: BufferedReader = null
    @volatile
    var ready: Boolean = false
    
    var testStartingList = new ListBuffer[Elem]
    def testStartingEvents = testStartingList.toArray
    
    var testSucceededList = new ListBuffer[Elem]
    def testSucceededEvents = testSucceededList.toArray
    
    var testFailedList = new ListBuffer[Elem]
    def testFailedEvents = testFailedList.toArray
    
    var testIgnoredList = new ListBuffer[Elem]
    def testIgnoredEvents = testIgnoredList.toArray
    
    var testPendingList = new ListBuffer[Elem]
    def testPendingEvents = testPendingList.toArray
    
    var testCanceledList = new ListBuffer[Elem]
    def testCanceledEvents = testCanceledList.toArray
    
    var scopeOpenedList = new ListBuffer[Elem]
    def scopeOpenedEvents = scopeOpenedList.toArray
    
    var scopeClosedList = new ListBuffer[Elem]
    def scopeClosedEvents = scopeClosedList.toArray
    
    def run() {
      try {
        connection = socket.accept
        in = new BufferedReader(new InputStreamReader(connection.getInputStream))
        while (!stopped || in.ready) {
          var eventRawXml = ""
          var eventXml: Elem = null
          while (eventXml == null) {
            val line = in.readLine
            //println("#####line: " + line)
            if (line != null) {
              eventRawXml += line
              try {
                eventXml = XML.loadString(eventRawXml)
              }
              catch {
                case e: SAXException => 
                  Thread.sleep(10)
              }
            }
            else
              Thread.sleep(10)
          }
          eventXml.label match {
            case "TestStarting" => testStartingList += eventXml
            case "TestSucceeded" => testSucceededList += eventXml
            case "TestFailed" => testFailedList += eventXml
            case "TestIgnored" => testIgnoredList += eventXml
            case "TestPending" => testPendingList += eventXml
            case "TestCanceled" => testCanceledList += eventXml
            case "ScopeOpened" => scopeOpenedList += eventXml
            case "ScopeClosed" => scopeClosedList += eventXml
          }
          
          Thread.sleep(10)
        }
      }
      finally {
        in.close()
        connection.close()
        ready = true
      }
    }
  }
  
  def checkStringOption(value: String, expectedValueOpt: Option[String]) {
    expectedValueOpt match {
      case Some(expectedValue) => 
        assert(value === expectedValue)
      case None =>
        assert(value === "")
    }
  }
  
  def checkScopeEvents(scopeOpened: Elem, message: String, suiteName: String, suiteId: String, suiteClassName: Option[String], 
                     decodedSuiteName:Option[String], fileName: String, lineNumber: Int) {
    assert((scopeOpened \ "message").text === message )
    assert((scopeOpened \ "nameInfo" \ "suiteName").text === suiteName )
    assert((scopeOpened \ "nameInfo" \ "suiteId").text === suiteId)
    checkStringOption((scopeOpened \ "nameInfo" \ "suiteClassName").text, suiteClassName)
    checkStringOption((scopeOpened \ "decodedSuiteName" \ "decodedSuiteName").text, decodedSuiteName)
    assert((scopeOpened \ "location" \ "LineInFile" \ "fileName").text === fileName)
    assert((scopeOpened \ "location" \ "LineInFile" \ "lineNumber").text === lineNumber.toString)
  }
  
  def checkTestStarting(testStarting:Elem, suiteName: String, suiteId: String, suiteClassName: Option[String], decodedSuiteName: Option[String],
                        testName: String, testText: String, decodedTestName: Option[String], fileName: String, lineNumber: Int, 
                        rerunner: Option[String]) {
    assert((testStarting \ "suiteName").text === suiteName)
    assert((testStarting \ "suiteId").text === suiteId)
    checkStringOption((testStarting \ "suiteClassName").text, suiteClassName)
    checkStringOption((testStarting \ "decodedSuiteName").text, decodedSuiteName)
    assert((testStarting \ "testName").text === testName)
    assert((testStarting \ "testText").text === testText)
    checkStringOption((testStarting \ "decodedTestName").text, decodedTestName)
    assert((testStarting \ "location" \ "LineInFile" \ "fileName").text === fileName)
    assert((testStarting \ "location" \ "LineInFile" \ "lineNumber").text === lineNumber.toString)
    // TODO: Fix this.
    // checkStringOption((testStarting \ "rerunner").text, rerunner)
  }

  describe("Socket Reporter") {
    
    it("should send test TestStarting, TestSucceeded, TestFailed, TestIgnored, TestPending, " +
       "TestCanceled, ScopeOpened and ScopeClosed event using socket.") {
      
      class TestSpec extends FunSpec {
        describe("A Feature") {
          it("should succeed") {}
          it("should failed") { fail }
          ignore("should ignored") {}
          it("should pending") { pending }
          it("should canceled") { cancel }
        }
      }
      
      val socket = new ServerSocket(0)
      val eventRecorder = new SocketEventRecorder(socket)
      val eventRecorderThread = new Thread(eventRecorder)
      eventRecorderThread.start()
      val spec = new TestSpec()
      spec.run(None, new SocketReporter("localhost", socket.getLocalPort), new Stopper {}, Filter(), Map(), None, new Tracker())
      eventRecorder.stopped = true
      while (!eventRecorder.ready) // Wait until the receiver is ready
        Thread.sleep(100)
        
      assert(eventRecorder.scopeOpenedEvents.length === 1)
      checkScopeEvents(eventRecorder.scopeOpenedEvents(0), "A Feature", spec.suiteName, spec.suiteId, 
                     Some(spec.getClass.getName), None, "SocketReporterSpec.scala", thisLineNumber - 21)
      assert(eventRecorder.scopeClosedEvents.length === 1)
      checkScopeEvents(eventRecorder.scopeClosedEvents(0), "A Feature", spec.suiteName, spec.suiteId, 
                     Some(spec.getClass.getName), None, "SocketReporterSpec.scala", thisLineNumber - 24)
                     
      assert(eventRecorder.testStartingEvents.length === 4)
      checkTestStarting(eventRecorder.testStartingEvents(0), spec.suiteName, spec.suiteId, Some(spec.getClass.getName), None,
                        "A Feature should succeed", "should succeed", None, "SocketReporterSpec.scala", thisLineNumber - 27, 
                        Some(spec.getClass.getName))
    }
  }
  
}
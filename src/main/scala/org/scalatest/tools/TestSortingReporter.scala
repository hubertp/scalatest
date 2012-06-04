package org.scalatest.tools

import org.scalatest._
import org.scalatest.events._
import DispatchReporter.propagateDispose
import scala.collection.mutable.ListBuffer

class TestSortingReporter(dispatch: Reporter) extends ResourcefulReporter {

  case class Slot(startEvent: Option[Event], completedEvent: Option[Event], ready: Boolean)
  
  private val waitingBuffer = new ListBuffer[Slot]()
  private val slotMap = new collection.mutable.HashMap[String, Slot]()  // testName -> Slot
  
  def waitForTestCompleted(testName: String) {
    synchronized {
      slotMap.put(testName, Slot(None, None, false))
    }
  }
  
  override def apply(event: Event) {
    synchronized {
      event match {
        case testStarting: TestStarting => 
          slotMap.get(testStarting.testName) match {
            case Some(slot) => 
              val newSlot = slot.copy(startEvent = Some(testStarting))
              waitingBuffer += newSlot
              slotMap.put(testStarting.testName, newSlot)
            case None => 
              dispatch(testStarting)
          }
        case testIgnored: TestIgnored => 
          slotMap.get(testIgnored.testName) match {
            case Some(slot) => 
              val newSlot = slot.copy(startEvent = Some(testIgnored), ready = true)
              waitingBuffer += newSlot
              slotMap.put(testIgnored.testName, newSlot)
            case None => 
              dispatch(testIgnored)
          }
        case testSucceeded: TestSucceeded => 
          handleTestCompleted(testSucceeded, testSucceeded.testName)
        case testFailed: TestFailed => 
          handleTestCompleted(testFailed, testFailed.testName)
        case testPending: TestPending => 
          handleTestCompleted(testPending, testPending.testName)
        case testCanceled: TestCanceled => 
          handleTestCompleted(testCanceled, testCanceled.testName)
        case _ => 
          dispatch(event)
      }
      fireReadyEvents()
    }
  }
  
  private def handleTestCompleted(event: Event, testName: String) {
    slotMap.get(testName) match {
      case Some(slot) => 
        val newSlot = slot.copy(completedEvent = Some(event), ready = true)
        waitingBuffer.update(waitingBuffer.indexOf(slot), newSlot)
        slotMap.put(testName, newSlot)
      case None => 
        dispatch(event)
    }
  }
  
  private def fireReadyEvents() {
    val (ready, pending) = waitingBuffer.span(slot => slot.ready)
    ready.foreach { slot => 
      dispatch(slot.startEvent.get)
      slot.completedEvent match {
        case Some(completedEvent) => 
          dispatch(completedEvent)
          completedEvent match {
            case testSucceeded: TestSucceeded => 
              testSucceeded.testEvents.foreach(dispatch(_))
            case testFailed: TestFailed => 
              testFailed.testEvents.foreach(dispatch(_))
            case testPending: TestPending => 
              testPending.testEvents.foreach(dispatch(_))
            case testCanceled: TestCanceled => 
              testCanceled.testEvents.foreach(dispatch(_))
          }
        case None =>
      }
    }
    waitingBuffer.clear()
    waitingBuffer ++= pending
  }
  
  override def dispose() = propagateDispose(dispatch)
}
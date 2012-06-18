package org.scalatest.tools

import org.scalatest._
import org.scalatest.events._
import DispatchReporter.propagateDispose
import scala.collection.mutable.ListBuffer
import org.scalatest.time.Span
import java.util.Timer
import java.util.TimerTask

private[scalatest] class TestSortingReporter(dispatch: Reporter, timeout: Span) extends ResourcefulReporter {

  case class Slot(startEvent: Option[Event], completedEvent: Option[Event], ready: Boolean)
  
  private val waitingBuffer = new ListBuffer[Slot]()
  private val slotMap = new collection.mutable.HashMap[String, Slot]()  // testName -> Slot
  
  class TimeoutTask(val event: Event) extends TimerTask {
    override def run() {
      timeout()
    }
  }
  
  private val timer = new Timer()
  private var timeoutTask: Option[TimeoutTask] = None
  
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
        case scopeOpened: ScopeOpened =>
          handleSuiteEvent(scopeOpened)
        case scopeClosed: ScopeClosed =>
          handleSuiteEvent(scopeClosed)
        case infoProvided: InfoProvided =>
          handleSuiteEvent(infoProvided)
        case markupProvided: MarkupProvided =>
          handleSuiteEvent(markupProvided)
        case _ => 
          dispatch(event)
      }
      fireReadyEvents()
    }
  }
  
  private def handleSuiteEvent(event: Event) {
    val slot = Slot(Some(event), None, true)
    waitingBuffer += slot
  }
  
  private def handleTestCompleted(event: Event, testName: String) {
    slotMap.get(testName) match {
      case Some(slot) =>
        val slotIdx = waitingBuffer.indexOf(slot)
        if (slotIdx >= 0) {
          val newSlot = slot.copy(completedEvent = Some(event), ready = true)
          waitingBuffer.update(waitingBuffer.indexOf(slot), newSlot)
          slotMap.put(testName, newSlot)
        }
        else // could happen when timeout, just fire the test completed event.
          dispatch(event)
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
          /*completedEvent match {
            case testSucceeded: TestSucceeded => 
              testSucceeded.recordedEvents.foreach(dispatch(_))
            case testFailed: TestFailed => 
              testFailed.recordedEvents.foreach(dispatch(_))
            case testPending: TestPending => 
              testPending.recordedEvents.foreach(dispatch(_))
            case testCanceled: TestCanceled => 
              testCanceled.recordedEvents.foreach(dispatch(_))
          }*/
        case None =>
      }
    }
    waitingBuffer.clear()
    waitingBuffer ++= pending
    if (waitingBuffer.size > 0) 
      scheduleTimeoutTask()
    else {
      timeoutTask match {
        case Some(task) => 
          task.cancel()
          timeoutTask = None
        case None =>
      }
    }
  }
  
  private def scheduleTimeoutTask() {
    val head = waitingBuffer.head
    timeoutTask match {
        case Some(task) => 
          if (head.startEvent.get != task.event) {
            task.cancel()
            timeoutTask = Some(new TimeoutTask(head.startEvent.get))
            timer.schedule(timeoutTask.get, timeout.millisPart)
          }
        case None => 
          timeoutTask = Some(new TimeoutTask(head.startEvent.get))
          timer.schedule(timeoutTask.get, timeout.millisPart)
      }
  }
  
  private def timeout() {
    synchronized {
      if (waitingBuffer.size > 0) {
        val head = waitingBuffer.head
        if (timeoutTask.get.event == head.startEvent.get) {
          val newSlot = head.copy(ready = true)
          waitingBuffer.update(0, newSlot)
        }
        fireReadyEvents()
      }
    }
  }
  
  override def dispose() = propagateDispose(dispatch)
}
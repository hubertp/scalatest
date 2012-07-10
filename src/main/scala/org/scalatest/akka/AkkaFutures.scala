package org.scalatest.akka

import org.scalatest.concurrent.Futures
import akka.dispatch.{Future => FutureOfAkka}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.Resources
import akka.dispatch.Await
import akka.util.Duration
import java.util.concurrent.TimeUnit
import org.scalatest.exceptions.StackDepthExceptionHelper.getStackDepthFun
import org.scalatest.exceptions.TimeoutField
import org.scalatest.time.Span
import org.scalatest.Suite.anErrorThatShouldCauseAnAbort
import org.scalatest.exceptions.TestPendingException
import scala.annotation.tailrec

trait AkkaFutures extends Futures {

  implicit def convertAkkaFuture[T](akkaFuture: FutureOfAkka[T]): FutureConcept[T] = 
    new FutureConcept[T] {
      def isExpired = false
      def isCanceled = false
      def eitherValue = akkaFuture.value
      override def futureValue(implicit config: PatienceConfig): T = {
        
        val st = Thread.currentThread.getStackTrace
        val callerStackFrame =
          if (!st(2).getMethodName.contains("futureValue"))
            st(2)
          else
            st(3)

        val methodName =
          if (callerStackFrame.getFileName == "Futures.scala" && callerStackFrame.getMethodName == "whenReady")
            "whenReady"
          else if (callerStackFrame.getFileName == "Futures.scala" && callerStackFrame.getMethodName == "isReadyWithin")
            "isReadyWithin"
          else
            "futureValue"

        val adjustment =
          if (methodName == "whenReady")
            3
          else
            0
            
        try {
          Await.result(akkaFuture, Duration(config.timeout.totalNanos, TimeUnit.NANOSECONDS))
        }
        catch {
          case e: java.util.concurrent.TimeoutException =>
            throw new TestFailedException(
              sde => Some(Resources("wasNeverReady")),
              None,
              getStackDepthFun("JavaFutures.scala", methodName, adjustment)
            ) with TimeoutField {
              val timeout: Span = config.timeout
            }
          case e: java.util.concurrent.ExecutionException =>
            val cause = e.getCause
            val exToReport = if (cause == null) e else cause // TODO: in 2.0 add TestCanceledException here
            if (anErrorThatShouldCauseAnAbort(exToReport) || exToReport.isInstanceOf[TestPendingException]) {
              throw exToReport
            }
            throw new TestFailedException(
              sde => Some {
                if (exToReport.getMessage == null)
                  Resources("futureReturnedAnException", exToReport.getClass.getName)
                else
                  Resources("futureReturnedAnExceptionWithMessage", exToReport.getClass.getName, exToReport.getMessage)
              },
              Some(exToReport),
              getStackDepthFun("JavaFutures.scala", methodName, adjustment)
            )
        }
      }
    }
}
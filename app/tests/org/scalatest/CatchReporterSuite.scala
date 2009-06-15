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

import java.io.PrintStream
import java.io.ByteArrayOutputStream
import org.scalatest.events._

class CatchReporterSuite extends Suite {

  def testCatching() {

    val buggyReporter = new Reporter {
      override def apply(event: Event) {
        throw new RuntimeException
      }
      override def testStarting(report: Report) {
        throw new RuntimeException
      }
      override def testSucceeded(report: Report) {
        throw new RuntimeException
      }
      override def testIgnored(report: Report) {
        throw new RuntimeException
      }
      override def testFailed(report: Report) {
        throw new RuntimeException
      }
      override def suiteCompleted(report: Report) {
        throw new RuntimeException
      }
      override def infoProvided(report: Report) {
        throw new RuntimeException
      }
      override def dispose() {
        throw new RuntimeException
      }
    }

    // Pass in a PrintStream so you don't get an ugly msg to the standard error stream
    val catchReporter = new CatchReporter(buggyReporter, new PrintStream(new ByteArrayOutputStream))
    val report = new Report("name", "msg")

    intercept[RuntimeException] {
      buggyReporter.testStarting(report)
    }
    catchReporter.testStarting(report)

    intercept[RuntimeException] {
      buggyReporter(RunStarting(new Ordinal(99), 1))
    }
    catchReporter(RunStarting(new Ordinal(99), 1))

    intercept[RuntimeException] {
      buggyReporter.testStarting(report)
    }
    catchReporter.testStarting(report)

    intercept[RuntimeException] {
      buggyReporter.testSucceeded(report)
    }
    catchReporter.testSucceeded(report)

    intercept[RuntimeException] {
      buggyReporter.testIgnored(report)
    }
    catchReporter.testIgnored(report)

    intercept[RuntimeException] {
      buggyReporter.testFailed(report)
    }
    catchReporter.testFailed(report)

    intercept[RuntimeException] {
      buggyReporter(SuiteStarting(new Ordinal(99), "suite name", None))
    }
    catchReporter(SuiteStarting(new Ordinal(99), "suite name", None))

    intercept[RuntimeException] {
      buggyReporter.suiteCompleted(report)
    }
    catchReporter.suiteCompleted(report)

    intercept[RuntimeException] {
      buggyReporter(SuiteAborted(new Ordinal(99), "msg", "suiteName", None, None))
    }
    catchReporter(SuiteAborted(new Ordinal(99), "msg", "suiteName", None, None))

    intercept[RuntimeException] {
      buggyReporter.infoProvided(report)
    }
    catchReporter.infoProvided(report)

    intercept[RuntimeException] {
      buggyReporter(RunStopped(new Ordinal(99)))
    }
    catchReporter(RunStopped(new Ordinal(99)))

    intercept[RuntimeException] {
      buggyReporter(RunAborted(new Ordinal(99), "", None))
    }
    catchReporter(RunAborted(new Ordinal(99), "", None))

    intercept[RuntimeException] {
      buggyReporter(RunCompleted(new Ordinal(99)))
    }
    catchReporter(RunCompleted(new Ordinal(99)))

    intercept[RuntimeException] {
      buggyReporter.dispose()
    }
    catchReporter.dispose()
  }
}

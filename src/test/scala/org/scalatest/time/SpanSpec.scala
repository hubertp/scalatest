/*
 * Copyright 2001-2012 Artima, Inc.
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
package org.scalatest.time

import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher, ShouldMatchers}
import org.scalatest.{SeveredStackTraces, FunSpec}

class SpanSpec extends FunSpec with ShouldMatchers with SeveredStackTraces {

  def totalNanos(expectedValue: Long) =
    HavePropertyMatcher { (span: Span) =>
      HavePropertyMatchResult(
        span.totalNanos == expectedValue,
        "nanos",
        expectedValue,
        span.totalNanos
      )
    }

  def millisPart(expectedValue: Long) =
    HavePropertyMatcher { (span: Span) =>
      HavePropertyMatchResult(
        span.millisPart == expectedValue,
        "nanos",
        expectedValue,
        span.millisPart
      )
    }

  def nanosPart(expectedValue: Long) =
    HavePropertyMatcher { (span: Span) =>
      HavePropertyMatchResult(
        span.nanosPart == expectedValue,
        "nanos",
        expectedValue,
        span.nanosPart
      )
    }

  describe("A Span") {

    it("should produce IAE if a negative length is passed") {
      for (u <- Seq(Nanosecond, Nanoseconds, Microsecond, Microseconds, Millisecond, Milliseconds, Second, Seconds,
          Minute, Minutes, Hour, Hours, Day, Days)) {
        for (i <- Seq(-1, -2, -3, Long.MinValue)) {
          withClue("u was: " + u + "; i was: " + i) {
            intercept[IllegalArgumentException] {
              Span(i, u)
            }
          }
        }
        for (d <- Seq(-1, -2, -3, -1.5, -9.98, Double.MinValue)) {
          withClue("u was: " + u + "; d was: " + d) {
            intercept[IllegalArgumentException] {
              Span(d, u)
            }
          }
        }
      }
    }

    it("should produce IAE if anything other than 1 is passed for singular units forms") {
      for (u <- Seq(Nanosecond, Microsecond, Millisecond, Second, Minute, Hour, Day)) {
        for (i <- Seq(0, 2, 3, Long.MaxValue)) {
          withClue("u was: " + u + "; i was: " + i) {
            intercept[IllegalArgumentException] {
              Span(i, u)
            }
          }
        }
        for (d <- Seq(0.0, 0.1, 1.1, 2.0, 9.98, Double.MaxValue)) {
          withClue("u was: " + u + "; d was: " + d) {
            intercept[IllegalArgumentException] {
              Span(d, u)
            }
          }
        }
      }
    }

    it("should construct with valid nanoseconds passed") {

      Span(0, Nanoseconds) should have (totalNanos(0), millisPart(0), nanosPart(0))
      Span(1, Nanosecond) should have (totalNanos(1))
      Span(1, Nanoseconds) should have (totalNanos(1))
      Span(2, Nanoseconds) should have (totalNanos(2))
      Span(Long.MaxValue, Nanoseconds) should have (totalNanos(1000L * 1000 * 9223372036854L + 775807))

      Span(0.0, Nanoseconds) should have (totalNanos(0))
      Span(1.0, Nanosecond) should have (totalNanos(1))
      Span(1.0, Nanoseconds) should have (totalNanos(1))
      Span(2.0, Nanoseconds) should have (totalNanos(2))
      Span(0.1, Nanoseconds) should have (totalNanos(0))
      Span(1.1, Nanoseconds) should have (totalNanos(1))
      Span(1.2, Nanoseconds) should have (totalNanos(1))
      Span(1.499, Nanoseconds) should have (totalNanos(1))
      Span(1.5, Nanoseconds) should have (totalNanos(1))
      Span(1.9, Nanoseconds) should have (totalNanos(1))
      Span(2.2, Nanoseconds) should have (totalNanos(2))
      // Span(Long.MaxValue.toDouble, Nanoseconds) should have (totalNanos(1000L * 1000 * 9223372036854L + 775808))
    }

    it("should throw IAE if a Double nanos value larger than the largest expressible amount is passed.") {
      val biggest = Long.MaxValue.toDouble
      for (d <- Seq(biggest + 1e10, biggest + 2e10, biggest + 3e10, Double.MaxValue)) {
        withClue("d was: " + d) {
          val caught =
            intercept[IllegalArgumentException] {
              Span(d, Nanoseconds)
            }
          caught.getMessage should include ("Passed length")
        }
      }
    }

    it("should construct with valid microseconds passed") {

      Span(0, Microseconds) should have (totalNanos(0))
      Span(1, Microsecond) should have (totalNanos(1000))
      Span(1, Microseconds) should have (totalNanos(1000))
      Span(2, Microseconds) should have (totalNanos(2000))
      Span(1000, Microseconds) should have (totalNanos(1000 * 1000))
      Span(1001, Microseconds) should have (totalNanos(1001L * 1000))
      Span(1002, Microseconds) should have (totalNanos(1002L * 1000))
      Span(2000, Microseconds) should have (totalNanos(2000 * 1000))
      Span(2001, Microseconds) should have (totalNanos(2001 * 1000))
      Span(2002, Microseconds) should have (totalNanos(2002 * 1000))
     // Span(Long.MaxValue, Microseconds) should have (totalNanos(1000L * 1000 * 9223372036854775L), totalNanos(807000))

      Span(0.0, Microseconds) should have (totalNanos(0))
      Span(1.0, Microsecond) should have (totalNanos(1000))
      Span(1.0, Microseconds) should have (totalNanos(1000))
      Span(2.0, Microseconds) should have (totalNanos(2000))
      Span(1000.0, Microseconds) should have (totalNanos(1000L * 1000))
      Span(1001.0, Microseconds) should have (totalNanos(1001L * 1000))
      Span(1002.0, Microseconds) should have (totalNanos(1002L * 1000))
      Span(2000.0, Microseconds) should have (totalNanos(2000 * 1000))
      Span(2001.0, Microseconds) should have (totalNanos(2001 * 1000))
      Span(2002.0, Microseconds) should have (totalNanos(2002 * 1000))
      Span(0.1, Microseconds) should have (totalNanos(100))
      Span(1.1, Microseconds) should have (totalNanos(1100))
      Span(1.2, Microseconds) should have (totalNanos(1200))
      Span(1.499, Microseconds) should have (totalNanos(1499))
      Span(1.5, Microseconds) should have (totalNanos(1500))
      Span(1.9, Microseconds) should have (totalNanos(1900))
      Span(2.2, Microseconds) should have (totalNanos(2200))
      // Span(Long.MaxValue.toDouble, Microseconds) should have (totalNanos(1000L * 1000 * 9223372036854776L + 808000))
    }

    it("should construct with valid milliseconds passed") {

      Span(0, Milliseconds) should have (totalNanos(0))
      Span(0, Millis) should have (totalNanos(0))
      Span(1, Millisecond) should have (totalNanos(1000L * 1000))
      Span(1, Milliseconds) should have (totalNanos(1000L * 1000))
      Span(2, Milliseconds) should have (totalNanos(2 * 1000 * 1000))
      Span(2, Millis) should have (totalNanos(2 * 1000 * 1000))
      Span(1000, Milliseconds) should have (totalNanos(1000L * 1000 * 1000))
      Span(1001, Milliseconds) should have (totalNanos(1001 * 1000 * 1000))
      Span(1002, Milliseconds) should have (totalNanos(1002L * 1000 * 1000))
      Span(2000, Milliseconds) should have (totalNanos(2000L * 1000 * 1000))
      Span(2001, Milliseconds) should have (totalNanos(2001L * 1000 * 1000))
      Span(2002, Milliseconds) should have (totalNanos(2002L * 1000 * 1000))
      // Span(Long.MaxValue, Milliseconds) should have (totalNanos(1000L * 1000 * Long.MaxValue))
      // TODO: Repeat tests for Millis as well as Milliseconds
      Span(0.0, Milliseconds) should have (totalNanos(0))
      Span(0.0, Millis) should have (totalNanos(0))
      Span(1.0, Millisecond) should have (totalNanos(1000L * 1000))
      Span(1.0, Milliseconds) should have (totalNanos(1000L * 1000))
      Span(2.0, Milliseconds) should have (totalNanos(2 * 1000 * 1000))
      Span(2.0, Millis) should have (totalNanos(2 * 1000 * 1000))
      Span(1000.0, Milliseconds) should have (totalNanos(1000L * 1000 * 1000))
      Span(1001.0, Milliseconds) should have (totalNanos(1001 * 1000 * 1000))
      Span(1002.0, Milliseconds) should have (totalNanos(1002L * 1000 * 1000))
      Span(2000.0, Milliseconds) should have (totalNanos(2000L * 1000 * 1000))
      Span(2001.0, Milliseconds) should have (totalNanos(2001L * 1000 * 1000))
      Span(2002.0, Milliseconds) should have (totalNanos(2002L * 1000 * 1000))
      Span(0.1, Milliseconds) should have (totalNanos(100L * 1000))
      Span(1.1, Milliseconds) should have (totalNanos(1100L * 1000))
      Span(1.2, Milliseconds) should have (totalNanos(1200L * 1000))
      Span(1.499, Milliseconds) should have (totalNanos(1499L * 1000))
      Span(1.5, Milliseconds) should have (totalNanos(1500L * 1000))
      Span(1.9, Milliseconds) should have (totalNanos(1900L * 1000))
      Span(2.2, Milliseconds) should have (totalNanos(2200 * 1000))
      // Span(Long.MaxValue.toDouble, Milliseconds) should have (totalNanos(1000L * 1000 * Long.MaxValue))
    }
                // TODO: Write one for the max number of seconds
    it("should construct with valid seconds passed") {

      Span(0, Seconds) should have (totalNanos(0))
      Span(1, Second) should have (totalNanos(1000L * 1000 * 1000))
      Span(1, Seconds) should have (totalNanos(1000L * 1000 * 1000))
      Span(2, Seconds) should have (totalNanos(2000L * 1000 * 1000))
      Span(1000, Seconds) should have (totalNanos(1000L * 1000 * 1000000))
      Span(1001, Seconds) should have (totalNanos(1000L * 1000 * 1001000))
      Span(1002, Seconds) should have (totalNanos(1000L * 1000 * 1002000))
      Span(2000, Seconds) should have (totalNanos(1000L * 1000 * 2000000))
      Span(2001, Seconds) should have (totalNanos(1000L * 1000 * 2001000))
      Span(2002, Seconds) should have (totalNanos(1000L * 1000 * 2002000))

      Span(0.0, Seconds) should have (totalNanos(0))
      Span(1.0, Second) should have (totalNanos(1000L * 1000 * 1000))
      Span(1.0, Seconds) should have (totalNanos(1000L * 1000 * 1000))
      Span(2.0, Seconds) should have (totalNanos(2000L * 1000 * 1000))
      Span(1000.0, Seconds) should have (totalNanos(1000L * 1000 * 1000000))
      Span(1001.0, Seconds) should have (totalNanos(1000L * 1000 * 1001000))
      Span(1002.0, Seconds) should have (totalNanos(1000L * 1000 * 1002000))
      Span(2000.0, Seconds) should have (totalNanos(1000L * 1000 * 2000000))
      Span(2001.0, Seconds) should have (totalNanos(1000L * 1000 * 2001000))
      Span(2002.0, Seconds) should have (totalNanos(1000L * 1000 * 2002000))
      Span(0.1, Seconds) should have (totalNanos(1000L * 1000 * 100))
      Span(1.1, Seconds) should have (totalNanos(1000L * 1000 * 1100))
      Span(1.2, Seconds) should have (totalNanos(1000L * 1000 * 1200))
      Span(1.499, Seconds) should have (totalNanos(1000L * 1000 * 1499))
      Span(1.5, Seconds) should have (totalNanos(1000L * 1000 * 1500))
      Span(1.9, Seconds) should have (totalNanos(1000L * 1000 * 1900))
      Span(2.2, Seconds) should have (totalNanos(1000L * 1000 * 2200))
      Span(0.001, Seconds) should have (totalNanos(1000L * 1000))
      Span(88.0001, Seconds) should have (totalNanos(1000L * 1000 * 88000 + 100000))
      Span(88.000001, Seconds) should have (totalNanos(1000L * 1000 * 88000 + 1000))
      Span(88.000000001, Seconds) should have (totalNanos(1000L * 1000 * 88000 + 1))
    }

    it("should throw IAE if a seconds value larger than the largest expressible amount is passed.") {
      val biggest = Long.MaxValue / 1000
      for (i <- Seq(biggest + 1, biggest + 2, biggest + 3, Long.MaxValue)) {
        withClue("i was: " + i) {
          val caught =
            intercept[IllegalArgumentException] {
              Span(i, Seconds)
            }
          caught.getMessage should include ("Passed length")
        }
      }
    }

    it("should throw IAE if a Double seconds value larger than the largest expressible amount is passed.") {
      val biggest = (Long.MaxValue / 1000).toDouble
      for (d <- Seq(biggest + 1e10, biggest + 2e10, biggest + 3e10, Double.MaxValue)) {
        withClue("d was: " + d) {
          val caught =
            intercept[IllegalArgumentException] {
              Span(d, Seconds)
            }
          caught.getMessage should include ("Passed length")
        }
      }
    }

    it("should construct with valid minutes passed") {

      Span(0, Minutes) should have (totalNanos(0))
      Span(1, Minute) should have (totalNanos(1000L * 1000 * 1000 * 60))
      Span(1, Minutes) should have (totalNanos(1000L * 1000 * 1000 * 60))
      Span(2, Minutes) should have (totalNanos(1000L * 1000 * 2 * 1000 * 60))
      Span(1000, Minutes) should have (totalNanos(1000L * 1000 * 1000 * 1000 * 60))
      Span(1001, Minutes) should have (totalNanos(1000L * 1000 * 1001 * 1000 * 60))
      Span(1002, Minutes) should have (totalNanos(1000L * 1000 * 1002 * 1000 * 60))
      Span(2000, Minutes) should have (totalNanos(1000L * 1000 * 2000 * 1000 * 60))
      Span(2001, Minutes) should have (totalNanos(1000L * 1000 * 2001 * 1000 * 60))
      Span(2002, Minutes) should have (totalNanos(1000L * 1000 * 2002 * 1000 * 60))

      Span(0.0, Minutes) should have (totalNanos(0))
      Span(1.0, Minute) should have (totalNanos(1000L * 1000 * 1000 * 60))
      Span(1.0, Minutes) should have (totalNanos(1000L * 1000 * 1000 * 60))
      Span(2.0, Minutes) should have (totalNanos(1000L * 1000 * 2 * 1000 * 60))
      Span(1000.0, Minutes) should have (totalNanos(1000L * 1000 * 1000 * 1000 * 60))
      Span(1001.0, Minutes) should have (totalNanos(1000L * 1000 * 1001 * 1000 * 60))
      Span(1002.0, Minutes) should have (totalNanos(1000L * 1000 * 1002 * 1000 * 60))
      Span(2000.0, Minutes) should have (totalNanos(1000L * 1000 * 2000 * 1000 * 60))
      Span(2001.0, Minutes) should have (totalNanos(1000L * 1000 * 2001 * 1000 * 60))
      Span(2002.0, Minutes) should have (totalNanos(1000L * 1000 * 2002 * 1000 * 60))
      Span(0.1, Minutes) should have (totalNanos(1000L * 1000 * 100 * 60))
      Span(1.1, Minutes) should have (totalNanos(1000L * 1000 * 1100 * 60))
      Span(1.2, Minutes) should have (totalNanos(1000L * 1000 * 1200 * 60))
      Span(1.499, Minutes) should have (totalNanos(1000L * 1000 * 1499 * 60))
      Span(1.5, Minutes) should have (totalNanos(1000L * 1000 * 1500 * 60))
      Span(1.9, Minutes) should have (totalNanos(1000L * 1000 * 1900 * 60))
      Span(2.2, Minutes) should have (totalNanos(1000L * 1000 * 2200 * 60))
      Span(0.001, Minutes) should have (totalNanos(1000L * 1000 * 60))
      Span(88.0001, Minutes) should have (totalNanos(1000L * 1000 * 5280006))
      Span(88.000001, Minutes) should have (totalNanos(1000L * 1000 * 5280000 + 60000))
      Span(88.000000001, Minutes) should have (totalNanos(1000L * 1000 * 5280000 + 60))
    }

    it("should throw IAE if a minutes value larger than the largest expressible amount is passed.") {
      val biggest = Long.MaxValue / 1000 / 60
      for (i <- Seq(biggest + 1, biggest + 2, biggest + 3, Long.MaxValue)) {
        withClue("i was: " + i) {
          val caught =
            intercept[IllegalArgumentException] {
              Span(i, Minutes)
            }
          caught.getMessage should include ("Passed length")
        }
      }
    }

    it("should throw IAE if a Double minutes value larger than the largest expressible amount is passed.") {
      val biggest = (Long.MaxValue / 1000 / 60).toDouble
      for (d <- Seq(biggest + 1, biggest + 2, biggest + 3, Double.MaxValue)) {
        withClue("d was: " + d) {
          val caught =
            intercept[IllegalArgumentException] {
              Span(d, Minutes)
            }
          caught.getMessage should include ("Passed length")
        }
      }
    }

    it("should construct with valid hours passed") {

      Span(0, Hours) should have (totalNanos(0))
      Span(1, Hour) should have (totalNanos(1000L * 1000 * 1000 * 60 * 60))
      Span(1, Hours) should have (totalNanos(1000L * 1000 * 1000 * 60 * 60))
      Span(2, Hours) should have (totalNanos(1000L * 1000 * 2 * 1000 * 60 * 60))
      Span(1000, Hours) should have (totalNanos(1000L * 1000 * 1000L * 1000 * 60 * 60))
      Span(1001, Hours) should have (totalNanos(1000L * 1000 * 1001L * 1000 * 60 * 60))
      Span(1002, Hours) should have (totalNanos(1000L * 1000 * 1002L * 1000 * 60 * 60))
      Span(2000, Hours) should have (totalNanos(1000L * 1000 * 2000L * 1000 * 60 * 60))
      Span(2001, Hours) should have (totalNanos(1000L * 1000 * 2001L * 1000 * 60 * 60))
      Span(2002, Hours) should have (totalNanos(1000L * 1000 * 2002L * 1000 * 60 * 60))

      Span(0.0, Hours) should have (totalNanos(0))
      Span(1.0, Hour) should have (totalNanos(1000L * 1000 * 1000 * 60 * 60))
      Span(1.0, Hours) should have (totalNanos(1000L * 1000 * 1000 * 60 * 60))
      Span(2.0, Hours) should have (totalNanos(1000L * 1000 * 2 * 1000 * 60 * 60))
      Span(1000.0, Hours) should have (totalNanos(1000L * 1000 * 1000L * 1000 * 60 * 60))
      Span(1001.0, Hours) should have (totalNanos(1000L * 1000 * 1001L * 1000 * 60 * 60))
      Span(1002.0, Hours) should have (totalNanos(1000L * 1000 * 1002L * 1000 * 60 * 60))
      Span(2000.0, Hours) should have (totalNanos(1000L * 1000 * 2000L * 1000 * 60 * 60))
      Span(2001.0, Hours) should have (totalNanos(1000L * 1000 * 2001L * 1000 * 60 * 60))
      Span(2002.0, Hours) should have (totalNanos(1000L * 1000 * 2002L * 1000 * 60 * 60))
      Span(0.1, Hours) should have (totalNanos(1000L * 1000 * 100 * 60 * 60))
      Span(1.1, Hours) should have (totalNanos(1000L * 1000 * 1100 * 60 * 60))
      Span(1.2, Hours) should have (totalNanos(1000L * 1000 * 1200 * 60 * 60))
      Span(1.499, Hours) should have (totalNanos(1000L * 1000 * 1499 * 60 * 60))
      Span(1.5, Hours) should have (totalNanos(1000L * 1000 * 1500 * 60 * 60))
      Span(1.9, Hours) should have (totalNanos(1000L * 1000 * 1900 * 60 * 60))
      Span(2.2, Hours) should have (totalNanos(1000L * 1000 * 2200 * 60 * 60))
      Span(0.001, Hours) should have (totalNanos(1000L * 1000 * 60 * 60))
      Span(88.0001, Hours) should have (totalNanos(1000L * 1000 * 5280006 * 60))
      Span(88.000001, Hours) should have (totalNanos(1000L * 1000 * 316800003 + 600000))
      Span(88.000000001, Hours) should have (totalNanos(1000L * 1000 * 5280000 * 60 + 3600))
    }

    it("should throw IAE if an hours value larger than the largest expressible amount is passed.") {
      val biggest = Long.MaxValue / 1000 / 60 / 60
      for (i <- Seq(biggest + 1, biggest + 2, biggest + 3, Long.MaxValue)) {
        withClue("i was: " + i) {
          val caught =
            intercept[IllegalArgumentException] {
              Span(i, Hours)
            }
          caught.getMessage should include ("Passed length")
        }
      }
    }

    it("should throw IAE if a Double hours value larger than the largest expressible amount is passed.") {
      val biggest = (Long.MaxValue / 1000 / 60 / 60).toDouble
      for (d <- Seq(biggest + 1, biggest + 2, biggest + 3, Double.MaxValue)) {
        withClue("d was: " + d) {
          val caught =
            intercept[IllegalArgumentException] {
              Span(d, Hours)
            }
          caught.getMessage should include ("Passed length")
        }
      }
    }

    it("should construct with valid days passed") {

      Span(0, Days) should have (totalNanos(0))
      Span(1, Day) should have (totalNanos(1000L * 1000 * 1000 * 60 * 60 * 24))
      Span(1, Days) should have (totalNanos(1000L * 1000 * 1000 * 60 * 60 * 24))
      Span(2, Days) should have (totalNanos(1000L * 1000 * 2 * 1000 * 60 * 60 * 24))
      Span(1000, Days) should have (totalNanos(1000L * 1000 * 1000L * 1000 * 60 * 60 * 24))
      Span(1001, Days) should have (totalNanos(1000L * 1000 * 1001L * 1000 * 60 * 60 * 24))
      Span(1002, Days) should have (totalNanos(1000L * 1000 * 1002L * 1000 * 60 * 60 * 24))
      Span(2000, Days) should have (totalNanos(1000L * 1000 * 2000L * 1000 * 60 * 60 * 24))
      Span(2001, Days) should have (totalNanos(1000L * 1000 * 2001L * 1000 * 60 * 60 * 24))
      Span(2002, Days) should have (totalNanos(1000L * 1000 * 2002L * 1000 * 60 * 60 * 24))

      Span(0.0, Days) should have (totalNanos(0))
      Span(1.0, Day) should have (totalNanos(1000L * 1000 * 1000 * 60 * 60 * 24))
      Span(1.0, Days) should have (totalNanos(1000L * 1000 * 1000 * 60 * 60 * 24))
      Span(2.0, Days) should have (totalNanos(1000L * 1000 * 2 * 1000 * 60 * 60 * 24))
      Span(1000.0, Days) should have (totalNanos(1000L * 1000 * 1000L * 1000 * 60 * 60 * 24))
      Span(1001.0, Days) should have (totalNanos(1000L * 1000 * 1001L * 1000 * 60 * 60 * 24))
      Span(1002.0, Days) should have (totalNanos(1000L * 1000 * 1002L * 1000 * 60 * 60 * 24))
      Span(2000.0, Days) should have (totalNanos(1000L * 1000 * 2000L * 1000 * 60 * 60 * 24))
      Span(2001.0, Days) should have (totalNanos(1000L * 1000 * 2001L * 1000 * 60 * 60 * 24))
      Span(2002.0, Days) should have (totalNanos(1000L * 1000 * 2002L * 1000 * 60 * 60 * 24))
      Span(0.1, Days) should have (totalNanos(1000L * 1000 * 100 * 60 * 60 * 24))
      Span(1.1, Days) should have (totalNanos(1000L * 1000 * 1100 * 60 * 60 * 24))
      Span(1.2, Days) should have (totalNanos(1000L * 1000 * 1200 * 60 * 60 * 24))
      Span(1.499, Days) should have (totalNanos(1000L * 1000 * 1499 * 60 * 60 * 24))
      Span(1.5, Days) should have (totalNanos(1000L * 1000 * 1500 * 60 * 60 * 24))
      Span(1.9, Days) should have (totalNanos(1000L * 1000 * 1900 * 60 * 60 * 24))
      Span(2.2, Days) should have (totalNanos(1000L * 1000 * 2200 * 60 * 60 * 24))
      Span(0.001, Days) should have (totalNanos(1000L * 1000 * 60 * 60 * 24))
      Span(88.0001, Days) should have (totalNanos(1000L * 1000 * 5280006L * 60 * 24))
      Span(88.000001, Days) should have (totalNanos(1000L * 1000 * 7603200086L + 400000))
      Span(88.000000001, Days) should have (totalNanos(1000L * 1000 * 5280000L * 60 * 24 + 86400))
    }

    it("should throw IAE if a days value larger than the largest expressible amount is passed.") {
      val biggest = Long.MaxValue / 1000 / 60 / 60 / 24
      for (i <- Seq(biggest + 1, biggest + 2, biggest + 3, Long.MaxValue)) {
        withClue("i was: " + i) {
          val caught =
            intercept[IllegalArgumentException] {
              Span(i, Days)
            }
          caught.getMessage should include ("Passed length")
        }
      }
    }

    it("should throw IAE if a Double days value larger than the largest expressible amount is passed.") {
      val biggest = (Long.MaxValue / 1000 / 60 / 60 / 24).toDouble
      for (d <- Seq(biggest + 1, biggest + 2, biggest + 3, Double.MaxValue)) {
        withClue("d was: " + d) {
          val caught =
            intercept[IllegalArgumentException] {
              Span(d, Days)
            }
          caught.getMessage should include ("Passed length")
        }
      }
    }

    it("should give pretty, localized toStrings reflecting what went in") {
      Span(1, Nanosecond).prettyString should be ("1 nanosecond")
      Span(1, Nanoseconds).prettyString should be ("1 nanosecond")
      Span(2, Nanoseconds).prettyString should be ("2 nanoseconds")
      Span(1.0, Nanosecond).prettyString should be ("1.0 nanosecond")
      Span(1.0, Nanoseconds).prettyString should be ("1.0 nanosecond")
      Span(1.1, Nanoseconds).prettyString should be ("1.1 nanoseconds")
      Span(2.0, Nanoseconds).prettyString should be ("2.0 nanoseconds")

      Span(1, Microsecond).prettyString should be ("1 microsecond")
      Span(1, Microseconds).prettyString should be ("1 microsecond")
      Span(2, Microseconds).prettyString should be ("2 microseconds")
      Span(1.0, Microsecond).prettyString should be ("1.0 microsecond")
      Span(1.0, Microseconds).prettyString should be ("1.0 microsecond")
      Span(1.1, Microseconds).prettyString should be ("1.1 microseconds")
      Span(2.0, Microseconds).prettyString should be ("2.0 microseconds")

      Span(1, Millisecond).prettyString should be ("1 millisecond")
      Span(1, Milliseconds).prettyString should be ("1 millisecond")
      Span(2, Milliseconds).prettyString should be ("2 milliseconds")
      Span(1.0, Millisecond).prettyString should be ("1.0 millisecond")
      Span(1.0, Milliseconds).prettyString should be ("1.0 millisecond")
      Span(1.1, Milliseconds).prettyString should be ("1.1 milliseconds")
      Span(2.0, Milliseconds).prettyString should be ("2.0 milliseconds")

      Span(1, Second).prettyString should be ("1 second")
      Span(1, Seconds).prettyString should be ("1 second")
      Span(2, Seconds).prettyString should be ("2 seconds")
      Span(1.0, Second).prettyString should be ("1.0 second")
      Span(1.0, Seconds).prettyString should be ("1.0 second")
      Span(1.1, Seconds).prettyString should be ("1.1 seconds")
      Span(2.0, Seconds).prettyString should be ("2.0 seconds")

      Span(1, Minute).prettyString should be ("1 minute")
      Span(1, Minutes).prettyString should be ("1 minute")
      Span(2, Minutes).prettyString should be ("2 minutes")
      Span(1.0, Minute).prettyString should be ("1.0 minute")
      Span(1.0, Minutes).prettyString should be ("1.0 minute")
      Span(1.1, Minutes).prettyString should be ("1.1 minutes")
      Span(2.0, Minutes).prettyString should be ("2.0 minutes")

      Span(1, Hour).prettyString should be ("1 hour")
      Span(1, Hours).prettyString should be ("1 hour")
      Span(2, Hours).prettyString should be ("2 hours")
      Span(1.0, Hour).prettyString should be ("1.0 hour")
      Span(1.0, Hours).prettyString should be ("1.0 hour")
      Span(1.1, Hours).prettyString should be ("1.1 hours")
      Span(2.0, Hours).prettyString should be ("2.0 hours")

      Span(1, Day).prettyString should be ("1 day")
      Span(1, Days).prettyString should be ("1 day")
      Span(2, Days).prettyString should be ("2 days")
      Span(1.0, Day).prettyString should be ("1.0 day")
      Span(1.0, Days).prettyString should be ("1.0 day")
      Span(1.1, Days).prettyString should be ("1.1 days")
      Span(2.0, Days).prettyString should be ("2.0 days")
    }

    it("should have a pretty toString") {
      Span(1, Nanosecond).toString should be ("Span(1, Nanosecond)")
      Span(1, Nanoseconds).toString should be ("Span(1, Nanoseconds)")
      Span(2, Nanoseconds).toString should be ("Span(2, Nanoseconds)")
      Span(1.0, Nanosecond).toString should be ("Span(1.0, Nanosecond)")
      Span(1.0, Nanoseconds).toString should be ("Span(1.0, Nanoseconds)")
      Span(1.1, Nanoseconds).toString should be ("Span(1.1, Nanoseconds)")
      Span(2.0, Nanoseconds).toString should be ("Span(2.0, Nanoseconds)")

      Span(1, Microsecond).toString should be ("Span(1, Microsecond)")
      Span(1, Microseconds).toString should be ("Span(1, Microseconds)")
      Span(2, Microseconds).toString should be ("Span(2, Microseconds)")
      Span(1.0, Microsecond).toString should be ("Span(1.0, Microsecond)")
      Span(1.0, Microseconds).toString should be ("Span(1.0, Microseconds)")
      Span(1.1, Microseconds).toString should be ("Span(1.1, Microseconds)")
      Span(2.0, Microseconds).toString should be ("Span(2.0, Microseconds)")

      Span(1, Millisecond).toString should be ("Span(1, Millisecond)")
      Span(1, Milliseconds).toString should be ("Span(1, Milliseconds)")
      Span(2, Milliseconds).toString should be ("Span(2, Milliseconds)")
      Span(1.0, Millisecond).toString should be ("Span(1.0, Millisecond)")
      Span(1.0, Milliseconds).toString should be ("Span(1.0, Milliseconds)")
      Span(1.1, Milliseconds).toString should be ("Span(1.1, Milliseconds)")
      Span(2.0, Milliseconds).toString should be ("Span(2.0, Milliseconds)")

      Span(1, Second).toString should be ("Span(1, Second)")
      Span(1, Seconds).toString should be ("Span(1, Seconds)")
      Span(2, Seconds).toString should be ("Span(2, Seconds)")
      Span(1.0, Second).toString should be ("Span(1.0, Second)")
      Span(1.0, Seconds).toString should be ("Span(1.0, Seconds)")
      Span(1.1, Seconds).toString should be ("Span(1.1, Seconds)")
      Span(2.0, Seconds).toString should be ("Span(2.0, Seconds)")

      Span(1, Minute).toString should be ("Span(1, Minute)")
      Span(1, Minutes).toString should be ("Span(1, Minutes)")
      Span(2, Minutes).toString should be ("Span(2, Minutes)")
      Span(1.0, Minute).toString should be ("Span(1.0, Minute)")
      Span(1.0, Minutes).toString should be ("Span(1.0, Minutes)")
      Span(1.1, Minutes).toString should be ("Span(1.1, Minutes)")
      Span(2.0, Minutes).toString should be ("Span(2.0, Minutes)")

      Span(1, Hour).toString should be ("Span(1, Hour)")
      Span(1, Hours).toString should be ("Span(1, Hours)")
      Span(2, Hours).toString should be ("Span(2, Hours)")
      Span(1.0, Hour).toString should be ("Span(1.0, Hour)")
      Span(1.0, Hours).toString should be ("Span(1.0, Hours)")
      Span(1.1, Hours).toString should be ("Span(1.1, Hours)")
      Span(2.0, Hours).toString should be ("Span(2.0, Hours)")

      Span(1, Day).toString should be ("Span(1, Day)")
      Span(1, Days).toString should be ("Span(1, Days)")
      Span(2, Days).toString should be ("Span(2, Days)")
      Span(1.0, Day).toString should be ("Span(1.0, Day)")
      Span(1.0, Days).toString should be ("Span(1.0, Days)")
      Span(1.1, Days).toString should be ("Span(1.1, Days)")
      Span(2.0, Days).toString should be ("Span(2.0, Days)")
    }
    // TODO: write tests for equals and hashcode
  }
}

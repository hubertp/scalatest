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

import scala.reflect.NameTransformer.encode
import org.scalatest.events._
import collection.immutable.TreeSet

class SpecSpec extends FunSpec with PrivateMethodTester with SharedHelpers {

  describe("A Spec") {
    /*
    it("should send InfoProvided events with aboutAPendingTest set to true and aboutACanceledTest set to false for info " +
            "calls made from a test that is pending") {
      val a = new Spec {
        def `test: something`(r: Rep) {
          r.info("two integers")
          r.info("one is subracted from the other")
          r.info("the result is the difference between the two numbers")
          pending
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep, new Stopper {}, Filter(), Map(), None, new Tracker(), Set.empty))
      val testPending = rep.testPendingEventsReceived
      assert(testPending.size === 1)
      val recordedEvents = testPending(0).recordedEvents
      assert(recordedEvents.size === 3)
      for (event <- recordedEvents) {
        val ip = event.asInstanceOf[InfoProvided]
        assert(ip.aboutAPendingTest.isDefined && ip.aboutAPendingTest.get)
        assert(ip.aboutACanceledTest.isDefined && !ip.aboutACanceledTest.get)
      }
    }
    it("should send InfoProvided events with aboutAPendingTest and aboutACanceledTest set to false for info " +
            "calls made from a test that is not pending or canceled") {
      val a = new Spec {
        def `test: something`(r: Rep) {
          r.info("two integers")
          r.info("one is subracted from the other")
          r.info("the result is the difference between the two numbers")
          assert(1 + 1 === 2)
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep, new Stopper {}, Filter(), Map(), None, new Tracker(), Set.empty))
      val testSucceeded = rep.testSucceededEventsReceived
      assert(testSucceeded.size === 1)
      val recordedEvents = testSucceeded(0).recordedEvents
      assert(recordedEvents.size === 3)
      for (event <- recordedEvents) {
        val ip = event.asInstanceOf[InfoProvided]
        assert(ip.aboutAPendingTest.isDefined && !ip.aboutAPendingTest.get)
        assert(ip.aboutACanceledTest.isDefined && !ip.aboutACanceledTest.get)
      }
    }
    it("should send InfoProvided events with aboutAPendingTest set to false and aboutACanceledTest set to true for info " +
            "calls made from a test that is canceled") {
      val a = new Spec {
        def `test: something`(r: Rep) {
          r.info("two integers")
          r.info("one is subracted from the other")
          r.info("the result is the difference between the two numbers")
          cancel()
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep, new Stopper {}, Filter(), Map(), None, new Tracker(), Set.empty))
      val testCanceled = rep.testCanceledEventsReceived
      assert(testCanceled.size === 1)
      val recordedEvents = testCanceled(0).recordedEvents
      assert(recordedEvents.size === 3)
      for (event <- recordedEvents) {
        val ip = event.asInstanceOf[InfoProvided]
        assert(ip.aboutAPendingTest.isDefined && !ip.aboutAPendingTest.get)
        assert(ip.aboutACanceledTest.isDefined && ip.aboutACanceledTest.get)
      }
    }
*/
    
    it("should return the test names in alphabetical order from testNames") {
      val a = new Spec {
        def `it should do this`() {}
        def `it should do that`() {}
      }

      expectResult(List(encode("it should do that"), encode("it should do this"))) {
        a.testNames.iterator.toList
      }

      val b = new Spec {}

      expectResult(List[String]()) {
        b.testNames.iterator.toList
      }

      val c = new Spec {
        def `test: that`() {}
        def `test: this`() {}
      }

      expectResult(List(encode("test: that"), encode("test: this"))) {
        c.testNames.iterator.toList
      }

      val d = new Spec {
        object `A Tester` {
          def `should test that` {}
          def `should test this` {}
        }
      }

      expectResult(List(encode("A Tester should test that"), encode("A Tester should test this"))) {
        d.testNames.iterator.toList
      }

      val e = new Spec {
        object `A Tester` {
          def `should test this` {}
          def `should test that` {}
        }
      }

      expectResult(List(encode("A Tester should test this"), encode("A Tester should test that"))) {
        e.testNames.iterator.toList
      }
    }
 
    class TestWasCalledSpec extends Spec {
      var theTestThisCalled = false
      var theTestThatCalled = false
      def `test: this`() { theTestThisCalled = true }
      def `test: that`() { theTestThatCalled = true }
    }
    
    it("should execute all tests when run is called with testName None") {

      val b = new TestWasCalledSpec
      b.run(None, Args(SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))
      assert(b.theTestThisCalled)
      assert(b.theTestThatCalled)
    }
    
    it("should execute one test when run is called with a defined testName") {

      val a = new TestWasCalledSpec
      a.run(Some(encode("test: this")), Args(SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))
      assert(a.theTestThisCalled)
      assert(!a.theTestThatCalled)
    }
    
    it("should report as ignored, and not run, tests marked ignored") {

      val a = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        def `test: this`() { theTestThisCalled = true }
        def `test: that` { theTestThatCalled = true }
      }

      val repA = new TestIgnoredTrackingReporter
      a.run(None, Args(repA, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))
      assert(!repA.testIgnoredReceived)
      assert(a.theTestThisCalled)
      assert(a.theTestThatCalled)

      val b = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @Ignore
        def `test: this`() { theTestThisCalled = true }
        def `test: that` { theTestThatCalled = true }
      }

      val repB = new TestIgnoredTrackingReporter
      b.run(None, Args(repB, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))
      assert(repB.testIgnoredReceived)
      assert(repB.lastEvent.isDefined)
      assert(repB.lastEvent.get.testName endsWith encode("test: this"))
      assert(!b.theTestThisCalled)
      assert(b.theTestThatCalled)

      val c = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        def `test: this`() { theTestThisCalled = true }
        @Ignore
        def `test: that` { theTestThatCalled = true }
      }

      val repC = new TestIgnoredTrackingReporter
      c.run(None, Args(repC, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))
      assert(repC.testIgnoredReceived)
      assert(repC.lastEvent.isDefined)
      assert(repC.lastEvent.get.testName endsWith encode("test: that"), repC.lastEvent.get.testName)
      assert(c.theTestThisCalled)
      assert(!c.theTestThatCalled)

      val d = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @Ignore
        def `test: this`() { theTestThisCalled = true }
        @Ignore
        def `test: that` { theTestThatCalled = true }
      }

      val repD = new TestIgnoredTrackingReporter
      d.run(None, Args(repD, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))
      assert(repD.testIgnoredReceived)
      assert(repD.lastEvent.isDefined)
      assert(repD.lastEvent.get.testName === encode("test: this")) // last because run alphabetically
      assert(!d.theTestThisCalled)
      assert(!d.theTestThatCalled)
    }
    
    it("should ignore a test marked as ignored if run is invoked with that testName") {

      val e = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @Ignore
        def `test: this`() { theTestThisCalled = true }
        def `test: that` { theTestThatCalled = true }
      }

      val repE = new TestIgnoredTrackingReporter
      e.run(Some(encode("test: this")), Args(repE, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))
      assert(repE.testIgnoredReceived)
      assert(!e.theTestThisCalled)
      assert(!e.theTestThatCalled)
    }
    
    it("should exclude a test with a tag included in the tagsToExclude set even if run is invoked with that testName") {

      val e = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @SlowAsMolasses
        def `test: this`() { theTestThisCalled = true }
        def `test: that` { theTestThatCalled = true }
      }

      val repE = new TestIgnoredTrackingReporter
      e.run(Some(encode("test: this")), Args(repE, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses")), Map(), None, new Tracker, Set.empty))
      assert(!repE.testIgnoredReceived)
      assert(!e.theTestThisCalled)
      assert(!e.theTestThatCalled)
    }
    
    it("should throw IllegalArgumentException if run is passed a testName that does not exist") {
      val spec = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        def `test: this`() { theTestThisCalled = true }
        def `test: that` { theTestThatCalled = true }
      }

      intercept[IllegalArgumentException] {
        // Here, they forgot that the name is actually `test: this`(Fixture)
        spec.run(Some(encode("test: misspelled")), Args(SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))
      }
    }
    
    it("should run only those tests selected by the tags to include and exclude sets") {

      // Nothing is excluded
      val a = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @SlowAsMolasses
        def `test: this`() { theTestThisCalled = true }
        def `test: that` { theTestThatCalled = true }
      }
      val repA = new TestIgnoredTrackingReporter
      a.run(None, Args(repA, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))
      assert(!repA.testIgnoredReceived)
      assert(a.theTestThisCalled)
      assert(a.theTestThatCalled)

      // SlowAsMolasses is included, one test should be excluded
      val b = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @SlowAsMolasses
        def `test: this`() { theTestThisCalled = true }
        def `test: that` { theTestThatCalled = true }
      }
      val repB = new TestIgnoredTrackingReporter
      b.run(None, Args(repB, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), Map(), None, new Tracker, Set.empty))
      assert(!repB.testIgnoredReceived)
      assert(b.theTestThisCalled)
      assert(!b.theTestThatCalled)

      // SlowAsMolasses is included, and both tests should be included
      val c = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @SlowAsMolasses
        def `test: this`() { theTestThisCalled = true }
        @SlowAsMolasses
        def `test: that` { theTestThatCalled = true }
      }
      val repC = new TestIgnoredTrackingReporter
      c.run(None, Args(repB, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), Map(), None, new Tracker, Set.empty))
      assert(!repC.testIgnoredReceived)
      assert(c.theTestThisCalled)
      assert(c.theTestThatCalled)

      // SlowAsMolasses is included. both tests should be included but one ignored
      val d = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @Ignore
        @SlowAsMolasses
        def `test: this`() { theTestThisCalled = true }
        @SlowAsMolasses
        def `test: that` { theTestThatCalled = true }
      }
      val repD = new TestIgnoredTrackingReporter
      d.run(None, Args(repD, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.Ignore")), Map(), None, new Tracker, Set.empty))
      assert(repD.testIgnoredReceived)
      assert(!d.theTestThisCalled)
      assert(d.theTestThatCalled)

      // SlowAsMolasses included, FastAsLight excluded
      val e = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @FastAsLight
        @SlowAsMolasses
        def `test: this`() { theTestThisCalled = true }
        @SlowAsMolasses
        def `test: that` { theTestThatCalled = true }
        def `test: the other` { theTestTheOtherCalled = true }
      }
      val repE = new TestIgnoredTrackingReporter
      e.run(None, Args(repE, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
                Map(), None, new Tracker, Set.empty))
      assert(!repE.testIgnoredReceived)
      assert(!e.theTestThisCalled)
      assert(e.theTestThatCalled)
      assert(!e.theTestTheOtherCalled)

      // An Ignored test that was both included and excluded should not generate a TestIgnored event
      val f = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @Ignore
        @FastAsLight
        @SlowAsMolasses
        def `test: this`() { theTestThisCalled = true }
        @SlowAsMolasses
        def `test: that` { theTestThatCalled = true }
        def `test: the other` { theTestTheOtherCalled = true }
      }
      val repF = new TestIgnoredTrackingReporter
      f.run(None, Args(repF, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
                Map(), None, new Tracker, Set.empty))
      assert(!repF.testIgnoredReceived)
      assert(!f.theTestThisCalled)
      assert(f.theTestThatCalled)
      assert(!f.theTestTheOtherCalled)

      // An Ignored test that was not included should not generate a TestIgnored event
      val g = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @FastAsLight
        @SlowAsMolasses
        def `test: this`() { theTestThisCalled = true }
        @SlowAsMolasses
        def `test: that` { theTestThatCalled = true }
        @Ignore
        def `test: the other` { theTestTheOtherCalled = true }
      }
      val repG = new TestIgnoredTrackingReporter
      g.run(None, Args(repG, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
                Map(), None, new Tracker, Set.empty))
      assert(!repG.testIgnoredReceived)
      assert(!g.theTestThisCalled)
      assert(g.theTestThatCalled)
      assert(!g.theTestTheOtherCalled)

      // No tagsToInclude set, FastAsLight excluded
      val h = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @FastAsLight
        @SlowAsMolasses
        def `test: this`() { theTestThisCalled = true }
        @SlowAsMolasses
        def `test: that` { theTestThatCalled = true }
        def `test: the other` { theTestTheOtherCalled = true }
      }
      val repH = new TestIgnoredTrackingReporter
      h.run(None, Args(repH, new Stopper {}, Filter(None, Set("org.scalatest.FastAsLight")), Map(), None, new Tracker, Set.empty))
      assert(!repH.testIgnoredReceived)
      assert(!h.theTestThisCalled)
      assert(h.theTestThatCalled)
      assert(h.theTestTheOtherCalled)

      // No tagsToInclude set, SlowAsMolasses excluded
      val i = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @FastAsLight
        @SlowAsMolasses
        def `test: this`() { theTestThisCalled = true }
        @SlowAsMolasses
        def `test: that` { theTestThatCalled = true }
        def `test: the other` { theTestTheOtherCalled = true }
      }
      val repI = new TestIgnoredTrackingReporter
      i.run(None, Args(repI, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses")), Map(), None, new Tracker, Set.empty))
      assert(!repI.testIgnoredReceived)
      assert(!i.theTestThisCalled)
      assert(!i.theTestThatCalled)
      assert(i.theTestTheOtherCalled)

      // No tagsToInclude set, SlowAsMolasses excluded, TestIgnored should not be received on excluded ones
      val j = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @Ignore
        @FastAsLight
        @SlowAsMolasses
        def `test: this`() { theTestThisCalled = true }
        @Ignore
        @SlowAsMolasses
        def `test: that` { theTestThatCalled = true }
        def `test: the other` { theTestTheOtherCalled = true }
      }
      val repJ = new TestIgnoredTrackingReporter
      j.run(None, Args(repJ, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses")), Map(), None, new Tracker, Set.empty))
      assert(!repI.testIgnoredReceived)
      assert(!j.theTestThisCalled)
      assert(!j.theTestThatCalled)
      assert(j.theTestTheOtherCalled)

      // Same as previous, except Ignore specifically mentioned in excludes set
      val k = new Spec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @Ignore
        @FastAsLight
        @SlowAsMolasses
        def `test: this`() { theTestThisCalled = true }
        @Ignore
        @SlowAsMolasses
        def `test: that` { theTestThatCalled = true }
        @Ignore
        def `test: the other` { theTestTheOtherCalled = true }
      }
      val repK = new TestIgnoredTrackingReporter
      k.run(None, Args(repK, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore")), Map(), None, new Tracker, Set.empty))
      assert(repK.testIgnoredReceived)
      assert(!k.theTestThisCalled)
      assert(!k.theTestThatCalled)
      assert(!k.theTestTheOtherCalled)
    }
    
    it("should return the correct test count from its expectedTestCount method") {

      val a = new Spec {
        def `test: this`() = ()
        def `test: that` = ()
      }
      assert(a.expectedTestCount(Filter()) === 2)

      val b = new Spec {
        @Ignore
        def `test: this`() = ()
        def `test: that` = ()
      }
      assert(b.expectedTestCount(Filter()) === 1)

      val c = new Spec {
        @FastAsLight
        def `test: this`() = ()
        def `test: that` = ()
      }
      assert(c.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(c.expectedTestCount(Filter(None, Set("org.scalatest.FastAsLight"))) === 1)

      val d = new Spec {
        @FastAsLight
        @SlowAsMolasses
        def `test: this`() = ()
        @SlowAsMolasses
        def `test: that` = ()
        def `test: the other thing` = ()
      }
      assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
      assert(d.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 1)
      assert(d.expectedTestCount(Filter()) === 3)

      val e = new Spec {
        @FastAsLight
        @SlowAsMolasses
        def `test: this`() = ()
        @SlowAsMolasses
        def `test: that` = ()
        @Ignore
        def `test: the other thing` = ()
      }
      assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
      assert(e.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 0)
      assert(e.expectedTestCount(Filter()) === 2)

      val f = new Specs(a, b, c, d, e)
      assert(f.expectedTestCount(Filter()) === 10)
    }
    
    it("should generate a TestPending message when the test body is (pending)") {
      val a = new Spec {

        def `test: do this`() { pending }

        def `test: do that`() {
          assert(2 + 2 === 4)
        }

        def `test: do something else`() {
          assert(2 + 2 === 4)
          pending
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep, new Stopper {}, Filter(), Map(), None, new Tracker(), Set.empty))
      val tp = rep.testPendingEventsReceived
      assert(tp.size === 2)
    }
    
    it("should generate a TestCanceled message when the test body includes a cancel call") {
      val a = new Spec {

        def `test: do this`() { cancel() }

        def `test: do that`() {
          assert(2 + 2 === 4)
        }

        def `test: do something else`() {
          assert(2 + 2 === 4)
          cancel()
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep, new Stopper {}, Filter(), Map(), None, new Tracker(), Set.empty))
      val tp = rep.testCanceledEventsReceived
      assert(tp.size === 2)
    }
    
    it("should generate a TestCanceled message when the test body includes a failed assume call") {
      val a = new Spec {

        def `test: do this`() { assume(1 === 2) }

        def `test: do that`() {
          assert(2 + 2 === 4)
        }

        def `test: do something else`() {
          assert(2 + 2 === 4)
          assume(3 === 4)
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep, new Stopper {}, Filter(), Map(), None, new Tracker(), Set.empty))
      val tp = rep.testCanceledEventsReceived
      assert(tp.size === 2)
    }
    
    it("should generate a test failure if a Throwable, or an Error other than direct Error subtypes " +
            "known in JDK 1.5, excluding AssertionError") {
      val a = new Spec {
        def `test: throws AssertionError`() { throw new AssertionError }
        def `test: throws plain old Error`() { throw new Error }
        def `test: throws Throwable`() { throw new Throwable }
      }
      val rep = new EventRecordingReporter
      a.run(None, Args(rep, new Stopper {}, Filter(), Map(), None, new Tracker(), Set.empty))
      val tf = rep.testFailedEventsReceived
      assert(tf.size === 3)
    }
    
    it("should propagate out Errors that are direct subtypes of Error in JDK 1.5, other than " +
            "AssertionError, causing Suites and Runs to abort.") {
      val a = new Spec {
        def `test: throws AssertionError`() { throw new OutOfMemoryError }
      }
      intercept[OutOfMemoryError] {
        a.run(None, Args(SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker(), Set.empty))
      }
    }
    
    it("should invoke withFixture from runTest for no-arg test method") {
      val a = new Spec {
        var withFixtureWasInvoked = false
        var theTestWasInvoked = false
        override def withFixture(test: NoArgTest) {
          withFixtureWasInvoked = true
          super.withFixture(test)
        }
        def `test: something`() {
          theTestWasInvoked = true
        }
      }
      a.run(None, Args(SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker(), Set.empty))
      assert(a.withFixtureWasInvoked)
      assert(a.theTestWasInvoked)
    }
    
    it("should pass the correct test name in the NoArgTest passed to withFixture") {
      val a = new Spec {
        var correctTestNameWasPassed = false
        override def withFixture(test: NoArgTest) {
          correctTestNameWasPassed = test.name == encode("test: something")
          super.withFixture(test)
        }
        def `test: something` {}
      }
      a.run(None, Args(SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker(), Set.empty))
      assert(a.correctTestNameWasPassed)
    }

    it("should pass the correct config map in the NoArgTest passed to withFixture") {
      val a = new Spec {
        var correctConfigMapWasPassed = false
        override def withFixture(test: NoArgTest) {
          correctConfigMapWasPassed = (test.configMap == Map("hi" -> 7))
          super.withFixture(test)
        }
        def `test: something` {}
      }
      a.run(None, Args(SilentReporter, new Stopper {}, Filter(), Map("hi" -> 7), None, new Tracker(), Set.empty))
      assert(a.correctConfigMapWasPassed)
    }
    
    it("should, when a test method writes to the Informer, report the info in test completion event") {
      val msg = "hi there dude"
      class MySpec extends Spec {
        def `test: with Informer` {
          info(msg)
        }
      }
      val myRep = new EventRecordingReporter
      new MySpec().run(None, Args(myRep, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))
      val testStarting = myRep.testStartingEventsReceived
      assert(testStarting.size === 1)
      val testSucceeded = myRep.testSucceededEventsReceived
      assert(testSucceeded.size === 1)
      assert(testSucceeded(0).recordedEvents.size === 1)
      val ip: InfoProvided = testSucceeded(0).recordedEvents(0).asInstanceOf[InfoProvided]
      assert(msg === ip.message)
    }
    
    describe("the stopper") {
      
      it("should stop nested suites from being executed") {
        class SpecA extends Spec {
          var executed = false;
          override def run(testName: Option[String], args: Args) {
            executed = true
            super.run(testName, args)
          }
        }
        class SpecB extends Spec {
          var executed = false;
          override def run(testName: Option[String], args: Args) {
            executed = true
            super.run(testName, args)
          }
        }
        class SpecC extends Spec {
          var executed = false;
          override def run(testName: Option[String], args: Args) {
            executed = true
            super.run(testName, args)
          }
        }
        class SpecD extends Spec {
          var executed = false;
          override def run(testName: Option[String], args: Args) {
            executed = true
            super.run(testName, args)
            args.stopper match {
              case s: MyStopper => s.stop = true
              case _ =>
            }
          }
        }
        class SpecE extends Spec {
          var executed = false;
          override def run(testName: Option[String], args: Args) {
            executed = true
            super.run(testName, args)
          }
        }
        class SpecF extends Spec {
          var executed = false;
          override def run(testName: Option[String], args: Args) {
            executed = true
            super.run(testName, args)
          }
        }
        class SpecG extends Spec {
          var executed = false;
          override def run(testName: Option[String], args: Args) {
            executed = true
            super.run(testName, args)
          }
        }

        val a = new SpecA
        val b = new SpecB
        val c = new SpecC
        val d = new SpecD
        val e = new SpecE
        val f = new SpecF
        val g = new SpecG

        val x = Specs(a, b, c, d, e, f, g)
        x.run(None, Args(SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))

        assert(a.executed)
        assert(b.executed)
        assert(c.executed)
        assert(d.executed)
        assert(e.executed)
        assert(f.executed)
        assert(g.executed)

        class MyStopper extends Stopper {
          var stop = false
          override def apply() = stop
        }

        val h = new SpecA
        val i = new SpecB
        val j = new SpecC
        val k = new SpecD
        val l = new SpecE
        val m = new SpecF
        val n = new SpecG

        val y = Specs(h, i, j, k, l, m, n)
        y.run(None, Args(SilentReporter, new MyStopper, Filter(), Map(), None, new Tracker, Set.empty))

        assert(k.executed)
        assert(i.executed)
        assert(j.executed)
        assert(k.executed)
        assert(!l.executed)
        assert(!m.executed)
        assert(!n.executed)
      }
      
      it("should stop tests from being executed") {

        class MySpec extends Spec {
          var theTestsExecutedCount = 0
          def `test: 1`() { theTestsExecutedCount += 1 }
          def `test: 2`() { theTestsExecutedCount += 1 }
          def `test: 3`() { theTestsExecutedCount += 1 }
          def `test: 4`() {
            theTestsExecutedCount += 1
          }
          def `test: 5`() { theTestsExecutedCount += 1 }
          def `test: 6`() { theTestsExecutedCount += 1 }
          def `test: 7`() { theTestsExecutedCount += 1 }
        }

        val x = new MySpec
        x.run(None, Args(SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))
        assert(x.theTestsExecutedCount === 7)

        class MyStopper extends Stopper {
          var stop = false
          override def apply() = stop
        }

        val myStopper = new MyStopper

        class MyStoppingSpec extends Spec {
          var testsExecutedCount = 0
          def `test: 1`() { testsExecutedCount += 1 }
          def `test: 2`() { testsExecutedCount += 1 }
          def `test: 3`() { testsExecutedCount += 1 }
          def `test: 4`() {
            testsExecutedCount += 1
            myStopper.stop = true
          }
          def `test: 5`() { testsExecutedCount += 1 }
          def `test: 6`() { testsExecutedCount += 1 }
          def `test: 7`() { testsExecutedCount += 1 }
        }

        val y = new MyStoppingSpec
        y.run(None, Args(SilentReporter, myStopper, Filter(), Map(), None, new Tracker, Set.empty))
        assert(y.testsExecutedCount === 4)
      }
    }
  }
  
  describe("A Suite's execute method") {
    it("should throw NPE if passed null for configMap") {
      class MySpec extends Spec
      intercept[NullPointerException] {
        (new MySpec).execute(configMap = null)
      }
    }
    it("should throw IAE if a testName is passed that does not exist on the suite") {
      class MySpec extends Spec
      intercept[IllegalArgumentException] {
        (new MySpec).execute(testName = "fred")
      }
    }
  }

  // TODO: ensure spec aborts if same name is used with and without communicator
  it("should discover method names and tags") {

    val a = new Spec {
      def `some test name`: Unit = ()
    }
    assert(a.expectedTestCount(Filter()) === 1)
    val tnResult: Set[String] = a.testNames
    val gResult: Map[String, Set[String]] = a.tags
    assert(tnResult.size === 1)
    assert(gResult.keySet.size === 0)
  }
  
  it("should not return tests with no tags in the tags map") {
    
    val a = new Spec {
      def `test: not tagged` = ()
    }
    assert(a.tags.keySet.size === 0)
  }
  
  it("should discover methods that return non-Unit") {
    val a = new Spec {
      def `test: this`: Int = 1
      def `test: that`(): String = "hi"
    }
    assert(a.expectedTestCount(Filter()) === 2)
    assert(a.testNames.size === 2)
    assert(a.tags.keySet.size === 0)
  }
  
  it("should send defined durations") {

    class MySpec extends Spec {
      def `test succeeds` = ()
      def `test fails` { fail() }
    }

    val mySpec = new MySpec
    val myReporter = new TestDurationReporter
    mySpec.run(None, Args(myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)), Set.empty))
    assert(myReporter.testSucceededWasFiredAndHadADuration)
    assert(myReporter.testFailedWasFiredAndHadADuration)
  }
  
  class SpecThatAborts extends Spec {
    override def run(testName: Option[String], args: Args) {
      throw new RuntimeException("Aborting for testing purposes")
    }
  }

  it("should fire a defined duration in a SuiteCompleted event") {

    // the spec duration is sent by runNestedSuites, so MySpec needs a
    // nested suite
    class MySpec extends Spec {
      override def nestedSuites = Vector(new Spec {})
      def testSucceeds() = ()
      def testFails() { fail() }
    }

    val mySpec = new MySpec
    val myReporter = new SuiteDurationReporter
    mySpec.run(None, Args(myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)), Set.empty))
    assert(myReporter.suiteCompletedWasFiredAndHadADuration)

  }

  it("should fire a defined duration in a SuiteAborted event") {

    // the suite duration is sent by runNestedSuites, so MySuite needs a
    // nested suite
    class MyOtherSpec extends Spec {
      override def nestedSuites = Vector(new SpecThatAborts)
      def testSucceeds() = ()
      def testFails() { fail() }
    }

    val myOtherSpec = new MyOtherSpec
    val myOtherReporter = new SuiteDurationReporter
    myOtherSpec.run(None, Args(myOtherReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)), Set.empty))
    assert(myOtherReporter.suiteAbortedWasFiredAndHadADuration)
  }

  it("should fire TestPending event for a pending test") {

    class MySpec extends Spec {
      def `this is a pending test` { pending }
    }

    val mySpec = new MySpec
    val myReporter = new PendingReporter
    mySpec.run(None, Args(myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)), Set.empty))
    assert(myReporter.testPendingWasFired)
  }

  class TestWasCalledSpec extends Spec {
    var theTestThisCalled = false
    var theTestThatCalled = false
    var theTestTheOtherCalled = false
    var theTestThisConfigMapWasEmpty = true
    var theTestThatConfigMapWasEmpty = true
    var theTestTheOtherConfigMapWasEmpty = true
    override def withFixture(test: NoArgTest) {
      if (test.configMap.size > 0)
        test.name match {
          case "test$u0020this" => theTestThisConfigMapWasEmpty = false
          case "test$u0020that" => theTestThatConfigMapWasEmpty = false
          case "test$u0020the$u0020other" => theTestTheOtherConfigMapWasEmpty = false
          case _ => throw new Exception("Should never happen")
        }
      test()
    }
    def `test this`() { theTestThisCalled = true }
    def `test that`() { theTestThatCalled = true }
    def `test the other`() { theTestTheOtherCalled = true }
  }
  
  describe("when its execute method is invoked") {

    it("should run all tests, passing an empty config map, if no arguments are passed") {
      val s1 = new TestWasCalledSpec
      s1.execute()
      assert(s1.theTestThisCalled)
      assert(s1.theTestThatCalled)
      assert(s1.theTestTheOtherCalled)
      assert(s1.theTestThisConfigMapWasEmpty)
      assert(s1.theTestThatConfigMapWasEmpty)
      assert(s1.theTestTheOtherConfigMapWasEmpty)
    }

    it("should run just the specified test, passing an empty config map, if a full test name is passed") {
      val s2 = new TestWasCalledSpec
      s2.execute("test this")
      assert(s2.theTestThisCalled)
      assert(!s2.theTestThatCalled)
      assert(!s2.theTestTheOtherCalled)
      assert(s2.theTestThisConfigMapWasEmpty)
      assert(s2.theTestThatConfigMapWasEmpty)
      assert(s2.theTestTheOtherConfigMapWasEmpty)
    }

    it("should pass a non-empty config map into all tests if a just a config map is specified") {
      val s3 = new TestWasCalledSpec
      s3.execute(configMap = Map("s" -> "s"))
      assert(s3.theTestThisCalled)
      assert(s3.theTestThatCalled)
      assert(s3.theTestTheOtherCalled)
      assert(!s3.theTestThisConfigMapWasEmpty)
      assert(!s3.theTestThatConfigMapWasEmpty)
      assert(!s3.theTestTheOtherConfigMapWasEmpty)
    }

    it("should pass a non-empty config map into just the specified test if a full test name and a config map is specified") {
      val s4 = new TestWasCalledSpec
      s4.execute("test this", Map("s" -> "s"))
      assert(s4.theTestThisCalled)
      assert(!s4.theTestThatCalled)
      assert(!s4.theTestTheOtherCalled)
      assert(!s4.theTestThisConfigMapWasEmpty)
      assert(s4.theTestThatConfigMapWasEmpty)
      assert(s4.theTestTheOtherConfigMapWasEmpty)
    }

    it("should run just the specified test, passing an empty config map, if a full test name is passed via a named parameter") {
      val s5 = new TestWasCalledSpec
      s5.execute(testName = "test this")
      assert(s5.theTestThisCalled)
      assert(!s5.theTestThatCalled)
      assert(!s5.theTestTheOtherCalled)
      assert(s5.theTestThisConfigMapWasEmpty)
      assert(s5.theTestThatConfigMapWasEmpty)
      assert(s5.theTestTheOtherConfigMapWasEmpty)
    }

    it("should pass a non-empty config map into just the specified test if a full test name and a config map is specified via named parameters") {
      val s6 = new TestWasCalledSpec
      s6.execute(testName = "test this", configMap = Map("s" -> "s"))
      assert(s6.theTestThisCalled)
      assert(!s6.theTestThatCalled)
      assert(!s6.theTestTheOtherCalled)
      assert(!s6.theTestThisConfigMapWasEmpty)
      assert(s6.theTestThatConfigMapWasEmpty)
      assert(s6.theTestTheOtherConfigMapWasEmpty)
    }
  }
  describe("when its execute method is invoked and a wildcard is passed for the selected test names") {
  

/* Remove if indeed don't need
    class TestWasCalledSpec extends Spec {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      var theTestThisConfigMapWasEmpty = true
      var theTestThatConfigMapWasEmpty = true
      var theTestTheOtherConfigMapWasEmpty = true
      override def withFixture(test: NoArgTest) {
        if (test.configMap.size > 0)
          test.name match {
            case "test$u0020this" => theTestThisConfigMapWasEmpty = false
            case "test$u0020that" => theTestThatConfigMapWasEmpty = false
            case "test$u0020the$u0020other" => theTestTheOtherConfigMapWasEmpty = false
            case _ => throw new Exception("Should never happen")
          }
        test()
      }
      def `test this` { theTestThisCalled = true }
      def `test that` { theTestThatCalled = true }
      def `test the other` { theTestTheOtherCalled = true }
    }
*/

    it("should run all tests, passing in an empty config map, if a wildcard selecting all tests is passed") {
      val s1 = new TestWasCalledSpec
      s1.execute(" th")
      assert(s1.theTestThisCalled)
      assert(s1.theTestThatCalled)
      assert(s1.theTestTheOtherCalled)
      assert(s1.theTestThisConfigMapWasEmpty)
      assert(s1.theTestThatConfigMapWasEmpty)
      assert(s1.theTestTheOtherConfigMapWasEmpty)
    }

    it("should run just one test, passing in an empty config map, if a wildcard selecting only that test is passed") {
      val s2 = new TestWasCalledSpec
      s2.execute(" this")
      assert(s2.theTestThisCalled)
      assert(!s2.theTestThatCalled)
      assert(!s2.theTestTheOtherCalled)
      assert(s2.theTestThisConfigMapWasEmpty)
      assert(s2.theTestThatConfigMapWasEmpty)
      assert(s2.theTestTheOtherConfigMapWasEmpty)
    }

    it("should run all tests, passing in a non-empty config map, if a wildcard selecting all tests and a config map is passed (with config map specified via a named parameter)") {
      val s3 = new TestWasCalledSpec
      s3.execute(" th", configMap = Map("s" -> "s"))
      assert(s3.theTestThisCalled)
      assert(s3.theTestThatCalled)
      assert(s3.theTestTheOtherCalled)
      assert(!s3.theTestThisConfigMapWasEmpty)
      assert(!s3.theTestThatConfigMapWasEmpty)
      assert(!s3.theTestTheOtherConfigMapWasEmpty)
    }

    it("should run all tests, passing in a non-empty config map, if a wildcard selecting all tests and a config map is passed") {
      val s4 = new TestWasCalledSpec
      s4.execute(" th", Map("s" -> "s"))
      assert(s4.theTestThisCalled)
      assert(s4.theTestThatCalled)
      assert(s4.theTestTheOtherCalled)
      assert(!s4.theTestThisConfigMapWasEmpty)
      assert(!s4.theTestThatConfigMapWasEmpty)
      assert(!s4.theTestTheOtherConfigMapWasEmpty)
    }

    it("should run all tests, passing in an empty config map, if a wildcard selecting all tests is passed with a named parameter") {
      val s5 = new TestWasCalledSpec
      s5.execute(testName = " th")
      assert(s5.theTestThisCalled)
      assert(s5.theTestThatCalled)
      assert(s5.theTestTheOtherCalled)
      assert(s5.theTestThisConfigMapWasEmpty)
      assert(s5.theTestThatConfigMapWasEmpty)
      assert(s5.theTestTheOtherConfigMapWasEmpty)
    }

    it("should run all tests, passing in a non-empty config map, if a wildcard selecting all tests and a config map are passed with named parameters") {
      val s6 = new TestWasCalledSpec
      s6.execute(testName = " this", configMap = Map("s" -> "s"))
      assert(s6.theTestThisCalled)
      assert(!s6.theTestThatCalled)
      assert(!s6.theTestTheOtherCalled)
      assert(!s6.theTestThisConfigMapWasEmpty)
      assert(s6.theTestThatConfigMapWasEmpty)
      assert(s6.theTestTheOtherConfigMapWasEmpty)
    }
  }

  it("should return the test names in alphabetical order from testNames even if the encoded name would sort in the opposite order") {

    // + comes before -
    // but $plus comes after $minus
    class ASpec extends Spec {

      def `test: the + operator should add` {
        val sum = 1 + 1
        assert(sum === 2)
      }

      def `test: the - operator should subtract` {
        val diff = 4 - 1
        assert(diff === 3)
      }
    }

    val a = new ASpec
    val expectedTestNames = List("" +
      "test$colon$u0020the$u0020$plus$u0020operator$u0020should$u0020add",
      "test$colon$u0020the$u0020$minus$u0020operator$u0020should$u0020subtract"
    )
    assert(a.testNames.iterator.toList === expectedTestNames)
  }

  it("should send a defined decoded suite name for a class whose name is given in backticks") {
    expectResult("My Spec") { (new My$u0020Spec).decodedSuiteName.get }
    expectResult(None) { (new NormalSpec).decodedSuiteName }
  }

  it("should send defined decoded test names") {
 
/*
    class NormalSpec extends Spec {
      def testSucceed() = {}
      def testFail() = { fail }
      def testPending() = { pending }
      @Ignore
      def testIgnore() = {}
    }

    val normalSpec = new NormalSpec
    val normalReporter = new EventRecordingReporter
    normalSpec.run(None, Args(normalReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)), Set.empty))
    val normalEventList:List[Event] = normalReporter.eventsReceived
    expectResult(7) { normalEventList.size }
    normalEventList.foreach {event =>
      event match {
        case testStarting:TestStarting => 
          expectResult(None) { testStarting.decodedTestName }
          expectResult(None) { testStarting.decodedSuiteName }
        case testSucceed:TestSucceeded => 
          expectResult("testSucceed") { testSucceed.testName }
          expectResult(None) { testSucceed.decodedTestName }
        case testFail:TestFailed =>
          expectResult("testFail") { testFail.testName }
          expectResult(None) { testFail.decodedTestName }
        case testPending:TestPending =>
          expectResult("testPending") { testPending.testName }
          expectResult(None) { testPending.decodedTestName }
        case testIgnore:TestIgnored => 
          expectResult("testIgnore") { testIgnore.testName }
          expectResult(None) { testIgnore.decodedTestName }
        case _ =>
      }
    }
*/
    
    class DecodedSpec extends Spec {
      def `test Succeed`() {}
      def `test Fail`() = { fail }
      def `test Pending`() = { pending }
      @Ignore
      def `test Ignore`() = {}
    }
    
    val decodedSpec = new DecodedSpec
    val decodedReporter = new EventRecordingReporter
    decodedSpec.run(None, Args(decodedReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)), Set.empty))
    val decodedEventList:List[Event] = decodedReporter.eventsReceived
    expectResult(7) { decodedEventList.size }
    decodedEventList.foreach {event =>
      event match {
        case testStarting:TestStarting => 
          testStarting.decodedTestName match {
            case Some(name) => assert(name.length() > 0, "decodedTestName should not be empty.")
            case None => fail("decodedTestName should not be empty.")
          }
          expectResult(None) { testStarting.decodedSuiteName }
        case testSucceed:TestSucceeded => 
          expectResult("test$u0020Succeed") { testSucceed.testName }
          expectResult(Some("test Succeed")) { testSucceed.decodedTestName }
        case testFail:TestFailed =>
          expectResult("test$u0020Fail") { testFail.testName }
          expectResult(Some("test Fail")) { testFail.decodedTestName }
        case testPending:TestPending =>
          expectResult("test$u0020Pending") { testPending.testName }
          expectResult(Some("test Pending")) { testPending.decodedTestName }
        case testIgnore:TestIgnored => 
          expectResult("test$u0020Ignore") { testIgnore.testName }
          expectResult(Some("test Ignore")) { testIgnore.decodedTestName }
        case _ =>
      }
    }
  }

  def testTestTags() {
    class TagSpec extends Spec {  
      def testNoTagMethod() {}
      @SlowAsMolasses
      def testTagMethod() {}
    }
    val testTags = new TagSpec().tags
    assert(testTags.size === 1)
    val tagSet = testTags.getOrElse("testTagMethod", null)
    assert(tagSet != null)
    assert(tagSet.size === 1)
    assert(tagSet.toList(0) === classOf[SlowAsMolasses].getName)
  }
  
  describe("when annotations are applied at the class level") {
    it("should propate those annotations to all tests in the class") {
    
      class NoTagSpec extends Spec
      @Ignore
      class IgnoreSpec extends Spec {
        def `test method 1` {}
        def `test method 2` {}
        def `test method 3` {}
      }
      @SlowAsMolasses
      class SlowAsMolassesSpec extends Spec
      @FastAsLight
      class FastAsLightSpec extends Spec
    
      class MasterSpec extends Spec {
        override def nestedSuites = Vector(new NoTagSpec(), new IgnoreSpec(), new SlowAsMolassesSpec(), new FastAsLightSpec())
        override def runNestedSuites(args: Args) {
          super.runNestedSuites(args)
        }
      }
    
      class CounterDistributor extends Distributor {
        var count = 0
        def apply(suite: Suite, args: Args) {
          count += 1
        }
        def apply(suite: Suite, tracker: Tracker) {
          count += 1
        }
      }

      val masterSpec = new MasterSpec()

      val defaultFilter = new Filter(None, Set.empty)
      val defaultReporter = new EventRecordingReporter
      masterSpec.runNestedSuites(Args(defaultReporter, new Stopper {}, defaultFilter, Map.empty, None, new Tracker(new Ordinal(99)), Set.empty))
      assert(defaultReporter.suiteStartingEventsReceived.size === 4)
      assert(defaultReporter.testIgnoredEventsReceived.size === 3)
      val defaultReporterDist = new EventRecordingReporter
      val defaultDistributor = new CounterDistributor
      masterSpec.runNestedSuites(Args(defaultReporterDist, new Stopper {}, defaultFilter, Map.empty, Some(defaultDistributor), new Tracker(new Ordinal(99)), Set.empty))
      assert(defaultDistributor.count === 4)

      val includeFilter = new Filter(Some(Set("org.scalatest.FastAsLight")), Set.empty)
      val includeReporter = new EventRecordingReporter
      masterSpec.runNestedSuites(Args(includeReporter, new Stopper {}, includeFilter, Map.empty, None, new Tracker(new Ordinal(99)), Set.empty))
      assert(includeReporter.suiteStartingEventsReceived.size === 4) 
      assert(includeReporter.testIgnoredEventsReceived.size === 0) 
      val includeReporterDist = new EventRecordingReporter
      val includeDistributor = new CounterDistributor
      masterSpec.runNestedSuites(Args(includeReporterDist, new Stopper {}, includeFilter, Map.empty, Some(includeDistributor), new Tracker(new Ordinal(99)), Set.empty))
      assert(includeDistributor.count === 4) 

      val excludeFilter = new Filter(None, Set("org.scalatest.SlowAsMolasses"))
      val excludeReporter = new EventRecordingReporter
      masterSpec.runNestedSuites(Args(excludeReporter, new Stopper {}, excludeFilter, Map.empty, None, new Tracker(new Ordinal(99)), Set.empty))
      assert(excludeReporter.suiteStartingEventsReceived.size === 4)
      assert(excludeReporter.testIgnoredEventsReceived.size === 3)
      val excludeReporterDist = new EventRecordingReporter
      val excludeDistributor = new CounterDistributor
      masterSpec.runNestedSuites(Args(excludeReporterDist, new Stopper {}, excludeFilter, Map.empty, Some(excludeDistributor), new Tracker(new Ordinal(99)), Set.empty))
      assert(excludeDistributor.count === 4)
    }
  }
  
  describe("when its expectedTestCount method is invoked") {
    it("should return a count that takes into 'account' the passed filter") {
      class NoTagSpec extends Spec {
        def `test method 1` {}
        def `test method 2` {}
        def `test method 3` {}
      }
      @Ignore
      class IgnoreSpec extends Spec {
        def `test method 1` {}
        def `test method 2` {}
        def `test method 3` {}
      }
      @SlowAsMolasses
      class SlowAsMolassesSpec extends Spec {
        def `test method 1` {}
        def `test method 2` {}
        def `test method 3` {}
      }
      @FastAsLight
      class FastAsLightSpec extends Spec {
        def `test method 1` {}
        def `test method 2` {}
        def `test method 3` {}
      }
    
      class MasterSpec extends Spec {
        override def nestedSuites = Vector(new NoTagSpec(), new IgnoreSpec(), new SlowAsMolassesSpec(), new FastAsLightSpec())
        override def runNestedSuites(args: Args) {
          super.runNestedSuites(args)
        }
      }
    
      val masterSpec = new MasterSpec()
      assert(masterSpec.expectedTestCount(new Filter(None, Set.empty)) === 9)
      assert(masterSpec.expectedTestCount(new Filter(Some(Set("org.scalatest.FastAsLight")), Set.empty)) === 3)
      assert(masterSpec.expectedTestCount(new Filter(None, Set("org.scalatest.FastAsLight"))) === 6)
      assert(masterSpec.expectedTestCount(new Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set.empty)) === 3)
      assert(masterSpec.expectedTestCount(new Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 6)
    }
  }
  
  describe("when its rerunner method is invoked") {
    it("should provide the fully qualified name of a class that could be used to rerun the suite, wrapped in a Some, or None, if can't be rerun") {
      assert(new NormalSpec().rerunner.get === classOf[NormalSpec].getName)
      assert(new WrappedSpec(Map.empty).rerunner.get === classOf[WrappedSpec].getName)
      assert(new NotAccessibleSpec("test").rerunner === None)
    }
  }
  
  it("should run only chosen styles, if specified, and throw an exception from run if a non-chosen style is attempted to be run") {

    class SimpleSpec extends Spec {
      def `test method 1` {}
      def `test method 2` {}
      def `test method 3` {}
    }
    
    val simpleSpec = new SimpleSpec()
    simpleSpec.run(None, Args(SilentReporter, new Stopper {}, Filter(), Map.empty, None, new Tracker, Set.empty))
    simpleSpec.run(None, Args(SilentReporter, new Stopper {}, Filter(), Map("org.scalatest.ChosenStyles" -> Set("org.scalatest.Spec")), None, new Tracker, Set.empty))
    val caught =
      intercept[NotAllowedException] {
        simpleSpec.run(None, Args(SilentReporter, new Stopper {}, Filter(), Map("org.scalatest.ChosenStyles" -> Set("org.scalatest.FunSpec")), None, new Tracker, Set.empty))
      }
    import OptionValues._
    assert(caught.message.value === Resources("notTheChosenStyle", "org.scalatest.Spec", "org.scalatest.FunSpec"))
    val caught2 =
      intercept[NotAllowedException] {
        simpleSpec.run(None, Args(SilentReporter, new Stopper {}, Filter(), Map("org.scalatest.ChosenStyles" -> Set("org.scalatest.FunSpec", "org.scalatest.FreeSpec")), None, new Tracker, Set.empty))
      }
    assert(caught2.message.value === Resources("notOneOfTheChosenStyles", "org.scalatest.Spec", Suite.makeListForHumans(Vector("org.scalatest.FunSpec", "org.scalatest.FreeSpec"))))
    val caught3 =
      intercept[NotAllowedException] {
        simpleSpec.run(None, Args(SilentReporter, new Stopper {}, Filter(), Map("org.scalatest.ChosenStyles" -> Set("org.scalatest.FunSpec", "org.scalatest.FreeSpec", "org.scalatest.FlatSpec")), None, new Tracker, Set.empty))
      }
    assert(caught3.message.value === Resources("notOneOfTheChosenStyles", "org.scalatest.Spec", Suite.makeListForHumans(Vector("org.scalatest.FunSpec", "org.scalatest.FreeSpec", "org.scalatest.FlatSpec"))))
  }
  
  describe("when a test fails") {
    it("should send proper stack depth information") {
      class TestSpec extends Spec {
        def `test failure`() {
          assert(1 === 2)
        }
      }
      val rep = new EventRecordingReporter
      val s1 = new TestSpec
      s1.run(None, Args(rep, new Stopper {}, Filter(), Map(), None, new Tracker, Set.empty))
      assert(rep.testFailedEventsReceived.size === 1)
      assert(rep.testFailedEventsReceived(0).throwable.get.asInstanceOf[TestFailedException].failedCodeFileName.get === "SpecSpec.scala")
      assert(rep.testFailedEventsReceived(0).throwable.get.asInstanceOf[TestFailedException].failedCodeLineNumber.get === thisLineNumber - 8)
    }
  }
}

@DoNotDiscover
class `My Spec` extends Spec {}
@DoNotDiscover
class NormalSpec extends Spec
@DoNotDiscover
@WrapWith(classOf[ConfigMapWrapperSuite]) 
class WrappedSpec(configMap: Map[_, _]) extends Spec
@DoNotDiscover
class NotAccessibleSpec(name: String) extends Spec

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
import org.scalatest.events.InfoProvided
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
}

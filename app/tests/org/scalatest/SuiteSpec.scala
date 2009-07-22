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

import collection.immutable.TreeSet
import org.scalatest.events._

class SuiteSpec extends Spec with PrivateMethodTester with SharedHelpers {

  describe("The simpleNameForTest method") {
    it("should return the correct test simple name with or without Informer") {
      val simpleNameForTest = PrivateMethod[String]('simpleNameForTest)
      assert((Suite invokePrivate simpleNameForTest("testThis")) === "testThis")
      assert((Suite invokePrivate simpleNameForTest("testThis(Informer)")) === "testThis")
      assert((Suite invokePrivate simpleNameForTest("test(Informer)")) === "test")
      assert((Suite invokePrivate simpleNameForTest("test")) === "test")
    }
  }

  describe("A Suite") {
    it("should return the test names in alphabetical order from testNames") {
      val a = new Suite {
        def testThis() {}
        def testThat() {}
      }

      expect(List("testThat", "testThis")) {
        a.testNames.elements.toList
      }

      val b = new Suite {}

      expect(List[String]()) {
        b.testNames.elements.toList
      }

      val c = new Suite {
        def testThat() {}
        def testThis() {}
      }

      expect(List("testThat", "testThis")) {
        c.testNames.elements.toList
      }
    }
    
    it("should return the proper testNames for test methods whether or not they take an Informer") {

      val a = new Suite {
        def testThis() = ()
        def testThat(info: Informer) = ()
      }
      assert(a.testNames === TreeSet("testThat(Informer)", "testThis"))

      val b = new Suite {}
      assert(b.testNames === TreeSet[String]())
    }

    it("should return a correct tags map from the tags method") {

      val a = new Suite {
        @Ignore
        def testThis() = ()
        def testThat(info: Informer) = ()
      }

      assert(a.tags === Map("testThis" -> Set("org.scalatest.Ignore")))

      val b = new Suite {
        def testThis() = ()
        @Ignore
        def testThat(info: Informer) = ()
      }

      assert(b.tags === Map("testThat(Informer)" -> Set("org.scalatest.Ignore")))

      val c = new Suite {
        @Ignore
        def testThis() = ()
        @Ignore
        def testThat(info: Informer) = ()
      }

      assert(c.tags === Map("testThis" -> Set("org.scalatest.Ignore"), "testThat(Informer)" -> Set("org.scalatest.Ignore")))

      val d = new Suite {
        @SlowAsMolasses
        def testThis() = ()
        @SlowAsMolasses
        @Ignore
        def testThat(info: Informer) = ()
      }

      assert(d.tags === Map("testThis" -> Set("org.scalatest.SlowAsMolasses"), "testThat(Informer)" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses")))

      val e = new Suite {}
      assert(e.tags === Map())
    }

    class TestWasCalledSuite extends Suite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      def testThis() { theTestThisCalled = true }
      def testThat() { theTestThatCalled = true }
    }

    it("should execute all tests when run is called with testName None") {

      val b = new TestWasCalledSuite
      b.run(None, SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(b.theTestThisCalled)
      assert(b.theTestThatCalled)
    }

    it("should execute one test when run is called with a defined testName") {

      val a = new TestWasCalledSuite
      a.run(Some("testThis"), SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(a.theTestThisCalled)
      assert(!a.theTestThatCalled)
    }

    it("should report as ignored, ant not run, tests marked ignored") {

      val a = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        def testThis() { theTestThisCalled = true }
        def testThat(info: Informer) { theTestThatCalled = true }
      }

      val repA = new TestIgnoredTrackingReporter
      a.run(None, repA, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(!repA.testIgnoredReceived)
      assert(a.theTestThisCalled)
      assert(a.theTestThatCalled)

      val b = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @Ignore
        def testThis() { theTestThisCalled = true }
        def testThat(info: Informer) { theTestThatCalled = true }
      }

      val repB = new TestIgnoredTrackingReporter
      b.run(None, repB, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repB.testIgnoredReceived)
      assert(repB.lastEvent.isDefined)
      assert(repB.lastEvent.get.testName endsWith "testThis")
      assert(!b.theTestThisCalled)
      assert(b.theTestThatCalled)

      val c = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        def testThis() { theTestThisCalled = true }
        @Ignore
        def testThat(info: Informer) { theTestThatCalled = true }
      }

      val repC = new TestIgnoredTrackingReporter
      c.run(None, repC, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repC.testIgnoredReceived)
      assert(repC.lastEvent.isDefined)
      assert(repC.lastEvent.get.testName endsWith "testThat(Informer)", repC.lastEvent.get.testName)
      assert(c.theTestThisCalled)
      assert(!c.theTestThatCalled)

      val d = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @Ignore
        def testThis() { theTestThisCalled = true }
        @Ignore
        def testThat(info: Informer) { theTestThatCalled = true }
      }

      val repD = new TestIgnoredTrackingReporter
      d.run(None, repD, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repD.testIgnoredReceived)
      assert(repD.lastEvent.isDefined)
      assert(repD.lastEvent.get.testName endsWith "testThis") // last because run alphabetically
      assert(!d.theTestThisCalled)
      assert(!d.theTestThatCalled)
    }
    
    it("should run a test marked as ignored if run is invoked with that testName") {

      val e = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @Ignore
        def testThis() { theTestThisCalled = true }
        def testThat(info: Informer) { theTestThatCalled = true }
      }

      val repE = new TestIgnoredTrackingReporter
      e.run(Some("testThis"), repE, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(!repE.testIgnoredReceived)
      assert(e.theTestThisCalled)
      assert(!e.theTestThatCalled)
    }

    it("should throw IllegalArgumentException if run is passed a testName that does not exist") {
      val suite = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        def testThis() { theTestThisCalled = true }
        def testThat(info: Informer) { theTestThatCalled = true }
      }

      intercept[IllegalArgumentException] {
        // Here, they forgot that the name is actually testThis(Fixture)
        suite.run(Some("misspelled"), SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
      }
    }

    it("should run only those tests selected by the tags to include and exclude sets") {

      // Nothing is excluded
      val a = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @SlowAsMolasses
        def testThis() { theTestThisCalled = true }
        def testThat(info: Informer) { theTestThatCalled = true }
      }
      val repA = new TestIgnoredTrackingReporter
      a.run(None, repA, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(!repA.testIgnoredReceived)
      assert(a.theTestThisCalled)
      assert(a.theTestThatCalled)

      // SlowAsMolasses is included, one test should be excluded
      val b = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @SlowAsMolasses
        def testThis() { theTestThisCalled = true }
        def testThat(info: Informer) { theTestThatCalled = true }
      }
      val repB = new TestIgnoredTrackingReporter
      b.run(None, repB, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), Map(), None, new Tracker)
      assert(!repB.testIgnoredReceived)
      assert(b.theTestThisCalled)
      assert(!b.theTestThatCalled)

      // SlowAsMolasses is included, and both tests should be included
      val c = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @SlowAsMolasses
        def testThis() { theTestThisCalled = true }
        @SlowAsMolasses
        def testThat(info: Informer) { theTestThatCalled = true }
      }
      val repC = new TestIgnoredTrackingReporter
      c.run(None, repB, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), Map(), None, new Tracker)
      assert(!repC.testIgnoredReceived)
      assert(c.theTestThisCalled)
      assert(c.theTestThatCalled)

      // SlowAsMolasses is included. both tests should be included but one ignored
      val d = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        @Ignore
        @SlowAsMolasses
        def testThis() { theTestThisCalled = true }
        @SlowAsMolasses
        def testThat(info: Informer) { theTestThatCalled = true }
      }
      val repD = new TestIgnoredTrackingReporter
      d.run(None, repD, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.Ignore")), Map(), None, new Tracker)
      assert(repD.testIgnoredReceived)
      assert(!d.theTestThisCalled)
      assert(d.theTestThatCalled)

      // SlowAsMolasses included, FastAsLight excluded
      val e = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @FastAsLight
        @SlowAsMolasses
        def testThis() { theTestThisCalled = true }
        @SlowAsMolasses
        def testThat(info: Informer) { theTestThatCalled = true }
        def testTheOther(info: Informer) { theTestTheOtherCalled = true }
      }
      val repE = new TestIgnoredTrackingReporter
      e.run(None, repE, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
                Map(), None, new Tracker)
      assert(!repE.testIgnoredReceived)
      assert(!e.theTestThisCalled)
      assert(e.theTestThatCalled)
      assert(!e.theTestTheOtherCalled)

      // An Ignored test that was both included and excluded should not generate a TestIgnored event
      val f = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @Ignore
        @FastAsLight
        @SlowAsMolasses
        def testThis() { theTestThisCalled = true }
        @SlowAsMolasses
        def testThat(info: Informer) { theTestThatCalled = true }
        def testTheOther(info: Informer) { theTestTheOtherCalled = true }
      }
      val repF = new TestIgnoredTrackingReporter
      f.run(None, repF, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
                Map(), None, new Tracker)
      assert(!repF.testIgnoredReceived)
      assert(!f.theTestThisCalled)
      assert(f.theTestThatCalled)
      assert(!f.theTestTheOtherCalled)

      // An Ignored test that was not included should not generate a TestIgnored event
      val g = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @FastAsLight
        @SlowAsMolasses
        def testThis() { theTestThisCalled = true }
        @SlowAsMolasses
        def testThat(info: Informer) { theTestThatCalled = true }
        @Ignore
        def testTheOther(info: Informer) { theTestTheOtherCalled = true }
      }
      val repG = new TestIgnoredTrackingReporter
      g.run(None, repG, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
                Map(), None, new Tracker)
      assert(!repG.testIgnoredReceived)
      assert(!g.theTestThisCalled)
      assert(g.theTestThatCalled)
      assert(!g.theTestTheOtherCalled)

      // No tagsToInclude set, FastAsLight excluded
      val h = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @FastAsLight
        @SlowAsMolasses
        def testThis() { theTestThisCalled = true }
        @SlowAsMolasses
        def testThat(info: Informer) { theTestThatCalled = true }
        def testTheOther(info: Informer) { theTestTheOtherCalled = true }
      }
      val repH = new TestIgnoredTrackingReporter
      h.run(None, repH, new Stopper {}, Filter(None, Set("org.scalatest.FastAsLight")), Map(), None, new Tracker)
      assert(!repH.testIgnoredReceived)
      assert(!h.theTestThisCalled)
      assert(h.theTestThatCalled)
      assert(h.theTestTheOtherCalled)

      // No tagsToInclude set, SlowAsMolasses excluded
      val i = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @FastAsLight
        @SlowAsMolasses
        def testThis() { theTestThisCalled = true }
        @SlowAsMolasses
        def testThat(info: Informer) { theTestThatCalled = true }
        def testTheOther(info: Informer) { theTestTheOtherCalled = true }
      }
      val repI = new TestIgnoredTrackingReporter
      i.run(None, repI, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses")), Map(), None, new Tracker)
      assert(!repI.testIgnoredReceived)
      assert(!i.theTestThisCalled)
      assert(!i.theTestThatCalled)
      assert(i.theTestTheOtherCalled)

      // No tagsToInclude set, SlowAsMolasses excluded, TestIgnored should not be received on excluded ones
      val j = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @Ignore
        @FastAsLight
        @SlowAsMolasses
        def testThis() { theTestThisCalled = true }
        @Ignore
        @SlowAsMolasses
        def testThat(info: Informer) { theTestThatCalled = true }
        def testTheOther(info: Informer) { theTestTheOtherCalled = true }
      }
      val repJ = new TestIgnoredTrackingReporter
      j.run(None, repJ, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses")), Map(), None, new Tracker)
      assert(!repI.testIgnoredReceived)
      assert(!j.theTestThisCalled)
      assert(!j.theTestThatCalled)
      assert(j.theTestTheOtherCalled)

      // Same as previous, except Ignore specifically mentioned in excludes set
      val k = new Suite {
        var theTestThisCalled = false
        var theTestThatCalled = false
        var theTestTheOtherCalled = false
        @Ignore
        @FastAsLight
        @SlowAsMolasses
        def testThis() { theTestThisCalled = true }
        @Ignore
        @SlowAsMolasses
        def testThat(info: Informer) { theTestThatCalled = true }
        @Ignore
        def testTheOther(info: Informer) { theTestTheOtherCalled = true }
      }
      val repK = new TestIgnoredTrackingReporter
      k.run(None, repK, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore")), Map(), None, new Tracker)
      assert(repK.testIgnoredReceived)
      assert(!k.theTestThisCalled)
      assert(!k.theTestThatCalled)
      assert(!k.theTestTheOtherCalled)
    }
  }
}


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

import org.scalatest.events._

class SpecSuite extends FunSpec with PrivateMethodTester with SharedHelpers with SeveredStackTraces {

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
    expectResult("My Spec") { new My$u0020Spec().decodedSuiteName.get }
    expectResult(None) { new SpecSuite().decodedSuiteName }
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
      assert(rep.testFailedEventsReceived(0).throwable.get.asInstanceOf[TestFailedException].failedCodeFileName.get === "SpecSuite.scala")
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

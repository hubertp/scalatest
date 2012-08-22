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

class SpecSuite extends Spec with PrivateMethodTester with SharedHelpers with SeveredStackTraces {

  def `test: Spec should discover method names and tags using deprecated Informer form` {

    val a = new Spec {
      def testNames(info: Informer): Unit = ()
    }
    assert(a.expectedTestCount(Filter()) === 1)
    val tnResult: Set[String] = a.testNames
    val gResult: Map[String, Set[String]] = a.tags
    assert(tnResult.size === 1)
    assert(gResult.keySet.size === 0)
  }
  
  // TODO: ensure spec aborts if same name is used with and without communicator
  def `test: Spec should discover method names and tags` {

    val a = new Spec {
      def testNames(r: Rep): Unit = ()
    }
    assert(a.expectedTestCount(Filter()) === 1)
    val tnResult: Set[String] = a.testNames
    val gResult: Map[String, Set[String]] = a.tags
    assert(tnResult.size === 1)
    assert(gResult.keySet.size === 0)
  }
  
  def `test: test methods with no tags should not show up in tags map` {
    
    val a = new Spec {
      def `test: not tagged` = ()
    }
    assert(a.tags.keySet.size === 0)
  }
  
  def `test: test methods that return non-Unit should be discovered using deprecated Informer form` {
    val a = new Spec {
      def testThis(): Int = 1
      def testThat(info: Informer): String = "hi"
    }
    assert(a.expectedTestCount(Filter()) === 2)
    assert(a.testNames.size === 2)
    assert(a.tags.keySet.size === 0)
  }
  
  def `test: test methods that return non-Unit should be discovered` {
    val a = new Spec {
      def `test: this`: Int = 1
      def `test: that`(r: Rep): String = "hi"
    }
    assert(a.expectedTestCount(Filter()) === 2)
    assert(a.testNames.size === 2)
    assert(a.tags.keySet.size === 0)
  }
  
  def `test: overloaded test methods should be discovered using deprecated Informer form` {
    val a = new Spec {
      def testThis() = ()
      def testThis(info: Informer) = ()
    }
    assert(a.expectedTestCount(Filter()) === 2)
    assert(a.testNames.size === 2)
    assert(a.tags.keySet.size === 0)
  }
  
  def testThatInterceptCatchesSubtypes() {
    class MyException extends RuntimeException
    class MyExceptionSubClass extends MyException
    intercept[MyException] {
      throw new MyException
      new AnyRef // This is needed because right now Nothing doesn't overload as an Any
    }
    intercept[MyException] {
      throw new MyExceptionSubClass
      new AnyRef // This is needed because right now Nothing doesn't overload as an Any
    }
    // Try with a trait
    trait MyTrait {
      def someRandomMethod() {}
    }
    class AnotherException extends RuntimeException with MyTrait
    val caught = intercept[MyTrait] {
      throw new AnotherException
      new AnyRef // This is needed because right now Nothing doesn't overload as an Any
    }
    // Make sure the result type is the type passed in, so I can 
    // not cast and still invoke any method on it I want
    caught.someRandomMethod()
  }
  
  def testThatInterceptReturnsTheCaughtException() {
    val e = new RuntimeException
    val result = intercept[RuntimeException] {
      throw e
      new AnyRef // This is needed because right now Nothing doesn't overload as an Any
    }
    assert(result eq e)
  }
  
  def testTestDurations() {

    class MySpec extends Spec {
      def testSucceeds() = ()
      def testFails() { fail() }
    }

    val mySpec = new MySpec
    val myReporter = new TestDurationReporter
    mySpec.run(None, Args(myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)), Set.empty))
    assert(myReporter.testSucceededWasFiredAndHadADuration)
    assert(myReporter.testFailedWasFiredAndHadADuration)
  }
  
  def testSpecDurations() {

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

    class SpecThatAborts extends Spec {
      override def run(testName: Option[String], args: Args) {
        throw new RuntimeException("Aborting for testing purposes")
      }
    }

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
  
  def testPending() {

    class MySpec extends Spec {
      def testPending() { pending }
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
          case "testThis" => theTestThisConfigMapWasEmpty = false
          case "testThat" => theTestThatConfigMapWasEmpty = false
          case "testTheOther" => theTestTheOtherConfigMapWasEmpty = false
          case _ => throw new Exception("Should never happen")
        }
      test()
    }
    def testThis() { theTestThisCalled = true }
    def testThat() { theTestThatCalled = true }
    def testTheOther() { theTestTheOtherCalled = true }
  }
  
  def testExecute() {

    val s1 = new TestWasCalledSpec
    s1.execute()
    assert(s1.theTestThisCalled)
    assert(s1.theTestThatCalled)
    assert(s1.theTestTheOtherCalled)
    assert(s1.theTestThisConfigMapWasEmpty)
    assert(s1.theTestThatConfigMapWasEmpty)
    assert(s1.theTestTheOtherConfigMapWasEmpty)

    val s2 = new TestWasCalledSpec
    s2.execute("testThis")
    assert(s2.theTestThisCalled)
    assert(!s2.theTestThatCalled)
    assert(!s2.theTestTheOtherCalled)
    assert(s2.theTestThisConfigMapWasEmpty)
    assert(s2.theTestThatConfigMapWasEmpty)
    assert(s2.theTestTheOtherConfigMapWasEmpty)

    val s3 = new TestWasCalledSpec
    s3.execute(configMap = Map("s" -> "s"))
    assert(s3.theTestThisCalled)
    assert(s3.theTestThatCalled)
    assert(s3.theTestTheOtherCalled)
    assert(!s3.theTestThisConfigMapWasEmpty)
    assert(!s3.theTestThatConfigMapWasEmpty)
    assert(!s3.theTestTheOtherConfigMapWasEmpty)

    val s4 = new TestWasCalledSpec
    s4.execute("testThis", Map("s" -> "s"))
    assert(s4.theTestThisCalled)
    assert(!s4.theTestThatCalled)
    assert(!s4.theTestTheOtherCalled)
    assert(!s4.theTestThisConfigMapWasEmpty)
    assert(s4.theTestThatConfigMapWasEmpty)
    assert(s4.theTestTheOtherConfigMapWasEmpty)

    val s5 = new TestWasCalledSpec
    s5.execute(testName = "testThis")
    assert(s5.theTestThisCalled)
    assert(!s5.theTestThatCalled)
    assert(!s5.theTestTheOtherCalled)
    assert(s5.theTestThisConfigMapWasEmpty)
    assert(s5.theTestThatConfigMapWasEmpty)
    assert(s5.theTestTheOtherConfigMapWasEmpty)

    val s6 = new TestWasCalledSpec
    s6.execute(testName = "testThis", configMap = Map("s" -> "s"))
    assert(s6.theTestThisCalled)
    assert(!s6.theTestThatCalled)
    assert(!s6.theTestTheOtherCalled)
    assert(!s6.theTestThisConfigMapWasEmpty)
    assert(s6.theTestThatConfigMapWasEmpty)
    assert(s6.theTestTheOtherConfigMapWasEmpty)
  }
  
  def `test: execute should use dynamic tagging to enable Doenitz wildcards for non-encoded test names` {
    val s1 = new TestWasCalledSpec
    s1.execute("Th")
    assert(s1.theTestThisCalled)
    assert(s1.theTestThatCalled)
    assert(s1.theTestTheOtherCalled)
    assert(s1.theTestThisConfigMapWasEmpty)
    assert(s1.theTestThatConfigMapWasEmpty)
    assert(s1.theTestTheOtherConfigMapWasEmpty)

    val s2 = new TestWasCalledSpec
    s2.execute("This")
    assert(s2.theTestThisCalled)
    assert(!s2.theTestThatCalled)
    assert(!s2.theTestTheOtherCalled)
    assert(s2.theTestThisConfigMapWasEmpty)
    assert(s2.theTestThatConfigMapWasEmpty)
    assert(s2.theTestTheOtherConfigMapWasEmpty)

    val s3 = new TestWasCalledSpec
    s3.execute("Th", configMap = Map("s" -> "s"))
    assert(s3.theTestThisCalled)
    assert(s3.theTestThatCalled)
    assert(s3.theTestTheOtherCalled)
    assert(!s3.theTestThisConfigMapWasEmpty)
    assert(!s3.theTestThatConfigMapWasEmpty)
    assert(!s3.theTestTheOtherConfigMapWasEmpty)

    val s4 = new TestWasCalledSpec
    s4.execute("Th", Map("s" -> "s"))
    assert(s4.theTestThisCalled)
    assert(s4.theTestThatCalled)
    assert(s4.theTestTheOtherCalled)
    assert(!s4.theTestThisConfigMapWasEmpty)
    assert(!s4.theTestThatConfigMapWasEmpty)
    assert(!s4.theTestTheOtherConfigMapWasEmpty)

    val s5 = new TestWasCalledSpec
    s5.execute(testName = "Th")
    assert(s5.theTestThisCalled)
    assert(s5.theTestThatCalled)
    assert(s5.theTestTheOtherCalled)
    assert(s5.theTestThisConfigMapWasEmpty)
    assert(s5.theTestThatConfigMapWasEmpty)
    assert(s5.theTestTheOtherConfigMapWasEmpty)

    val s6 = new TestWasCalledSpec
    s6.execute(testName = "This", configMap = Map("s" -> "s"))
    assert(s6.theTestThisCalled)
    assert(!s6.theTestThatCalled)
    assert(!s6.theTestTheOtherCalled)
    assert(!s6.theTestThisConfigMapWasEmpty)
    assert(s6.theTestThatConfigMapWasEmpty)
    assert(s6.theTestTheOtherConfigMapWasEmpty)
  }
  
  def `test: execute should use dynamic tagging to enable Doenitz wildcards for encoded test names` {

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

    val s1 = new TestWasCalledSpec
    s1.execute(" th")
    assert(s1.theTestThisCalled)
    assert(s1.theTestThatCalled)
    assert(s1.theTestTheOtherCalled)
    assert(s1.theTestThisConfigMapWasEmpty)
    assert(s1.theTestThatConfigMapWasEmpty)
    assert(s1.theTestTheOtherConfigMapWasEmpty)

    val s2 = new TestWasCalledSpec
    s2.execute(" this")
    assert(s2.theTestThisCalled)
    assert(!s2.theTestThatCalled)
    assert(!s2.theTestTheOtherCalled)
    assert(s2.theTestThisConfigMapWasEmpty)
    assert(s2.theTestThatConfigMapWasEmpty)
    assert(s2.theTestTheOtherConfigMapWasEmpty)

    val s3 = new TestWasCalledSpec
    s3.execute(" th", configMap = Map("s" -> "s"))
    assert(s3.theTestThisCalled)
    assert(s3.theTestThatCalled)
    assert(s3.theTestTheOtherCalled)
    assert(!s3.theTestThisConfigMapWasEmpty)
    assert(!s3.theTestThatConfigMapWasEmpty)
    assert(!s3.theTestTheOtherConfigMapWasEmpty)

    val s4 = new TestWasCalledSpec
    s4.execute(" th", Map("s" -> "s"))
    assert(s4.theTestThisCalled)
    assert(s4.theTestThatCalled)
    assert(s4.theTestTheOtherCalled)
    assert(!s4.theTestThisConfigMapWasEmpty)
    assert(!s4.theTestThatConfigMapWasEmpty)
    assert(!s4.theTestTheOtherConfigMapWasEmpty)

    val s5 = new TestWasCalledSpec
    s5.execute(testName = " th")
    assert(s5.theTestThisCalled)
    assert(s5.theTestThatCalled)
    assert(s5.theTestTheOtherCalled)
    assert(s5.theTestThisConfigMapWasEmpty)
    assert(s5.theTestThatConfigMapWasEmpty)
    assert(s5.theTestTheOtherConfigMapWasEmpty)

    val s6 = new TestWasCalledSpec
    s6.execute(testName = " this", configMap = Map("s" -> "s"))
    assert(s6.theTestThisCalled)
    assert(!s6.theTestThatCalled)
    assert(!s6.theTestTheOtherCalled)
    assert(!s6.theTestThisConfigMapWasEmpty)
    assert(s6.theTestThatConfigMapWasEmpty)
    assert(s6.theTestTheOtherConfigMapWasEmpty)
  }
  
  def `test: Suite should order encoded names in alphabetical decoded order` {

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
  
  def testDecodedSuiteName() {
    expectResult("My Spec") { new My$u0020Spec().decodedSuiteName.get }
    expectResult(None) { new SpecSuite().decodedSuiteName }
  }
  
  def testDecodedTestName() {
    
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
  
  def testRunNestedSuite() {
    
    class NoTagSpec extends Spec
    @Ignore
    class IgnoreSpec extends Spec {
      def testMethod1() {}
      def testMethod2() {}
      def testMethod3() {}
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
  
  def testExpectedTestCount() {
    class NoTagSpec extends Spec {
      def testMethod1() {}
      def testMethod2() {}
      def testMethod3() {}
    }
    @Ignore
    class IgnoreSpec extends Spec {
      def testMethod1() {}
      def testMethod2() {}
      def testMethod3() {}
    }
    @SlowAsMolasses
    class SlowAsMolassesSpec extends Spec {
      def testMethod1() {}
      def testMethod2() {}
      def testMethod3() {}
    }
    @FastAsLight
    class FastAsLightSpec extends Spec {
      def testMethod1() {}
      def testMethod2() {}
      def testMethod3() {}
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
  
  def testSuiteRunner() {
    assert(new NormalSpec().rerunner.get === classOf[NormalSpec].getName)
    assert(new WrappedSpec(Map.empty).rerunner.get === classOf[WrappedSpec].getName)
    assert(new NotAccessibleSpec("test").rerunner === None)
  }
  
  def testCheckChosenStyles() {
    class SimpleSpec extends Spec {
      def testMethod1() {}
      def testMethod2() {}
      def testMethod3() {}
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
  
  def testStackDepth() {
    class TestSpec extends Spec {
      def testFailure() {
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

@DoNotDiscover
class `My Spec` extends Spec {}
@DoNotDiscover
class NormalSpec extends Spec
@DoNotDiscover
@WrapWith(classOf[ConfigMapWrapperSuite]) 
class WrappedSpec(configMap: Map[_, _]) extends Spec
@DoNotDiscover
class NotAccessibleSpec(name: String) extends Spec
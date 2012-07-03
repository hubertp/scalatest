package org.scalatest

import org.scalatest.prop.Tables
import scala.collection.mutable.ListBuffer
import org.scalatest.events.Event
import org.scalatest.prop.TableDrivenPropertyChecks

trait ExpectedResults extends EventHelpers {
  def check(events: List[Event])
}

trait ParallelTestExecutionExamples extends Tables {
  
  def suite: Suite with ParallelTestExecution
  def fixtureSuite: fixture.Suite with ParallelTestExecution
  def funSuite: FunSuite with ParallelTestExecution
  def fixtureFunSuite: fixture.FunSuite with ParallelTestExecution
  def funSpec: FunSpec with ParallelTestExecution
  def fixtureFunSpec: fixture.FunSpec with ParallelTestExecution
  def featureSpec: FeatureSpec with ParallelTestExecution
  def fixtureFeatureSpec: fixture.FeatureSpec with ParallelTestExecution
  def flatSpec: FlatSpec with ParallelTestExecution
  def fixtureFlatSpec: fixture.FlatSpec with ParallelTestExecution
  def freeSpec: FreeSpec with ParallelTestExecution
  def fixtureFreeSpec: fixture.FreeSpec with ParallelTestExecution
  def propSpec: PropSpec with ParallelTestExecution
  def fixturePropSpec: fixture.PropSpec with ParallelTestExecution
  def wordSpec: WordSpec with ParallelTestExecution
  def fixtureWordSpec: fixture.WordSpec with ParallelTestExecution
  
  def examples =
  Table(
    ("suite", "expected"),
    (suite, new ExampleParallelTestExecutionSuiteExpectedResults), 
    (fixtureSuite, new ExampleParallelTestExecutionSuiteExpectedResults), 
    (funSuite, new ExampleParallelTestExecutionFunSuiteExpectedResults), 
    (fixtureFunSuite, new ExampleParallelTestExecutionFunSuiteExpectedResults), 
    (funSpec, new ExampleParallelTestExecutionFunSpecExpectedResults), 
    (fixtureFunSpec, new ExampleParallelTestExecutionFunSpecExpectedResults), 
    (featureSpec, new ExampleParallelTestExecutionFeatureSpecExpectedResults), 
    (fixtureFeatureSpec, new ExampleParallelTestExecutionFeatureSpecExpectedResults), 
    (flatSpec, new ExampleParallelTestExecutionFlatSpecExpectedResults), 
    (fixtureFlatSpec, new ExampleParallelTestExecutionFlatSpecExpectedResults), 
    (freeSpec, new ExampleParallelTestExecutionFreeSpecExpectedResults), 
    (fixtureFreeSpec, new ExampleParallelTestExecutionFreeSpecExpectedResults),
    (propSpec, new ExampleParallelTestExecutionPropSpecExpectedResults), 
    (fixturePropSpec, new ExampleParallelTestExecutionPropSpecExpectedResults), 
    (wordSpec, new ExampleParallelTestExecutionWordSpecExpectedResults), 
    (fixtureWordSpec, new ExampleParallelTestExecutionWordSpecExpectedResults)
  )
}

class ParallelTestExecutionProp extends FunSuite with ParallelTestExecutionExamples with TableDrivenPropertyChecks with SharedHelpers {
  
  class ControlledOrderDistributor extends Distributor {
    val buf = ListBuffer.empty[(Suite, Args)]
    def apply(suite: Suite, args: Args) {
      buf += ((suite, args))
    }
    def executeInOrder() {
      for ((suite, args) <- buf) {
        suite.run(None, args)
      }
    }
    def executeInReverseOrder() {
      for ((suite, args) <- buf.reverse) {
        suite.run(None, args)
      }
    }

    def apply(suite: Suite, tracker: Tracker) {
      throw new UnsupportedOperationException("Hey, we're not supposed to be calling this anymore!")
    }
  }
  
  def suite = new ExampleParallelTestExecutionSuite
  def fixtureSuite = new ExampleParallelTestExecutionFixtureSuite
  def funSuite = new ExampleParallelTestExecutionFunSuite
  def fixtureFunSuite = new ExampleParallelTestExecutionFixtureFunSuite
  def funSpec = new ExampleParallelTestExecutionFunSpec
  def fixtureFunSpec = new ExampleParallelTestExecutionFixtureFunSpec
  def featureSpec = new ExampleParallelTestExecutionFeatureSpec
  def fixtureFeatureSpec = new ExampleParallelTestExecutionFixtureFeatureSpec
  def flatSpec = new ExampleParallelTestExecutionFlatSpec
  def fixtureFlatSpec = new ExampleParallelTestExecutionFixtureFlatSpec
  def freeSpec = new ExampleParallelTestExecutionFreeSpec
  def fixtureFreeSpec = new ExampleParallelTestExecutionFixtureFreeSpec
  def propSpec = new ExampleParallelTestExecutionPropSpec
  def fixturePropSpec = new ExampleParallelTestExecutionFixturePropSpec
  def wordSpec = new ExampleParallelTestExecutionWordSpec
  def fixtureWordSpec = new ExampleParallelTestExecutionFixtureWordSpec
  
  def withDistributor(suite:Suite, fun: ControlledOrderDistributor => Unit) = {

    val recordingReporter = new EventRecordingReporter
    val outOfOrderDistributor = new ControlledOrderDistributor
    suite.run(None, Args(recordingReporter, distributor = Some(outOfOrderDistributor)))
    fun(outOfOrderDistributor)

    recordingReporter.eventsReceived
  }
  
  test("ParallelTestExecution should have the events reported in correct order when tests are executed in parallel") {
    forAll(examples) { (suite, expectedResults) =>
      val inOrderEvents = withDistributor(suite, _.executeInOrder)
      expectedResults.check(inOrderEvents)
      val reverseOrderEvents = withDistributor(suite, _.executeInReverseOrder)
      expectedResults.check(reverseOrderEvents)
    }
  }
}

class ExampleParallelTestExecutionSuite extends Suite with ParallelTestExecution {
  def testMethod1() {}
  def testMethod2() {}
  def testMethod3() {}
}
class ExampleParallelTestExecutionFixtureSuite extends fixture.Suite with ParallelTestExecution with StringFixture {
  def testMethod1() {}
  def testMethod2() {}
  def testMethod3() {}
}
class ExampleParallelTestExecutionSuiteExpectedResults extends ExpectedResults {
  def check(events: List[Event]) {
    assert(events.size === 6)
    checkTestStarting(events(0), "testMethod1")
    checkTestSucceeded(events(1), "testMethod1")
    checkTestStarting(events(2), "testMethod2")
    checkTestSucceeded(events(3), "testMethod2")
    checkTestStarting(events(4), "testMethod3")
    checkTestSucceeded(events(5), "testMethod3")
  }
}

class ExampleParallelTestExecutionFunSuite extends FunSuite with ParallelTestExecution {
  test("Test 1") {}
  test("Test 2") {}
  test("Test 3") {}
}
class ExampleParallelTestExecutionFixtureFunSuite extends fixture.FunSuite with ParallelTestExecution with StringFixture {
  test("Test 1") { fixture => }
  test("Test 2") { fixture => }
  test("Test 3") { fixture => }
}
class ExampleParallelTestExecutionFunSuiteExpectedResults extends ExpectedResults {
  def check(events: List[Event]) {
    assert(events.size === 6)
    checkTestStarting(events(0), "Test 1")
    checkTestSucceeded(events(1), "Test 1")
    checkTestStarting(events(2), "Test 2")
    checkTestSucceeded(events(3), "Test 2")
    checkTestStarting(events(4), "Test 3")
    checkTestSucceeded(events(5), "Test 3")
  }
}

class ExampleParallelTestExecutionFunSpec extends FunSpec with ParallelTestExecution {
  describe("Scope 1") {
    it("Test 1") {}
    it("Test 2") {}
  }
  describe("Scope 2") {
    it("Test 3") {}
    it("Test 4") {}
  }
}
class ExampleParallelTestExecutionFixtureFunSpec extends fixture.FunSpec with ParallelTestExecution with StringFixture {
  describe("Scope 1") {
    it("Test 1") { fixture => }
    it("Test 2") { fixture =>}
  }
  describe("Scope 2") {
    it("Test 3") { fixture => }
    it("Test 4") { fixture =>}
  }
}
class ExampleParallelTestExecutionFunSpecExpectedResults extends ExpectedResults {
  def check(events: List[Event]) {
    assert(events.size === 12)
    checkScopeOpened(events(0), "Scope 1")
    checkTestStarting(events(1), "Scope 1 Test 1")
    checkTestSucceeded(events(2), "Scope 1 Test 1")
    checkTestStarting(events(3), "Scope 1 Test 2")
    checkTestSucceeded(events(4), "Scope 1 Test 2")
    checkScopeClosed(events(5), "Scope 1")
    checkScopeOpened(events(6), "Scope 2")
    checkTestStarting(events(7), "Scope 2 Test 3")
    checkTestSucceeded(events(8), "Scope 2 Test 3")
    checkTestStarting(events(9), "Scope 2 Test 4")
    checkTestSucceeded(events(10), "Scope 2 Test 4")
    checkScopeClosed(events(11), "Scope 2")
  }
}

class ExampleParallelTestExecutionFeatureSpec extends FeatureSpec with ParallelTestExecution {
  feature("Scope 1") {
    scenario("Test 1") {}
    scenario("Test 2") {}
  }
  feature("Scope 2") {
    scenario("Test 3") {}
    scenario("Test 4") {}
  }
}
class ExampleParallelTestExecutionFixtureFeatureSpec extends fixture.FeatureSpec with ParallelTestExecution with StringFixture {
  feature("Scope 1") {
    scenario("Test 1") { fixture => }
    scenario("Test 2") { fixture =>}
  }
  feature("Scope 2") {
    scenario("Test 3") { fixture => }
    scenario("Test 4") { fixture =>}
  }
}
class ExampleParallelTestExecutionFeatureSpecExpectedResults extends ExpectedResults {
  def check(events: List[Event]) {
    assert(events.size === 12)
    checkScopeOpened(events(0), "Scope 1")
    checkTestStarting(events(1), "Scope 1 Scenario: Test 1")
    checkTestSucceeded(events(2), "Scope 1 Scenario: Test 1")
    checkTestStarting(events(3), "Scope 1 Scenario: Test 2")
    checkTestSucceeded(events(4), "Scope 1 Scenario: Test 2")
    checkScopeClosed(events(5), "Scope 1")
    checkScopeOpened(events(6), "Scope 2")
    checkTestStarting(events(7), "Scope 2 Scenario: Test 3")
    checkTestSucceeded(events(8), "Scope 2 Scenario: Test 3")
    checkTestStarting(events(9), "Scope 2 Scenario: Test 4")
    checkTestSucceeded(events(10), "Scope 2 Scenario: Test 4")
    checkScopeClosed(events(11), "Scope 2")
  }
}

class ExampleParallelTestExecutionFlatSpec extends FlatSpec with ParallelTestExecution {
  behavior of "Scope 1"
  it should "Test 1" in {}
  it should "Test 2" in {}
  
  behavior of "Scope 2"
  it should "Test 3" in {}
  it should "Test 4" in {}
}
class ExampleParallelTestExecutionFixtureFlatSpec extends fixture.FlatSpec with ParallelTestExecution with StringFixture {
  behavior of "Scope 1"
  it should "Test 1" in { fixture => }
  it should "Test 2" in { fixture => }
  
  behavior of "Scope 2"
  it should "Test 3" in { fixture => }
  it should "Test 4" in { fixture => }
}
class ExampleParallelTestExecutionFlatSpecExpectedResults extends ExpectedResults {
  def check(events: List[Event]) {
    assert(events.size === 12)
    checkScopeOpened(events(0), "Scope 1")
    checkTestStarting(events(1), "Scope 1 should Test 1")
    checkTestSucceeded(events(2), "Scope 1 should Test 1")
    checkTestStarting(events(3), "Scope 1 should Test 2")
    checkTestSucceeded(events(4), "Scope 1 should Test 2")
    checkScopeClosed(events(5), "Scope 1")
    checkScopeOpened(events(6), "Scope 2")
    checkTestStarting(events(7), "Scope 2 should Test 3")
    checkTestSucceeded(events(8), "Scope 2 should Test 3")
    checkTestStarting(events(9), "Scope 2 should Test 4")
    checkTestSucceeded(events(10), "Scope 2 should Test 4")
    checkScopeClosed(events(11), "Scope 2")
  }
}

class ExampleParallelTestExecutionFreeSpec extends FreeSpec with ParallelTestExecution {
  "Scope 1" - {
    "Test 1" in {}
    "Test 2" in {}
  }
  
  "Scope 2" - {
    "Test 3" in {}
    "Test 4" in {}
  }
}
class ExampleParallelTestExecutionFixtureFreeSpec extends fixture.FreeSpec with ParallelTestExecution with StringFixture {
  "Scope 1" - {
    "Test 1" in { fixture => }
    "Test 2" in { fixture => }
  }
  
  "Scope 2" - {
    "Test 3" in { fixture => }
    "Test 4" in { fixture => }
  }
}
class ExampleParallelTestExecutionFreeSpecExpectedResults extends ExpectedResults {
  def check(events: List[Event]) {
    assert(events.size === 12)
    checkScopeOpened(events(0), "Scope 1")
    checkTestStarting(events(1), "Scope 1 Test 1")
    checkTestSucceeded(events(2), "Scope 1 Test 1")
    checkTestStarting(events(3), "Scope 1 Test 2")
    checkTestSucceeded(events(4), "Scope 1 Test 2")
    checkScopeClosed(events(5), "Scope 1")
    checkScopeOpened(events(6), "Scope 2")
    checkTestStarting(events(7), "Scope 2 Test 3")
    checkTestSucceeded(events(8), "Scope 2 Test 3")
    checkTestStarting(events(9), "Scope 2 Test 4")
    checkTestSucceeded(events(10), "Scope 2 Test 4")
    checkScopeClosed(events(11), "Scope 2")
  }
}

class ExampleParallelTestExecutionPropSpec extends PropSpec with ParallelTestExecution {
  property("Test 1") {}
  property("Test 2") {}
  property("Test 3") {}
}
class ExampleParallelTestExecutionFixturePropSpec extends fixture.PropSpec with ParallelTestExecution with StringFixture {
  property("Test 1") { fixture => }
  property("Test 2") { fixture => }
  property("Test 3") { fixture => }
}
class ExampleParallelTestExecutionPropSpecExpectedResults extends ExpectedResults {
  def check(events: List[Event]) {
    assert(events.size === 6)
    checkTestStarting(events(0), "Test 1")
    checkTestSucceeded(events(1), "Test 1")
    checkTestStarting(events(2), "Test 2")
    checkTestSucceeded(events(3), "Test 2")
    checkTestStarting(events(4), "Test 3")
    checkTestSucceeded(events(5), "Test 3")
  }
}

class ExampleParallelTestExecutionWordSpec extends WordSpec with ParallelTestExecution {
  "Scope 1" should {
    "Test 1" in {}
    "Test 2" in {}
  }
  
  "Scope 2" should {
    "Test 3" in {}
    "Test 4" in {}
  }
}
class ExampleParallelTestExecutionFixtureWordSpec extends fixture.WordSpec with ParallelTestExecution with StringFixture {
  "Scope 1" should {
    "Test 1" in { fixture => }
    "Test 2" in { fixture => }
  }
  
  "Scope 2" should {
    "Test 3" in { fixture => }
    "Test 4" in { fixture => }
  }
}
class ExampleParallelTestExecutionWordSpecExpectedResults extends ExpectedResults {
  def check(events: List[Event]) {
    assert(events.size === 12)
    checkScopeOpened(events(0), "Scope 1")
    checkTestStarting(events(1), "Scope 1 should Test 1")
    checkTestSucceeded(events(2), "Scope 1 should Test 1")
    checkTestStarting(events(3), "Scope 1 should Test 2")
    checkTestSucceeded(events(4), "Scope 1 should Test 2")
    checkScopeClosed(events(5), "Scope 1")
    checkScopeOpened(events(6), "Scope 2")
    checkTestStarting(events(7), "Scope 2 should Test 3")
    checkTestSucceeded(events(8), "Scope 2 should Test 3")
    checkTestStarting(events(9), "Scope 2 should Test 4")
    checkTestSucceeded(events(10), "Scope 2 should Test 4")
    checkScopeClosed(events(11), "Scope 2")
  }
}
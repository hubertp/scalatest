package org.scalatest

import org.scalatest.prop.Tables
import scala.collection.mutable.ListBuffer
import org.scalatest.events.Event
import org.scalatest.prop.TableDrivenPropertyChecks

trait ExpectedResults extends EventHelpers {
  def assertOrderTest(events: List[Event])
  def assertBeforeAfterInfo(events: List[Event])
}

trait ParallelTestExecutionExamples extends Tables {
  
  def suite1Fun: () => Suite with ParallelTestExecution
  def suite2Fun: () => Suite with ParallelTestExecution
  def fixtureSuite1Fun: () => fixture.Suite with ParallelTestExecution
  def fixtureSuite2Fun: () => fixture.Suite with ParallelTestExecution
  def funSuite1Fun: () => FunSuite with ParallelTestExecution
  def funSuite2Fun: () => FunSuite with ParallelTestExecution
  def fixtureFunSuite1Fun: () => fixture.FunSuite with ParallelTestExecution
  def fixtureFunSuite2Fun: () => fixture.FunSuite with ParallelTestExecution
  def funSpec1Fun: () => FunSpec with ParallelTestExecution
  def funSpec2Fun: () => FunSpec with ParallelTestExecution
  def fixtureFunSpec1Fun: () => fixture.FunSpec with ParallelTestExecution
  def fixtureFunSpec2Fun: () => fixture.FunSpec with ParallelTestExecution
  def featureSpec1Fun: () => FeatureSpec with ParallelTestExecution
  def featureSpec2Fun: () => FeatureSpec with ParallelTestExecution
  def fixtureFeatureSpec1Fun: () => fixture.FeatureSpec with ParallelTestExecution
  def fixtureFeatureSpec2Fun: () => fixture.FeatureSpec with ParallelTestExecution
  def flatSpec1Fun: () => FlatSpec with ParallelTestExecution
  def flatSpec2Fun: () => FlatSpec with ParallelTestExecution
  def fixtureFlatSpec1Fun: () => fixture.FlatSpec with ParallelTestExecution
  def fixtureFlatSpec2Fun: () => fixture.FlatSpec with ParallelTestExecution
  def freeSpec1Fun: () => FreeSpec with ParallelTestExecution
  def freeSpec2Fun: () => FreeSpec with ParallelTestExecution
  def fixtureFreeSpec1Fun: () => fixture.FreeSpec with ParallelTestExecution
  def fixtureFreeSpec2Fun: () => fixture.FreeSpec with ParallelTestExecution
  def propSpec1Fun: () => PropSpec with ParallelTestExecution
  def propSpec2Fun: () => PropSpec with ParallelTestExecution
  def fixturePropSpec1Fun: () => fixture.PropSpec with ParallelTestExecution
  def fixturePropSpec2Fun: () => fixture.PropSpec with ParallelTestExecution
  def wordSpec1Fun: () => WordSpec with ParallelTestExecution
  def wordSpec2Fun: () => WordSpec with ParallelTestExecution
  def fixtureWordSpec1Fun: () => fixture.WordSpec with ParallelTestExecution
  def fixtureWordSpec2Fun: () => fixture.WordSpec with ParallelTestExecution
  
  def examples =
  Table(
    ("suite1", "suite2", "expected"),
    (suite1Fun, suite2Fun, new ExampleParallelTestExecutionSuiteExpectedResults), 
    (fixtureSuite1Fun, fixtureSuite2Fun, new ExampleParallelTestExecutionSuiteExpectedResults), 
    (funSuite1Fun, funSuite2Fun, new ExampleParallelTestExecutionFunSuiteExpectedResults), 
    (fixtureFunSuite1Fun, fixtureFunSuite2Fun, new ExampleParallelTestExecutionFunSuiteExpectedResults), 
    (funSpec1Fun, funSpec2Fun, new ExampleParallelTestExecutionFunSpecExpectedResults), 
    (fixtureFunSpec1Fun, fixtureFunSpec2Fun, new ExampleParallelTestExecutionFunSpecExpectedResults), 
    (featureSpec1Fun, featureSpec2Fun, new ExampleParallelTestExecutionFeatureSpecExpectedResults), 
    (fixtureFeatureSpec1Fun, fixtureFeatureSpec2Fun, new ExampleParallelTestExecutionFeatureSpecExpectedResults), 
    (flatSpec1Fun, flatSpec2Fun, new ExampleParallelTestExecutionFlatSpecExpectedResults), 
    (fixtureFlatSpec1Fun, fixtureFlatSpec2Fun, new ExampleParallelTestExecutionFlatSpecExpectedResults), 
    (freeSpec1Fun, freeSpec2Fun, new ExampleParallelTestExecutionFreeSpecExpectedResults), 
    (fixtureFreeSpec1Fun, fixtureFreeSpec2Fun, new ExampleParallelTestExecutionFreeSpecExpectedResults),
    (propSpec1Fun, propSpec2Fun, new ExampleParallelTestExecutionPropSpecExpectedResults), 
    (fixturePropSpec1Fun, fixturePropSpec2Fun, new ExampleParallelTestExecutionPropSpecExpectedResults), 
    (wordSpec1Fun, wordSpec2Fun, new ExampleParallelTestExecutionWordSpecExpectedResults), 
    (fixtureWordSpec1Fun, wordSpec2Fun, new ExampleParallelTestExecutionWordSpecExpectedResults)
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
  
  def suite1Fun = () => new ExampleParallelTestExecutionSuite1
  def suite2Fun = () => new ExampleParallelTestExecutionSuite2
  def fixtureSuite1Fun = () => new ExampleParallelTestExecutionFixtureSuite1
  def fixtureSuite2Fun = () => new ExampleParallelTestExecutionFixtureSuite2
  def funSuite1Fun = () => new ExampleParallelTestExecutionFunSuite1
  def funSuite2Fun = () => new ExampleParallelTestExecutionFunSuite2
  def fixtureFunSuite1Fun = () => new ExampleParallelTestExecutionFixtureFunSuite1
  def fixtureFunSuite2Fun = () => new ExampleParallelTestExecutionFixtureFunSuite2
  def funSpec1Fun = () => new ExampleParallelTestExecutionFunSpec1
  def funSpec2Fun = () => new ExampleParallelTestExecutionFunSpec2
  def fixtureFunSpec1Fun = () => new ExampleParallelTestExecutionFixtureFunSpec1
  def fixtureFunSpec2Fun = () => new ExampleParallelTestExecutionFixtureFunSpec2
  def featureSpec1Fun = () => new ExampleParallelTestExecutionFeatureSpec1
  def featureSpec2Fun = () => new ExampleParallelTestExecutionFeatureSpec2
  def fixtureFeatureSpec1Fun = () => new ExampleParallelTestExecutionFixtureFeatureSpec1
  def fixtureFeatureSpec2Fun = () => new ExampleParallelTestExecutionFixtureFeatureSpec2
  def flatSpec1Fun = () => new ExampleParallelTestExecutionFlatSpec1
  def flatSpec2Fun = () => new ExampleParallelTestExecutionFlatSpec2
  def fixtureFlatSpec1Fun = () => new ExampleParallelTestExecutionFixtureFlatSpec1
  def fixtureFlatSpec2Fun = () => new ExampleParallelTestExecutionFixtureFlatSpec2
  def freeSpec1Fun = () => new ExampleParallelTestExecutionFreeSpec1
  def freeSpec2Fun = () => new ExampleParallelTestExecutionFreeSpec2
  def fixtureFreeSpec1Fun = () => new ExampleParallelTestExecutionFixtureFreeSpec1
  def fixtureFreeSpec2Fun = () => new ExampleParallelTestExecutionFixtureFreeSpec2
  def propSpec1Fun = () => new ExampleParallelTestExecutionPropSpec1
  def propSpec2Fun = () => new ExampleParallelTestExecutionPropSpec2
  def fixturePropSpec1Fun = () => new ExampleParallelTestExecutionFixturePropSpec1
  def fixturePropSpec2Fun = () => new ExampleParallelTestExecutionFixturePropSpec2
  def wordSpec1Fun = () => new ExampleParallelTestExecutionWordSpec1
  def wordSpec2Fun = () => new ExampleParallelTestExecutionWordSpec2
  def fixtureWordSpec1Fun = () => new ExampleParallelTestExecutionFixtureWordSpec1
  def fixtureWordSpec2Fun = () => new ExampleParallelTestExecutionFixtureWordSpec2
  
  def withDistributor(suite:Suite, fun: ControlledOrderDistributor => Unit) = {

    val recordingReporter = new EventRecordingReporter
    val outOfOrderDistributor = new ControlledOrderDistributor
    suite.run(None, Args(recordingReporter, distributor = Some(outOfOrderDistributor)))
    fun(outOfOrderDistributor)

    recordingReporter.eventsReceived
  }
  
  test("ParallelTestExecution should have the events reported in correct order when tests are executed in parallel") {
    forAll(examples) { (suite1Fun, suite2Fun, expectedResults) =>
      val inOrderEvents = withDistributor(suite1Fun(), _.executeInOrder)
      expectedResults.assertOrderTest(inOrderEvents)
      val reverseOrderEvents = withDistributor(suite1Fun(), _.executeInReverseOrder)
      expectedResults.assertOrderTest(reverseOrderEvents)
    }
  }
  
  test("ParallelTestExecution should have InfoProvided fired from before and after block in correct order when tests are executed in parallel") {
    forAll(examples) { (suite1Fun, suite2Fun, expectedResults) =>
      val inOrderEvents = withDistributor(suite2Fun(), _.executeInOrder)
      expectedResults.assertBeforeAfterInfo(inOrderEvents)
      val reverseOrderEvents = withDistributor(suite2Fun(), _.executeInReverseOrder)
      expectedResults.assertBeforeAfterInfo(reverseOrderEvents)
    }
  }
}

class ExampleParallelTestExecutionSuite1 extends Suite with ParallelTestExecution {
  def testMethod1() {}
  def testMethod2() {}
  def testMethod3() {}
}
class ExampleParallelTestExecutionSuite2 extends Suite with BeforeAndAfter with ParallelTestExecution {
  before {}  // how to fire info here?
  def testMethod1() {}
  def testMethod2() {}
  def testMethod3() {}
  after {} // how to fire info here?
}
class ExampleParallelTestExecutionFixtureSuite1 extends fixture.Suite with ParallelTestExecution with StringFixture {
  def testMethod1() {}
  def testMethod2() {}
  def testMethod3() {}
}
class ExampleParallelTestExecutionFixtureSuite2 extends fixture.Suite with BeforeAndAfter with ParallelTestExecution with StringFixture {
  before {}  // how to fire info here?
  def testMethod1() {}
  def testMethod2() {}
  def testMethod3() {}
  after {}  // how to fire info here?
}
class ExampleParallelTestExecutionSuiteExpectedResults extends ExpectedResults {
  def assertOrderTest(events: List[Event]) {
    assert(events.size === 6)
    checkTestStarting(events(0), "testMethod1")
    checkTestSucceeded(events(1), "testMethod1")
    checkTestStarting(events(2), "testMethod2")
    checkTestSucceeded(events(3), "testMethod2")
    checkTestStarting(events(4), "testMethod3")
    checkTestSucceeded(events(5), "testMethod3")
  }
  def assertBeforeAfterInfo(events: List[Event]) {
    assert(events.size === 6)
    checkTestStarting(events(0), "testMethod1")
    checkTestSucceeded(events(1), "testMethod1")
    checkTestStarting(events(2), "testMethod2")
    checkTestSucceeded(events(3), "testMethod2")
    checkTestStarting(events(4), "testMethod3")
    checkTestSucceeded(events(5), "testMethod3")
  }
}

class ExampleParallelTestExecutionFunSuite1 extends FunSuite with ParallelTestExecution {
  test("Test 1") {}
  test("Test 2") {}
  test("Test 3") {}
}
class ExampleParallelTestExecutionFunSuite2 extends FunSuite with BeforeAndAfter with ParallelTestExecution {
  before { info("In Before") }
  after { info("In After") }
  test("Test 1") {}
  test("Test 2") {}
  test("Test 3") {}
}
class ExampleParallelTestExecutionFixtureFunSuite1 extends fixture.FunSuite with ParallelTestExecution with StringFixture {
  test("Test 1") { fixture => }
  test("Test 2") { fixture => }
  test("Test 3") { fixture => }
}
class ExampleParallelTestExecutionFixtureFunSuite2 extends fixture.FunSuite with BeforeAndAfter with ParallelTestExecution with StringFixture {
  before { info("In Before") }
  after { info("In After") }
  test("Test 1") { fixture => }
  test("Test 2") { fixture => }
  test("Test 3") { fixture => }
}
class ExampleParallelTestExecutionFunSuiteExpectedResults extends ExpectedResults {
  def assertOrderTest(events: List[Event]) {
    assert(events.size === 6)
    checkTestStarting(events(0), "Test 1")
    checkTestSucceeded(events(1), "Test 1")
    checkTestStarting(events(2), "Test 2")
    checkTestSucceeded(events(3), "Test 2")
    checkTestStarting(events(4), "Test 3")
    checkTestSucceeded(events(5), "Test 3")
  }
  def assertBeforeAfterInfo(events: List[Event]) {
    assert(events.size === 12)
    checkInfoProvided(events(0), "In Before")
    checkTestStarting(events(1), "Test 1")
    checkTestSucceeded(events(2), "Test 1")
    checkInfoProvided(events(3), "In After")
    checkInfoProvided(events(4), "In Before")
    checkTestStarting(events(5), "Test 2")
    checkTestSucceeded(events(6), "Test 2")
    checkInfoProvided(events(7), "In After")
    checkInfoProvided(events(8), "In Before")
    checkTestStarting(events(9), "Test 3")
    checkTestSucceeded(events(10), "Test 3")
    checkInfoProvided(events(11), "In After")
  }
}

class ExampleParallelTestExecutionFunSpec1 extends FunSpec with ParallelTestExecution {
  describe("Scope 1") {
    it("Test 1") {}
    it("Test 2") {}
  }
  describe("Scope 2") {
    it("Test 3") {}
    it("Test 4") {}
  }
}
class ExampleParallelTestExecutionFunSpec2 extends FunSpec with BeforeAndAfter with ParallelTestExecution {
  before { info("In Before") }
  after { info("In After") }
  describe("Scope 1") {
    it("Test 1") {}
    it("Test 2") {}
  }
  describe("Scope 2") {
    it("Test 3") {}
    it("Test 4") {}
  }
}
class ExampleParallelTestExecutionFixtureFunSpec1 extends fixture.FunSpec with ParallelTestExecution with StringFixture {
  describe("Scope 1") {
    it("Test 1") { fixture => }
    it("Test 2") { fixture =>}
  }
  describe("Scope 2") {
    it("Test 3") { fixture => }
    it("Test 4") { fixture =>}
  }
}
class ExampleParallelTestExecutionFixtureFunSpec2 extends fixture.FunSpec with BeforeAndAfter with ParallelTestExecution with StringFixture {
  before { info("In Before") }
  after { info("In After") }
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
  def assertOrderTest(events: List[Event]) {
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
  def assertBeforeAfterInfo(events: List[Event]) {
    assert(events.size === 20)
    checkScopeOpened(events(0), "Scope 1")
    checkInfoProvided(events(1), "In Before")
    checkTestStarting(events(2), "Scope 1 Test 1")
    checkTestSucceeded(events(3), "Scope 1 Test 1")
    checkInfoProvided(events(4), "In After")
    checkInfoProvided(events(5), "In Before")
    checkTestStarting(events(6), "Scope 1 Test 2")
    checkTestSucceeded(events(7), "Scope 1 Test 2")
    checkInfoProvided(events(8), "In After")
    checkScopeClosed(events(9), "Scope 1")
    checkScopeOpened(events(10), "Scope 2")
    checkInfoProvided(events(11), "In Before")
    checkTestStarting(events(12), "Scope 2 Test 3")
    checkTestSucceeded(events(13), "Scope 2 Test 3")
    checkInfoProvided(events(14), "In After")
    checkInfoProvided(events(15), "In Before")
    checkTestStarting(events(16), "Scope 2 Test 4")
    checkTestSucceeded(events(17), "Scope 2 Test 4")
    checkInfoProvided(events(18), "In After")
    checkScopeClosed(events(19), "Scope 2")
  }
}

class ExampleParallelTestExecutionFeatureSpec1 extends FeatureSpec with ParallelTestExecution {
  feature("Scope 1") {
    scenario("Test 1") {}
    scenario("Test 2") {}
  }
  feature("Scope 2") {
    scenario("Test 3") {}
    scenario("Test 4") {}
  }
}
class ExampleParallelTestExecutionFeatureSpec2 extends FeatureSpec with BeforeAndAfter with ParallelTestExecution {
  before { info("In Before") }
  after { info("In After") }
  feature("Scope 1") {
    scenario("Test 1") {}
    scenario("Test 2") {}
  }
  feature("Scope 2") {
    scenario("Test 3") {}
    scenario("Test 4") {}
  }
}
class ExampleParallelTestExecutionFixtureFeatureSpec1 extends fixture.FeatureSpec with ParallelTestExecution with StringFixture {
  feature("Scope 1") {
    scenario("Test 1") { fixture => }
    scenario("Test 2") { fixture =>}
  }
  feature("Scope 2") {
    scenario("Test 3") { fixture => }
    scenario("Test 4") { fixture =>}
  }
}
class ExampleParallelTestExecutionFixtureFeatureSpec2 extends fixture.FeatureSpec with BeforeAndAfter with ParallelTestExecution with StringFixture {
  before { info("In Before") }
  after { info("In After") }
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
  def assertOrderTest(events: List[Event]) {
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
  def assertBeforeAfterInfo(events: List[Event]) {
    assert(events.size === 20)
    checkScopeOpened(events(0), "Scope 1")
    checkInfoProvided(events(1), "In Before")
    checkTestStarting(events(2), "Scope 1 Scenario: Test 1")
    checkTestSucceeded(events(3), "Scope 1 Scenario: Test 1")
    checkInfoProvided(events(4), "In After")
    checkInfoProvided(events(5), "In Before")
    checkTestStarting(events(6), "Scope 1 Scenario: Test 2")
    checkTestSucceeded(events(7), "Scope 1 Scenario: Test 2")
    checkInfoProvided(events(8), "In After")
    checkScopeClosed(events(9), "Scope 1")
    checkScopeOpened(events(10), "Scope 2")
    checkInfoProvided(events(11), "In Before")
    checkTestStarting(events(12), "Scope 2 Scenario: Test 3")
    checkTestSucceeded(events(13), "Scope 2 Scenario: Test 3")
    checkInfoProvided(events(14), "In After")
    checkInfoProvided(events(15), "In Before")
    checkTestStarting(events(16), "Scope 2 Scenario: Test 4")
    checkTestSucceeded(events(17), "Scope 2 Scenario: Test 4")
    checkInfoProvided(events(18), "In After")
    checkScopeClosed(events(19), "Scope 2")
  }
}

class ExampleParallelTestExecutionFlatSpec1 extends FlatSpec with ParallelTestExecution {
  behavior of "Scope 1"
  it should "Test 1" in {}
  it should "Test 2" in {}
  
  behavior of "Scope 2"
  it should "Test 3" in {}
  it should "Test 4" in {}
}
class ExampleParallelTestExecutionFlatSpec2 extends FlatSpec with BeforeAndAfter with ParallelTestExecution {
  before { info("In Before") }
  after { info("In After") }
  behavior of "Scope 1"
  it should "Test 1" in {}
  it should "Test 2" in {}
  
  behavior of "Scope 2"
  it should "Test 3" in {}
  it should "Test 4" in {}
}
class ExampleParallelTestExecutionFixtureFlatSpec1 extends fixture.FlatSpec with ParallelTestExecution with StringFixture {
  behavior of "Scope 1"
  it should "Test 1" in { fixture => }
  it should "Test 2" in { fixture => }
  
  behavior of "Scope 2"
  it should "Test 3" in { fixture => }
  it should "Test 4" in { fixture => }
}
class ExampleParallelTestExecutionFixtureFlatSpec2 extends fixture.FlatSpec with BeforeAndAfter with ParallelTestExecution with StringFixture {
  before { info("In Before") }
  after { info("In After") }
  behavior of "Scope 1"
  it should "Test 1" in { fixture => }
  it should "Test 2" in { fixture => }
  
  behavior of "Scope 2"
  it should "Test 3" in { fixture => }
  it should "Test 4" in { fixture => }
}
class ExampleParallelTestExecutionFlatSpecExpectedResults extends ExpectedResults {
  def assertOrderTest(events: List[Event]) {
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
  def assertBeforeAfterInfo(events: List[Event]) {
    assert(events.size === 20)
    checkScopeOpened(events(0), "Scope 1")
    checkInfoProvided(events(1), "In Before")
    checkTestStarting(events(2), "Scope 1 should Test 1")
    checkTestSucceeded(events(3), "Scope 1 should Test 1")
    checkInfoProvided(events(4), "In After")
    checkInfoProvided(events(5), "In Before")
    checkTestStarting(events(6), "Scope 1 should Test 2")
    checkTestSucceeded(events(7), "Scope 1 should Test 2")
    checkInfoProvided(events(8), "In After")
    checkScopeClosed(events(9), "Scope 1")
    checkScopeOpened(events(10), "Scope 2")
    checkInfoProvided(events(11), "In Before")
    checkTestStarting(events(12), "Scope 2 should Test 3")
    checkTestSucceeded(events(13), "Scope 2 should Test 3")
    checkInfoProvided(events(14), "In After")
    checkInfoProvided(events(15), "In Before")
    checkTestStarting(events(16), "Scope 2 should Test 4")
    checkTestSucceeded(events(17), "Scope 2 should Test 4")
    checkInfoProvided(events(18), "In After")
    checkScopeClosed(events(19), "Scope 2")
  }
}

class ExampleParallelTestExecutionFreeSpec1 extends FreeSpec with ParallelTestExecution {
  "Scope 1" - {
    "Test 1" in {}
    "Test 2" in {}
  }
  
  "Scope 2" - {
    "Test 3" in {}
    "Test 4" in {}
  }
}
class ExampleParallelTestExecutionFreeSpec2 extends FreeSpec with BeforeAndAfter with ParallelTestExecution {
  before { info("In Before") }
  after { info("In After") }
  "Scope 1" - {
    "Test 1" in {}
    "Test 2" in {}
  }
  
  "Scope 2" - {
    "Test 3" in {}
    "Test 4" in {}
  }
}
class ExampleParallelTestExecutionFixtureFreeSpec1 extends fixture.FreeSpec with ParallelTestExecution with StringFixture {
  "Scope 1" - {
    "Test 1" in { fixture => }
    "Test 2" in { fixture => }
  }
  
  "Scope 2" - {
    "Test 3" in { fixture => }
    "Test 4" in { fixture => }
  }
}
class ExampleParallelTestExecutionFixtureFreeSpec2 extends fixture.FreeSpec with BeforeAndAfter with ParallelTestExecution with StringFixture {
  before { info("In Before") }
  after { info("In After") }
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
  def assertOrderTest(events: List[Event]) {
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
  def assertBeforeAfterInfo(events: List[Event]) {
    assert(events.size === 20)
    checkScopeOpened(events(0), "Scope 1")
    checkInfoProvided(events(1), "In Before")
    checkTestStarting(events(2), "Scope 1 Test 1")
    checkTestSucceeded(events(3), "Scope 1 Test 1")
    checkInfoProvided(events(4), "In After")
    checkInfoProvided(events(5), "In Before")
    checkTestStarting(events(6), "Scope 1 Test 2")
    checkTestSucceeded(events(7), "Scope 1 Test 2")
    checkInfoProvided(events(8), "In After")
    checkScopeClosed(events(9), "Scope 1")
    checkScopeOpened(events(10), "Scope 2")
    checkInfoProvided(events(11), "In Before")
    checkTestStarting(events(12), "Scope 2 Test 3")
    checkTestSucceeded(events(13), "Scope 2 Test 3")
    checkInfoProvided(events(14), "In After")
    checkInfoProvided(events(15), "In Before")
    checkTestStarting(events(16), "Scope 2 Test 4")
    checkTestSucceeded(events(17), "Scope 2 Test 4")
    checkInfoProvided(events(18), "In After")
    checkScopeClosed(events(19), "Scope 2")
  }
}

class ExampleParallelTestExecutionPropSpec1 extends PropSpec with ParallelTestExecution {
  property("Test 1") {}
  property("Test 2") {}
  property("Test 3") {}
}
class ExampleParallelTestExecutionPropSpec2 extends PropSpec with BeforeAndAfter with ParallelTestExecution {
  before { info("In Before") }
  after { info("In After") }
  property("Test 1") {}
  property("Test 2") {}
  property("Test 3") {}
}
class ExampleParallelTestExecutionFixturePropSpec1 extends fixture.PropSpec with ParallelTestExecution with StringFixture {
  property("Test 1") { fixture => }
  property("Test 2") { fixture => }
  property("Test 3") { fixture => }
}
class ExampleParallelTestExecutionFixturePropSpec2 extends fixture.PropSpec with BeforeAndAfter with ParallelTestExecution with StringFixture {
  before { info("In Before") }
  after { info("In After") }
  property("Test 1") { fixture => }
  property("Test 2") { fixture => }
  property("Test 3") { fixture => }
}
class ExampleParallelTestExecutionPropSpecExpectedResults extends ExpectedResults {
  def assertOrderTest(events: List[Event]) {
    assert(events.size === 6)
    checkTestStarting(events(0), "Test 1")
    checkTestSucceeded(events(1), "Test 1")
    checkTestStarting(events(2), "Test 2")
    checkTestSucceeded(events(3), "Test 2")
    checkTestStarting(events(4), "Test 3")
    checkTestSucceeded(events(5), "Test 3")
  }
  def assertBeforeAfterInfo(events: List[Event]) {
    assert(events.size === 12)
    checkInfoProvided(events(0), "In Before")
    checkTestStarting(events(1), "Test 1")
    checkTestSucceeded(events(2), "Test 1")
    checkInfoProvided(events(3), "In After")
    checkInfoProvided(events(4), "In Before")
    checkTestStarting(events(5), "Test 2")
    checkTestSucceeded(events(6), "Test 2")
    checkInfoProvided(events(7), "In After")
    checkInfoProvided(events(8), "In Before")
    checkTestStarting(events(9), "Test 3")
    checkTestSucceeded(events(10), "Test 3")
    checkInfoProvided(events(11), "In After")
  }
}

class ExampleParallelTestExecutionWordSpec1 extends WordSpec with ParallelTestExecution {
  "Scope 1" should {
    "Test 1" in {}
    "Test 2" in {}
  }
  
  "Scope 2" should {
    "Test 3" in {}
    "Test 4" in {}
  }
}
class ExampleParallelTestExecutionWordSpec2 extends WordSpec with BeforeAndAfter with ParallelTestExecution {
  before { info("In Before") }
  after { info("In After") }
  "Scope 1" should {
    "Test 1" in {}
    "Test 2" in {}
  }
  
  "Scope 2" should {
    "Test 3" in {}
    "Test 4" in {}
  }
}
class ExampleParallelTestExecutionFixtureWordSpec1 extends fixture.WordSpec with ParallelTestExecution with StringFixture {
  "Scope 1" should {
    "Test 1" in { fixture => }
    "Test 2" in { fixture => }
  }
  
  "Scope 2" should {
    "Test 3" in { fixture => }
    "Test 4" in { fixture => }
  }
}
class ExampleParallelTestExecutionFixtureWordSpec2 extends fixture.WordSpec with BeforeAndAfter with ParallelTestExecution with StringFixture {
  before { info("In Before") }
  after { info("In After") }
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
  def assertOrderTest(events: List[Event]) {
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
  def assertBeforeAfterInfo(events: List[Event]) {
    assert(events.size === 20)
    checkScopeOpened(events(0), "Scope 1")
    checkInfoProvided(events(1), "In Before")
    checkTestStarting(events(2), "Scope 1 should Test 1")
    checkTestSucceeded(events(3), "Scope 1 should Test 1")
    checkInfoProvided(events(4), "In After")
    checkInfoProvided(events(5), "In Before")
    checkTestStarting(events(6), "Scope 1 should Test 2")
    checkTestSucceeded(events(7), "Scope 1 should Test 2")
    checkInfoProvided(events(8), "In After")
    checkScopeClosed(events(9), "Scope 1")
    checkScopeOpened(events(10), "Scope 2")
    checkInfoProvided(events(11), "In Before")
    checkTestStarting(events(12), "Scope 2 should Test 3")
    checkTestSucceeded(events(13), "Scope 2 should Test 3")
    checkInfoProvided(events(14), "In After")
    checkInfoProvided(events(15), "In Before")
    checkTestStarting(events(16), "Scope 2 should Test 4")
    checkTestSucceeded(events(17), "Scope 2 should Test 4")
    checkInfoProvided(events(18), "In After")
    checkScopeClosed(events(19), "Scope 2")
  }
}
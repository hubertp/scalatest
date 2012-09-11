package org.scalatest
import org.scalatest.junit.JUnit3Suite
import org.scalatest.junit.JUnitSuite
import org.junit.Test
import org.testng.annotations.{Test => TestNG }
import org.scalatest.testng.TestNGSuite

class TestNameProp  extends AllSuiteProp {

  type FixtureServices = TestNameFixtureServices
  
  def suite = new ExampleTestNameSuite
  def fixtureSuite = new ExampleTestNameFixtureSuite
  def spec = new ExampleTestNameSpec
  def fixtureSpec = new ExampleTestNameFixtureSpec
  def junit3Suite = new ExampleTestNameJUnit3Suite
  def junitSuite = new ExampleTestNameJUnitSuite
  def testngSuite = new ExampleTestNameTestNGSuite
  def funSuite = new ExampleTestNameFunSuite
  def fixtureFunSuite = new ExampleTestNameFixtureFunSuite
  def funSpec = new ExampleTestNameFunSpec
  def fixtureFunSpec = new ExampleTestNameFixtureFunSpec
  def featureSpec = new ExampleTestNameFeatureSpec
  def fixtureFeatureSpec = new ExampleTestNameFixtureFeatureSpec
  def flatSpec = new ExampleTestNameFlatSpec
  def fixtureFlatSpec = new ExampleTestNameFixtureFlatSpec
  def freeSpec = new ExampleTestNameFreeSpec
  def fixtureFreeSpec = new ExampleTestNameFixtureFreeSpec
  def propSpec = new ExampleTestNamePropSpec
  def fixturePropSpec = new ExampleTestNameFixturePropSpec
  def wordSpec = new ExampleTestNameWordSpec
  def fixtureWordSpec = new ExampleTestNameFixtureWordSpec
  def pathFreeSpec = new ExampleTestNamePathFreeSpec
  def pathFunSpec = new ExampleTestNamePathFunSpec
  
  test("test name will be constructed by concatennating scopes, outer to inner, followed by the test text, separated by a space after each component is trimmed.") {
    forAll(examples) { s => s.assertTestNames() }
  }
  
}

trait TestNameFixtureServices { suite: Suite =>
  val expectedTestNames: Set[String]
  
  def assertTestNames() {
    val expectedSet = expectedTestNames
    val testNameSet = testNames
    assert(expectedSet.size === testNameSet.size)
    expectedSet.foreach { tn =>
      assert(testNameSet contains tn, "Unable to find test name: '" + tn + "', testNames is: \n" + testNameSet.map("'" + _ + "'").mkString("\n"))
    }
  }
}

@DoNotDiscover
class ExampleTestNameSuite extends Suite with TestNameFixtureServices {
  
  val expectedTestNames = 
    Set(
     "testingShouldBeFun"     
    )
    
  def testingShouldBeFun() { }
}

@DoNotDiscover
class ExampleTestNameFixtureSuite extends fixture.Suite with TestNameFixtureServices with StringFixture {
  
  val expectedTestNames = 
    Set(
     "testingShouldBeFun(FixtureParam)"     
    )
  
  def testingShouldBeFun(fixture: String) { }
}

@DoNotDiscover
class ExampleTestNameSpec extends Spec with TestNameFixtureServices {
  val expectedTestNames = 
    Set(
     "Testing 1 Scala code should be fun", 
     "Testing 2 Scala code should be fun", 
     "Testing 3 Scala code should be fun", 
     "Testing 4 Scala code should be fun", 
     "Testing 5 Scala code should be fun", 
     "Testing 6 Scala code should be fun", 
     "Testing 7 Scala code should be fun", 
     "Testing 8 Scala code should be fun", 
     "Testing 9 Scala code should be fun"
    )
  
  object `Testing 1` { // test name is "Testing 1 Scala code should be fun"
    object `Scala code` {
      def `should be fun` {}
    }
  }

  object `Testing 2 ` { // test name is "Testing 2 Scala code should be fun"
    object `Scala code` {
      def `should be fun` {}
    }
  }

  object `Testing 3` { // test name is "Testing 3 Scala code should be fun"
    object ` Scala code` {
      def `should be fun` {}
    }
  }

  object `Testing 4` { // test name is "Testing 4 Scala code should be fun"
    object `Scala code ` {
      def `should be fun` {}
    }
  }

  object `Testing 5` { // test name is "Testing 5 Scala code should be fun"
    object `Scala code` {
      def ` should be fun` {}
    }
  }

  // But a space at the beginning or end passes through:

  object ` Testing 6` { // test name is " Testing 6 Scala code should be fun"
    object `Scala code` {
      def `should be fun` {}
    }
  }

  object `Testing 7` { // test name is "Testing 7 Scala code should be fun "
    object `Scala code` {
      def `should be fun ` {}
    }
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  object `Testing 8 ` { // test name is "Testing 8  Scala code should be fun" 
    // Two spaces in name
    object ` Scala code` {
      def `should be fun` {}
    }
  }

  object `Testing 9  ` { // test name is "Testing 9  Scala code should be fun" 
    // Two spaces in name
    object `Scala code` {
      def `should be fun` {}
    }
  }
}

@DoNotDiscover
class ExampleTestNameFixtureSpec extends fixture.Spec with TestNameFixtureServices with StringFixture {
  
  val expectedTestNames = 
    Set(
     "Testing 1 Scala code should be fun", 
     "Testing 2 Scala code should be fun", 
     "Testing 3 Scala code should be fun", 
     "Testing 4 Scala code should be fun", 
     "Testing 5 Scala code should be fun", 
     "Testing 6 Scala code should be fun", 
     "Testing 7 Scala code should be fun", 
     "Testing 8 Scala code should be fun", 
     "Testing 9 Scala code should be fun"
    )
  
  object `Testing 1` { // test name is "Testing 1 Scala code should be fun"
    object `Scala code` {
      def `should be fun`(fixture: String) {}
    }
  }

  object `Testing 2 ` { // test name is "Testing 2 Scala code should be fun"
    object `Scala code` {
      def `should be fun`(fixture: String) {}
    }
  }

  object `Testing 3` { // test name is "Testing 3 Scala code should be fun"
    object ` Scala code` {
      def `should be fun`(fixture: String) {}
    }
  }

  object `Testing 4` { // test name is "Testing 4 Scala code should be fun"
    object `Scala code ` {
      def `should be fun`(fixture: String) {}
    }
  }

  object `Testing 5` { // test name is "Testing 5 Scala code should be fun"
    object `Scala code` {
      def ` should be fun`(fixture: String) {}
    }
  }

  // But a space at the beginning or end passes through:

  object ` Testing 6` { // test name is " Testing 6 Scala code should be fun"
    object `Scala code` {
      def `should be fun`(fixture: String) {}
    }
  }

  object `Testing 7` { // test name is "Testing 7 Scala code should be fun "
    object `Scala code` {
      def `should be fun `(fixture: String) {}
    }
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  object `Testing 8 ` { // test name is "Testing 8  Scala code should be fun" 
    // Two spaces in name
    object ` Scala code` {
      def `should be fun`(fixture: String) {}
    }
  }

  object `Testing 9  ` { // test name is "Testing 9  Scala code should be fun" 
    // Two spaces in name
    object `Scala code` {
      def `should be fun`(fixture: String) {}
    }
  }
}

@DoNotDiscover
class ExampleTestNameJUnit3Suite extends JUnit3Suite with TestNameFixtureServices {
  
  val expectedTestNames = 
    Set(
     "testingShouldBeFun"     
    )
    
  def testingShouldBeFun() { }
}

@DoNotDiscover
class ExampleTestNameJUnitSuite extends JUnitSuite with TestNameFixtureServices {
  
  val expectedTestNames = 
    Set(
     "testingShouldBeFun"     
    )
  
  @Test
  def testingShouldBeFun() {}
}

@DoNotDiscover
class ExampleTestNameTestNGSuite extends TestNGSuite with TestNameFixtureServices {
  
  val expectedTestNames = 
    Set(
     "testingShouldBeFun"     
    )
  
  @TestNG
  def testingShouldBeFun() {}
}

@DoNotDiscover
class ExampleTestNameFunSuite extends FunSuite with TestNameFixtureServices {
  
  val expectedTestNames = 
    Set(
     "Testing 1 should be fun",
     "Testing 2 should be fun",
     "Testing 3 should be fun",
     "Testing 4 should be fun",
     "Testing 5 should be fun"     
    )
  
  test("Testing 1 should be fun") {}
  test(" Testing 2 should be fun") {}
  test("Testing 3 should be fun ") {}
  test("  Testing 4 should be fun") {}
  test("Testing 5 should be fun  ") {}
}

@DoNotDiscover
class ExampleTestNameFixtureFunSuite extends fixture.FunSuite with TestNameFixtureServices with StringFixture {
  
  val expectedTestNames = 
    Set(
     "Testing 1 should be fun",
     "Testing 2 should be fun",
     "Testing 3 should be fun",
     "Testing 4 should be fun",
     "Testing 5 should be fun"     
    )
  
  test("Testing 1 should be fun") { s => }
  test(" Testing 2 should be fun") { s => }
  test("Testing 3 should be fun ") { s => }
  test("  Testing 4 should be fun") { s => }
  test("Testing 5 should be fun  ") { s => }
}

@DoNotDiscover
class ExampleTestNameFunSpec extends FunSpec with TestNameFixtureServices {
  val expectedTestNames = 
    Set(
     "Testing 1 Scala code should be fun", 
     "Testing 2 Scala code should be fun", 
     "Testing 3 Scala code should be fun", 
     "Testing 4 Scala code should be fun", 
     "Testing 5 Scala code should be fun", 
     "Testing 6 Scala code should be fun", 
     "Testing 7 Scala code should be fun", 
     "Testing 8 Scala code should be fun", 
     "Testing 9 Scala code should be fun"
    )
  
  describe("Testing 1") { // test name is "Testing 1 Scala code should be fun"
    describe("Scala code") {
      it("should be fun") {}
    }
  }

  describe("Testing 2 ") { // test name is "Testing 2 Scala code should be fun"
    describe("Scala code") {
      it("should be fun") {}
    }
  }

  describe("Testing 3") { // test name is "Testing 3 Scala code should be fun"
    describe(" Scala code") {
      it("should be fun") {}
    }
  }

  describe("Testing 4") { // test name is "Testing 4 Scala code should be fun"
    describe("Scala code ") {
      it("should be fun") {}
    }
  }

  describe("Testing 5") { // test name is "Testing 5 Scala code should be fun"
    describe("Scala code") {
      it(" should be fun") {}
    }
  }

  // But a space at the beginning or end passes through:

  describe(" Testing 6") { // test name is " Testing 6 Scala code should be fun"
    describe("Scala code") {
      it("should be fun") {}
    }
  }

  describe("Testing 7") { // test name is "Testing 7 Scala code should be fun "
    describe("Scala code") {
      it("should be fun ") {}
    }
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  describe("Testing 8 ") { // test name is "Testing 8  Scala code should be fun" 
    // Two spaces in name
    describe(" Scala code") {
      it("should be fun") {}
    }
  }

  describe("Testing 9  ") { // test name is "Testing 9  Scala code should be fun" 
    // Two spaces in name
    describe("Scala code") {
      it("should be fun") {}
    }
  }
}

@DoNotDiscover
class ExampleTestNameFixtureFunSpec extends fixture.FunSpec with TestNameFixtureServices with StringFixture {
  val expectedTestNames = 
    Set(
     "Testing 1 Scala code should be fun", 
     "Testing 2 Scala code should be fun", 
     "Testing 3 Scala code should be fun", 
     "Testing 4 Scala code should be fun", 
     "Testing 5 Scala code should be fun", 
     "Testing 6 Scala code should be fun", 
     "Testing 7 Scala code should be fun", 
     "Testing 8 Scala code should be fun", 
     "Testing 9 Scala code should be fun"
    )
  
  describe("Testing 1") { // test name is "Testing 1 Scala code should be fun"
    describe("Scala code") {
      it("should be fun") { s => }
    }
  }

  describe("Testing 2 ") { // test name is "Testing 2 Scala code should be fun"
    describe("Scala code") {
      it("should be fun") { s => }
    }
  }

  describe("Testing 3") { // test name is "Testing 3 Scala code should be fun"
    describe(" Scala code") {
      it("should be fun") { s => }
    }
  }

  describe("Testing 4") { // test name is "Testing 4 Scala code should be fun"
    describe("Scala code ") {
      it("should be fun") { s => }
    }
  }

  describe("Testing 5") { // test name is "Testing 5 Scala code should be fun"
    describe("Scala code") {
      it(" should be fun") { s => }
    }
  }

  // But a space at the beginning or end passes through:

  describe(" Testing 6") { // test name is " Testing 6 Scala code should be fun"
    describe("Scala code") {
      it("should be fun") { s => }
    }
  }

  describe("Testing 7") { // test name is "Testing 7 Scala code should be fun "
    describe("Scala code") {
      it("should be fun ") { s => }
    }
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  describe("Testing 8 ") { // test name is "Testing 8  Scala code should be fun" 
    // Two spaces in name
    describe(" Scala code") {
      it("should be fun") { s => }
    }
  }

  describe("Testing 9  ") { // test name is "Testing 9  Scala code should be fun" 
    // Two spaces in name
    describe("Scala code") {
      it("should be fun") { s => }
    }
  }
}

@DoNotDiscover
class ExampleTestNameFeatureSpec extends FeatureSpec with TestNameFixtureServices {
  val expectedTestNames = 
    Set(
     "Feature: Testing 1 Scenario: Scala code should be fun", 
     "Feature: Testing 2 Scenario: Scala code should be fun", 
     "Feature: Testing 3 Scenario: Scala code should be fun", 
     "Feature: Testing 4 Scenario: Scala code should be fun", 
     "Feature: Testing 5 Scenario: Scala code should be fun", 
     "Feature: Testing 6 Scenario: Scala code should be fun", 
     "Feature: Testing 7 Scenario: Scala code should be fun"
    )
  
  feature("Testing 1") { // test name is "Feature: Testing 1 Scenario: Scala code should be fun"
    scenario("Scala code should be fun") {}
  }

  feature("Testing 2 ") { // test name is "Feature: Testing 2 Scenario: Scala code should be fun"
    scenario("Scala code should be fun") {}
  }

  feature("Testing 3") { // test name is "Feature: Testing 3 Scenario: Scala code should be fun"
    scenario(" Scala code should be fun") {}
  }

  feature("Testing 4") { // test name is "Feature: Testing 4 Scenario: Scala code should be fun "
    scenario("Scala code should be fun ") {}
  }

  // But a space at the beginning or end passes through:

  feature(" Testing 5") { // test name is "Feature:  Testing 5 Scenario: Scala code should be fun"
    scenario("Scala code should be fun") {}
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  feature("Testing 6 ") { // test name is "Feature: Testing 6 Scenario: Scala code should be fun" 
    // Two spaces in name
    scenario(" Scala code should be fun") {}
  }

  feature("Testing 7  ") { // test name is "Feature: Testing 7  Scenario: Scala code should be fun" 
    // Two spaces in name
    scenario("Scala code should be fun") {}
  }
}

@DoNotDiscover
class ExampleTestNameFixtureFeatureSpec extends fixture.FeatureSpec with TestNameFixtureServices with StringFixture {
  val expectedTestNames = 
    Set(
     "Feature: Testing 1 Scenario: Scala code should be fun", 
     "Feature: Testing 2 Scenario: Scala code should be fun", 
     "Feature: Testing 3 Scenario: Scala code should be fun", 
     "Feature: Testing 4 Scenario: Scala code should be fun", 
     "Feature: Testing 5 Scenario: Scala code should be fun", 
     "Feature: Testing 6 Scenario: Scala code should be fun", 
     "Feature: Testing 7 Scenario: Scala code should be fun"
    )
  
  feature("Testing 1") { // test name is "Feature: Testing 1 Scenario: Scala code should be fun"
    scenario("Scala code should be fun") { s => }
  }

  feature("Testing 2 ") { // test name is "Feature: Testing 2 Scenario: Scala code should be fun"
    scenario("Scala code should be fun") { s => }
  }

  feature("Testing 3") { // test name is "Feature: Testing 3 Scenario: Scala code should be fun"
    scenario(" Scala code should be fun") { s => }
  }

  feature("Testing 4") { // test name is "Feature: Testing 4 Scenario: Scala code should be fun "
    scenario("Scala code should be fun ") { s => }
  }

  // But a space at the beginning or end passes through:

  feature(" Testing 5") { // test name is "Feature:  Testing 5 Scenario: Scala code should be fun"
    scenario("Scala code should be fun") { s => }
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  feature("Testing 6 ") { // test name is "Feature: Testing 6 Scenario: Scala code should be fun" 
    // Two spaces in name
    scenario(" Scala code should be fun") { s => }
  }

  feature("Testing 7  ") { // test name is "Feature: Testing 7  Scenario: Scala code should be fun" 
    // Two spaces in name
    scenario("Scala code should be fun") { s => }
  }
}

@DoNotDiscover
class ExampleTestNameFlatSpec extends FlatSpec with TestNameFixtureServices {
  
  val expectedTestNames = 
    Set(
     "Testing 1 should be fun to code in Scala", 
     "Testing 2 should be fun to code in Scala", 
     "Testing 3 should be fun to code in Scala", 
     "Testing 4 should be fun to code in Scala", 
     "Testing 5 should be fun to code in Scala", 
     "Testing 6 should be fun to code in Scala", 
     "Testing 7 should be fun to code in Scala"
    )
  
  "Testing 1" should "be fun to code in Scala" in { // test name is "Testing 1 should be fun to code in Scala"
  }

  "Testing 2 " should "be fun to code in Scala" in { // test name is "Testing 2 should be fun to code in Scala"
  }

  "Testing 3" should " be fun to code in Scala" in { // test name is "Testing 3 should be fun to code in Scala"
  }

  "Testing 4" should "be fun to code in Scala " in { // test name is "Testing 4 should be fun to code in Scala "
  }

  // But a space at the beginning or end passes through:

  " Testing 5" should "be fun to code in Scala" in { // test name is " Testing 5 should be fun to code in Scala"
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  "Testing 6 " should " be fun to code in Scala" in { // test name is "Testing 6 should be fun to code in Scala" 
  }

  "Testing 7  " should "be fun to code in Scala" in { // test name is "Testing 7  should be fun to code in Scala" 
  }
}

@DoNotDiscover
class ExampleTestNameFixtureFlatSpec extends fixture.FlatSpec with TestNameFixtureServices with StringFixture {
  val expectedTestNames = 
    Set(
     "Testing 1 should be fun to code in Scala", 
     "Testing 2 should be fun to code in Scala", 
     "Testing 3 should be fun to code in Scala", 
     "Testing 4 should be fun to code in Scala", 
     "Testing 5 should be fun to code in Scala", 
     "Testing 6 should be fun to code in Scala", 
     "Testing 7 should be fun to code in Scala"
    )
  
  "Testing 1" should "be fun to code in Scala" in { s => // test name is "Testing 1 should be fun to code in Scala"
  }

  "Testing 2 " should "be fun to code in Scala" in { s => // test name is "Testing 2 should be fun to code in Scala"
  }

  "Testing 3" should " be fun to code in Scala" in { s => // test name is "Testing 3 should be fun to code in Scala"
  }

  "Testing 4" should "be fun to code in Scala " in { s => // test name is "Testing 4 should be fun to code in Scala "
  }

  // But a space at the beginning or end passes through:

  " Testing 5" should "be fun to code in Scala" in { s => // test name is " Testing 5 should be fun to code in Scala"
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  "Testing 6 " should " be fun to code in Scala" in { s => // test name is "Testing 6 should be fun to code in Scala" 
  }

  "Testing 7  " should "be fun to code in Scala" in { s => // test name is "Testing 7  should be fun to code in Scala" 
  }
}

@DoNotDiscover
class ExampleTestNameFreeSpec extends FreeSpec with TestNameFixtureServices {
  val expectedTestNames = 
    Set(
     "Testing 1 Scala code should be fun", 
     "Testing 2 Scala code should be fun", 
     "Testing 3 Scala code should be fun", 
     "Testing 4 Scala code should be fun", 
     "Testing 5 Scala code should be fun", 
     "Testing 6 Scala code should be fun", 
     "Testing 7 Scala code should be fun", 
     "Testing 8 Scala code should be fun", 
     "Testing 9 Scala code should be fun"
    )
  
  "Testing 1" - { // test name is "Testing 1 Scala code should be fun"
    "Scala code" - {
      "should be fun" in {}
    }
  }

  "Testing 2 " - { // test name is "Testing 2 Scala code should be fun"
    "Scala code" - {
      "should be fun" in {}
    }
  }

  "Testing 3" - { // test name is "Testing 3 Scala code should be fun"
    " Scala code" - {
      "should be fun" in {}
    }
  }

  "Testing 4" - { // test name is "Testing 4 Scala code should be fun"
    "Scala code " - {
      "should be fun" in {}
    }
  }

  "Testing 5" - { // test name is "Testing 5 Scala code should be fun"
    "Scala code" - {
      " should be fun" in {}
    }
  }

  // But a space at the beginning or end passes through:

  " Testing 6" - { // test name is " Testing 6 Scala code should be fun"
    "Scala code" - {
      "should be fun" in {}
    }
  }

  "Testing 7" - { // test name is "Testing 7 Scala code should be fun "
    "Scala code" - {
      "should be fun " in {}
    }
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  "Testing 8 " - { // test name is "Testing 8  Scala code should be fun" 
    // Two spaces in name
    " Scala code" - {
      "should be fun" in {}
    }
  }

  "Testing 9  " - { // test name is "Testing 9  Scala code should be fun" 
    // Two spaces in name
    "Scala code" - {
      "should be fun" in {}
    }
  }
}

@DoNotDiscover
class ExampleTestNameFixtureFreeSpec extends fixture.FreeSpec with TestNameFixtureServices with StringFixture {
  val expectedTestNames = 
    Set(
     "Testing 1 Scala code should be fun", 
     "Testing 2 Scala code should be fun", 
     "Testing 3 Scala code should be fun", 
     "Testing 4 Scala code should be fun", 
     "Testing 5 Scala code should be fun", 
     "Testing 6 Scala code should be fun", 
     "Testing 7 Scala code should be fun", 
     "Testing 8 Scala code should be fun", 
     "Testing 9 Scala code should be fun"
    )
  
  "Testing 1" - { // test name is "Testing 1 Scala code should be fun"
    "Scala code" - {
      "should be fun" in { s => }
    }
  }

  "Testing 2 " - { // test name is "Testing 2 Scala code should be fun"
    "Scala code" - {
      "should be fun" in { s => }
    }
  }

  "Testing 3" - { // test name is "Testing 3 Scala code should be fun"
    " Scala code" - {
      "should be fun" in { s => }
    }
  }

  "Testing 4" - { // test name is "Testing 4 Scala code should be fun"
    "Scala code " - {
      "should be fun" in { s => }
    }
  }

  "Testing 5" - { // test name is "Testing 5 Scala code should be fun"
    "Scala code" - {
      " should be fun" in { s => }
    }
  }

  // But a space at the beginning or end passes through:

  " Testing 6" - { // test name is " Testing 6 Scala code should be fun"
    "Scala code" - {
      "should be fun" in { s => }
    }
  }

  "Testing 7" - { // test name is "Testing 7 Scala code should be fun "
    "Scala code" - {
      "should be fun " in { s => }
    }
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  "Testing 8 " - { // test name is "Testing 8  Scala code should be fun" 
    // Two spaces in name
    " Scala code" - {
      "should be fun" in { s => }
    }
  }

  "Testing 9  " - { // test name is "Testing 9  Scala code should be fun" 
    // Two spaces in name
    "Scala code" - {
      "should be fun" in { s => }
    }
  }
}

@DoNotDiscover
class ExampleTestNamePropSpec extends PropSpec with TestNameFixtureServices {
  
  val expectedTestNames = 
    Set(
     "Testing 1 Scala code should be fun",
     "Testing 2 Scala code should be fun",
     "Testing 3 Scala code should be fun",
     "Testing 4 Scala code should be fun",
     "Testing 5 Scala code should be fun",
     "Testing 6 Scala code should be fun"
    )
  
  property("Testing 1 Scala code should be fun") {}
  property(" Testing 2 Scala code should be fun") {}
  property("Testing 3 Scala code should be fun ") {}
  property("  Testing 4 Scala code should be fun") {}
  property("Testing 5 Scala code should be fun  ") {}
  property("  Testing 6 Scala code should be fun  ") {}
}

@DoNotDiscover
class ExampleTestNameFixturePropSpec extends fixture.PropSpec with TestNameFixtureServices with StringFixture {
  
  val expectedTestNames = 
    Set(
     "Testing 1 Scala code should be fun",
     "Testing 2 Scala code should be fun",
     "Testing 3 Scala code should be fun",
     "Testing 4 Scala code should be fun",
     "Testing 5 Scala code should be fun",
     "Testing 6 Scala code should be fun"
    )
  
  property("Testing 1 Scala code should be fun") { s => }
  property(" Testing 2 Scala code should be fun") { s => }
  property("Testing 3 Scala code should be fun ") { s => }
  property("  Testing 4 Scala code should be fun") { s => }
  property("Testing 5 Scala code should be fun  ") { s => }
  property("  Testing 6 Scala code should be fun  ") { s => }
}

@DoNotDiscover
class ExampleTestNameWordSpec extends WordSpec with TestNameFixtureServices {
  val expectedTestNames = 
    Set(
     "Testing 1 should test Scala code should be fun", 
     "Testing 2 should test Scala code should be fun", 
     "Testing 3 should test Scala code should be fun", 
     "Testing 4 should test Scala code should be fun", 
     "Testing 5 should test Scala code should be fun", 
     "Testing 6 should test Scala code should be fun", 
     "Testing 7 should test Scala code should be fun", 
     "Testing 8 should test Scala code should be fun", 
     "Testing 9 should test Scala code should be fun"
    )
  
  "Testing 1" should { // test name is "Testing 1 should test Scala code should be fun"
    "test Scala code" should {
      "be fun" in {}
    }
  }

  "Testing 2 " should { // test name is "Testing 2 should test Scala code should be fun"
    "test Scala code" should {
      "be fun" in {}
    }
  }

  "Testing 3" should { // test name is "Testing 3 should test Scala code should be fun"
    " test Scala code" should {
      "be fun" in {}
    }
  }

  "Testing 4" should { // test name is "Testing 4 should test Scala code should be fun"
    "test Scala code " should {
      "be fun" in {}
    }
  }

  "Testing 5" should { // test name is "Testing 5 should test Scala code should be fun"
    "test Scala code" should {
      " be fun" in {}
    }
  }

  // But a space at the beginning or end passes through:

  " Testing 6" should { // test name is " Testing 6 should test Scala code should be fun"
    "test Scala code" should {
      "be fun" in {}
    }
  }

  "Testing 7" should { // test name is "Testing 7 should test Scala code should be fun "
    "test Scala code" should {
      "be fun " in {}
    }
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  "Testing 8 " should { // test name is "Testing 8 should test Scala code should be fun" 
    // Two spaces in name
    " test Scala code" should {
      "be fun" in {}
    }
  }

  "Testing 9  " should { // test name is "Testing 9  should test Scala code should be fun" 
    // Two spaces in name
    "test Scala code" should {
      "be fun" in {}
    }
  }
}

@DoNotDiscover
class ExampleTestNameFixtureWordSpec extends fixture.WordSpec with TestNameFixtureServices with StringFixture {
  val expectedTestNames = 
    Set(
     "Testing 1 should test Scala code should be fun", 
     "Testing 2 should test Scala code should be fun", 
     "Testing 3 should test Scala code should be fun", 
     "Testing 4 should test Scala code should be fun", 
     "Testing 5 should test Scala code should be fun", 
     "Testing 6 should test Scala code should be fun", 
     "Testing 7 should test Scala code should be fun", 
     "Testing 8 should test Scala code should be fun", 
     "Testing 9 should test Scala code should be fun"
    )
  
  "Testing 1" should { // test name is "Testing 1 should test Scala code should be fun"
    "test Scala code" should {
      "be fun" in { s => }
    }
  }

  "Testing 2 " should { // test name is "Testing 2 should test Scala code should be fun"
    "test Scala code" should {
      "be fun" in { s => }
    }
  }

  "Testing 3" should { // test name is "Testing 3 should test Scala code should be fun"
    " test Scala code" should {
      "be fun" in { s => }
    }
  }

  "Testing 4" should { // test name is "Testing 4 should test Scala code should be fun"
    "test Scala code " should {
      "be fun" in { s => }
    }
  }

  "Testing 5" should { // test name is "Testing 5 should test Scala code should be fun"
    "test Scala code" should {
      " be fun" in { s => }
    }
  }

  // But a space at the beginning or end passes through:

  " Testing 6" should { // test name is " Testing 6 should test Scala code should be fun"
    "test Scala code" should {
      "be fun" in { s => }
    }
  }

  "Testing 7" should { // test name is "Testing 7 should test Scala code should be fun "
    "test Scala code" should {
      "be fun " in { s => }
    }
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  "Testing 8 " should { // test name is "Testing 8 should test Scala code should be fun" 
    // Two spaces in name
    " test Scala code" should {
      "be fun" in { s => }
    }
  }

  "Testing 9  " should { // test name is "Testing 9  should test Scala code should be fun" 
    // Two spaces in name
    "test Scala code" should {
      "be fun" in { s => }
    }
  }
}

@DoNotDiscover
class ExampleTestNamePathFreeSpec extends path.FreeSpec with TestNameFixtureServices {
  val expectedTestNames = 
    Set(
     "Testing 1 Scala code should be fun", 
     "Testing 2 Scala code should be fun", 
     "Testing 3 Scala code should be fun", 
     "Testing 4 Scala code should be fun", 
     "Testing 5 Scala code should be fun", 
     "Testing 6 Scala code should be fun", 
     "Testing 7 Scala code should be fun", 
     "Testing 8 Scala code should be fun", 
     "Testing 9 Scala code should be fun"
    )
  
  "Testing 1" - { // test name is "Testing 1 Scala code should be fun"
    "Scala code" - {
      "should be fun" in {}
    }
  }

  "Testing 2 " - { // test name is "Testing 2 Scala code should be fun"
    "Scala code" - {
      "should be fun" in {}
    }
  }

  "Testing 3" - { // test name is "Testing 3 Scala code should be fun"
    " Scala code" - {
      "should be fun" in {}
    }
  }

  "Testing 4" - { // test name is "Testing 4 Scala code should be fun"
    "Scala code " - {
      "should be fun" in {}
    }
  }

  "Testing 5" - { // test name is "Testing 5 Scala code should be fun"
    "Scala code" - {
      " should be fun" in {}
    }
  }

  // But a space at the beginning or end passes through:

  " Testing 6" - { // test name is " Testing 6 Scala code should be fun"
    "Scala code" - {
      "should be fun" in {}
    }
  }

  "Testing 7" - { // test name is "Testing 7 Scala code should be fun "
    "Scala code" - {
      "should be fun " in {}
    }
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  "Testing 8 " - { // test name is "Testing 8  Scala code should be fun" 
    // Two spaces in name
    " Scala code" - {
      "should be fun" in {}
    }
  }

  "Testing 9  " - { // test name is "Testing 9  Scala code should be fun" 
    // Two spaces in name
    "Scala code" - {
      "should be fun" in {}
    }
  }
}

@DoNotDiscover
class ExampleTestNamePathFunSpec extends path.FunSpec with TestNameFixtureServices {
  val expectedTestNames = 
    Set(
     "Testing 1 Scala code should be fun", 
     "Testing 2 Scala code should be fun", 
     "Testing 3 Scala code should be fun", 
     "Testing 4 Scala code should be fun", 
     "Testing 5 Scala code should be fun", 
     "Testing 6 Scala code should be fun", 
     "Testing 7 Scala code should be fun", 
     "Testing 8 Scala code should be fun", 
     "Testing 9 Scala code should be fun"
    )
  
  describe("Testing 1") { // test name is "Testing 1 Scala code should be fun"
    describe("Scala code") {
      it("should be fun") {}
    }
  }

  describe("Testing 2 ") { // test name is "Testing 2 Scala code should be fun"
    describe("Scala code") {
      it("should be fun") {}
    }
  }

  describe("Testing 3") { // test name is "Testing 3 Scala code should be fun"
    describe(" Scala code") {
      it("should be fun") {}
    }
  }

  describe("Testing 4") { // test name is "Testing 4 Scala code should be fun"
    describe("Scala code ") {
      it("should be fun") {}
    }
  }

  describe("Testing 5") { // test name is "Testing 5 Scala code should be fun"
    describe("Scala code") {
      it(" should be fun") {}
    }
  }

  // But a space at the beginning or end passes through:

  describe(" Testing 6") { // test name is " Testing 6 Scala code should be fun"
    describe("Scala code") {
      it("should be fun") {}
    }
  }

  describe("Testing 7") { // test name is "Testing 7 Scala code should be fun "
    describe("Scala code") {
      it("should be fun ") {}
    }
  }

  // Two spaces will be maintained. The only thing we do is opt to not
  // add a space if there's one already there.
  // That's a simple rule.

  describe("Testing 8 ") { // test name is "Testing 8  Scala code should be fun" 
    // Two spaces in name
    describe(" Scala code") {
      it("should be fun") {}
    }
  }

  describe("Testing 9  ") { // test name is "Testing 9  Scala code should be fun" 
    // Two spaces in name
    describe("Scala code") {
      it("should be fun") {}
    }
  }
}
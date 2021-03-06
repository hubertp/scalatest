/*
 * Copyright 2001-2009 Artima, Inc.
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
package org.scalatest.fixture

import org.scalatest._
import scala.collection.immutable.ListSet
import org.scalatest.Suite.autoTagClassAnnotations

/**
 * A sister trait to <code>org.scalatest.PropSpec</code> that can pass a fixture object into its tests.
 *
 * <table><tr><td class="usage">
 * <strong>Recommended Usage</strong>:
 * Use trait <code>fixture.PropSpec</code> in situations for which <a href="../PropSpec.html"><code>PropSpec</code></a>
 * would be a good choice, when all or most tests need the same fixture objects
 * that must be cleaned up afterwords. <em>Note: <code>fixture.PropSpec</code> is intended for use in special
 * situations, with trait <code>PropSpec</code> used for general needs. For
 * more insight into where <code>fixture.PropSpec</code> fits in the big picture, see
 * the <a href="../PropSpec.html#withFixtureOneArgTest"><code>withFixture(OneArgTest)</code></a> subsection of
 * the <a href="../PropSpec.html#sharedFixtures">Shared fixtures</a> section in the documentation for trait <code>PropSpec</code>.</em>
 * </td></tr></table>
 * 
 * <p>
 * Trait <code>fixture.PropSpec</code> behaves similarly to trait <code>org.scalatest.PropSpec</code>, except that tests may have a
 * fixture parameter. The type of the
 * fixture parameter is defined by the abstract <code>FixtureParam</code> type, which is declared as a member of this trait.
 * This trait also declares an abstract <code>withFixture</code> method. This <code>withFixture</code> method
 * takes a <code>OneArgTest</code>, which is a nested trait defined as a member of this trait.
 * <code>OneArgTest</code> has an <code>apply</code> method that takes a <code>FixtureParam</code>.
 * This <code>apply</code> method is responsible for running a test.
 * This trait's <code>runTest</code> method delegates the actual running of each test to <code>withFixture</code>, passing
 * in the test code to run via the <code>OneArgTest</code> argument. The <code>withFixture</code> method (abstract in this trait) is responsible
 * for creating the fixture argument and passing it to the test function.
 * </p>
 * 
 * <p>
 * Subclasses of this trait must, therefore, do three things differently from a plain old <code>org.scalatest.PropSpec</code>:
 * </p>
 * 
 * <ol>
 * <li>define the type of the fixture parameter by specifying type <code>FixtureParam</code></li>
 * <li>define the <code>withFixture(OneArgTest)</code> method</li>
 * <li>write tests that take a fixture parameter</li>
 * <li>(You can also define tests that don't take a fixture parameter.)</li>
 * </ol>
 *
 * <p>
 * Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.fixture.propspec
 * 
 * import org.scalatest._
 * import prop.PropertyChecks
 * import java.io._
 * 
 * class ExampleSpec extends fixture.PropSpec with PropertyChecks with ShouldMatchers {
 * 
 *   // 1. define type FixtureParam
 *   type FixtureParam = FileReader
 * 
 *   // 2. define the withFixture method
 *   def withFixture(test: OneArgTest) {
 * 
 *     val FileName = "TempFile.txt"
 * 
 *     // Set up the temp file needed by the test
 *     val writer = new FileWriter(FileName)
 *     try {
 *       writer.write("Hello, test!")
 *     }
 *     finally {
 *       writer.close()
 *     }
 * 
 *     // Create the reader needed by the test
 *     val reader = new FileReader(FileName)
 * 
 *     try {
 *       // Run the test using the temp file
 *       test(reader)
 *     }
 *     finally {
 *       // Close and delete the temp file
 *       reader.close()
 *       val file = new File(FileName)
 *       file.delete()
 *     }
 *   }
 * 
 *   // 3. write property-based tests that take a fixture parameter
 *   // (Hopefully less contrived than the examples shown here.)
 *   property("can read from a temp file") { reader =&gt;
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     val fileContents = builder.toString
 *     forAll { (c: Char) =&gt;
 *       whenever (c != 'H') {
 *         fileContents should not startWith c.toString
 *       }
 *     }
 *   }
 * 
 *   property("can read the first char of the temp file") { reader =&gt;
 *     val firstChar = reader.read()
 *     forAll { (c: Char) =&gt;
 *       whenever (c != 'H') {
 *         c should not equal firstChar
 *       }
 *     }
 *   }
 * 
 *   // (You can also write tests that don't take a fixture parameter.)
 *   property("can write tests that don't take the fixture") { () =&gt;
 *     forAll { (i: Int) => i + i should equal (2 * i) }
 *   }
 * }
 * </pre>
 *
 * <p>
 * Note: to run the examples on this page, you'll need to include <a href="http://www.scalacheck.org">ScalaCheck</a> on the classpath in addition to ScalaTest.
 * </p>
 *
 * <p>
 * In the previous example, <code>withFixture</code> creates and initializes a temp file, then invokes the test function,
 * passing in a <code>FileReader</code> connected to that file.  In addition to setting up the fixture before a test,
 * the <code>withFixture</code> method also cleans it up afterwards. If you need to do some clean up
 * that must happen even if a test fails, you should invoke the test function from inside a <code>try</code> block and do
 * the cleanup in a <code>finally</code> clause, as shown in the previous example.
 * </p>
 *
 * <p>
 * The reason you must perform cleanup in a <code>finally</code> clause is that <code>withFixture</code> is called by
 * <code>runTest</code>, which expects an exception to be thrown to indicate a failed test. Thus when you invoke
 * the <code>test</code> function, it may complete abruptly with an exception. The <code>finally</code> clause will
 * ensure the fixture cleanup happens as that exception propagates back up the call stack to <code>runTest</code>.
 * </p>
 *
 * <p>
 * If a test doesn't need the fixture, you can indicate that by providing a no-arg instead of a one-arg function.
 * In other words, instead of starting your function literal
 * with something like &ldquo;<code>reader =&gt;</code>&rdquo;, you'd start it with &ldquo;<code>() =&gt;</code>&rdquo;, as is done
 * in the third test in the above example. For such tests, <code>runTest</code>
 * will not invoke <code>withFixture(OneArgTest)</code>. It will instead directly invoke <code>withFixture(NoArgTest)</code>.
 * </p>
 *
 * <a name="multipleFixtures"></a>
 * <h2>Passing multiple fixture objects</h2>
 *
 * <p>
 * If the fixture you want to pass into your tests consists of multiple objects, you will need to combine
 * them into one object to use this trait. One good approach to passing multiple fixture objects is
 * to encapsulate them in a case class. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * case class F(builder: StringBuilder, buffer: ListBuffer[String])
 * type FixtureParam = F
 * </pre>
 *
 * <p>
 * To enable the stacking of traits that define <code>withFixture(NoArgTest)</code>, it is a good idea to let
 * <code>withFixture(NoArgTest)</code> invoke the test function instead of invoking the test
 * function directly. To do so, you'll need to convert the <code>OneArgTest</code> to a <code>NoArgTest</code>. You can do that by passing
 * the fixture object to the <code>toNoArgTest</code> method of <code>OneArgTest</code>. In other words, instead of
 * writing &ldquo;<code>test(theFixture)</code>&rdquo;, you'd delegate responsibility for
 * invoking the test function to the <code>withFixture(NoArgTest)</code> method of the same instance by writing:
 * </p>
 *
 * <pre>
 * withFixture(test.toNoArgTest(theFixture))
 * </pre>
 *
 * <p>
 * Here's a complete example:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.fixture.propspec.multi
 * 
 * import org.scalatest._
 * import prop.PropertyChecks
 * import scala.collection.mutable.ListBuffer
 * 
 * class ExampleSpec extends fixture.PropSpec with PropertyChecks with ShouldMatchers {
 * 
 *   case class F(builder: StringBuilder, buffer: ListBuffer[String])
 *   type FixtureParam = F
 * 
 *   def withFixture(test: OneArgTest) {
 * 
 *     // Create needed mutable objects
 *     val stringBuilder = new StringBuilder("ScalaTest is ")
 *     val listBuffer = new ListBuffer[String]
 *     val theFixture = F(stringBuilder, listBuffer)
 * 
 *     // Invoke the test function, passing in the mutable objects
 *     withFixture(test.toNoArgTest(theFixture))
 *   }
 * 
 *   property("testing should be easy") { f =&gt;
 *     f.builder.append("easy!")
 *     assert(f.builder.toString === "ScalaTest is easy!")
 *     assert(f.buffer.isEmpty)
 *     val firstChar = f.builder(0)
 *     forAll { (c: Char) =&gt;
 *       whenever (c != 'S') {
 *         c should not equal firstChar
 *       }
 *     }
 *     f.buffer += "sweet"
 *   }
 * 
 *   property("testing should be fun") { f =&gt;
 *     f.builder.append("fun!")
 *     assert(f.builder.toString === "ScalaTest is fun!")
 *     assert(f.buffer.isEmpty)
 *     val firstChar = f.builder(0)
 *     forAll { (c: Char) =&gt;
 *       whenever (c != 'S') {
 *         c should not equal firstChar
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * @author Bill Venners
 */
@Finders(Array("org.scalatest.finders.PropSpecFinder"))
trait PropSpec extends Suite { thisSuite =>

  private final val engine = new FixtureEngine[FixtureParam]("concurrentFixturePropSpecMod", "FixturePropSpec")
  import engine._
  
  private[scalatest] val sourceFileName = "PropSpec.scala"

  /**
   * Returns an <code>Informer</code> that during test execution will forward strings (and other objects) passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked while this
   * <code>fixture.PropSpec</code> is being executed, such as from inside a test function, it will forward the information to
   * the current reporter immediately. If invoked at any other time, it will
   * throw an exception. This method can be called safely by any thread.
   */
  implicit protected def info: Informer = atomicInformer.get

  /**
   * Register a property-based test with the specified name, optional tags, and function value that takes no arguments.
   * This method will register the test for later execution via an invocation of one of the <code>run</code>
   * methods. The passed test name must not have been registered previously on
   * this <code>PropSpec</code> instance.
   *
   * @param testName the name of the test
   * @param testTags the optional list of tags for this test
   * @param testFun the test function
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws NotAllowedException if <code>testName</code> had been registered previously
   * @throws NullPointerException if <code>testName</code> or any passed test tag is <code>null</code>
   */
  protected def property(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    registerTest(testName, testFun, "testCannotAppearInsideAnotherTest", sourceFileName, "property", 4, -2, None, None, None, testTags: _*)
  }

  /**
   * Register a property-based test to ignore, which has the specified name, optional tags, and function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>run</code>
   * methods. This method exists to make it easy to ignore an existing test by changing the call to <code>test</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be run, but a
   * report will be sent that indicates the test was ignored. The passed test name must not have been registered previously on
   * this <code>PropSpec</code> instance.
   *
   * @param testName the name of the test
   * @param testTags the optional list of tags for this test
   * @param testFun the test function
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws NotAllowedException if <code>testName</code> had been registered previously
   */
  protected def ignore(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    registerIgnoredTest(testName, testFun, "ignoreCannotAppearInsideATest", sourceFileName, "ignore", 4, 2, None, testTags: _*)
  }

  /**
  * An immutable <code>Set</code> of test names. If this <code>fixture.PropSpec</code> contains no tests, this method returns an empty <code>Set</code>.
  *
  * <p>
  * This trait's implementation of this method will return a set that contains the names of all registered tests. The set's iterator will
  * return those names in the order in which the tests were registered.
  * </p>
  */
  override def testNames: Set[String] = {
    // I'm returning a ListSet here so that they tests will be run in registration order
    ListSet(atomic.get.testNamesList.toArray: _*)
  }

  /**
   * Run a test. This trait's implementation runs the test registered with the name specified by <code>testName</code>.
   *
   * @param testName the name of one test to run.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param configMap a <code>Map</code> of properties that can be used by the executing <code>Suite</code> of tests.
   * @throws IllegalArgumentException if <code>testName</code> is defined but a test with that name does not exist on this <code>fixture.PropSpec</code>
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, or <code>configMap</code>
   *     is <code>null</code>.
   */
  protected override def runTest(testName: String, args: Args): Status = {

    def invokeWithFixture(theTest: TestLeaf) {
      theTest.testFun match {
        case wrapper: NoArgTestWrapper[_] =>
          withFixture(new FixturelessTestFunAndConfigMap(testName, wrapper.test, args.configMap))
        case fun => withFixture(new TestFunAndConfigMap(testName, fun, args.configMap))
      }
    }

    runTestImpl(thisSuite, testName, args, true, invokeWithFixture)
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> tag names to which tests in this <code>fixture.PropSpec</code> belong, and values
   * the <code>Set</code> of test names that belong to each tag. If this <code>fixture.PropSpec</code> contains no tags, this method returns an empty
   * <code>Map</code>.
   *
   * <p>
   * This trait's implementation returns tags that were passed as strings contained in <code>Tag</code> objects passed to
   * methods <code>test</code> and <code>ignore</code>.
   * </p>
   * 
   * <p>
   * In addition, this trait's implementation will also auto-tag tests with class level annotations.  
   * For example, if you annotate @Ignore at the class level, all test methods in the class will be auto-annotated with @Ignore.
   * </p>
   */
  override def tags: Map[String, Set[String]] = autoTagClassAnnotations(atomic.get.tagsMap, this)

  protected override def runTests(testName: Option[String], args: Args): Status = {
    runTestsImpl(thisSuite, testName, args, info, true, runTest)
  }

  override def run(testName: Option[String], args: Args): Status = {
    runImpl(thisSuite, testName, args, super.run)
  }

  /**
   * Registers shared tests.
   *
   * <p>
   * This method enables the following syntax for shared tests in a <code>fixture.PropSpec</code>:
   * </p>
   *
   * <pre class="stHighlight">
   * propertiesFor(nonEmptyStack(lastValuePushed))
   * </pre>
   *
   * <p>
   * This method just provides syntax sugar intended to make the intent of the code clearer.
   * Because the parameter passed to it is
   * type <code>Unit</code>, the expression will be evaluated before being passed, which
   * is sufficient to register the shared tests. For examples of shared tests, see the
   * <a href="../PropSpec.html#SharedTests">Shared tests section</a> in the main documentation for
   * trait <code>PropSpec</code>.
   * </p>
   */
  protected def propertiesFor(unit: Unit) {}

  @deprecated("Use propertiesFor instead.")
  protected def testsFor(unit: Unit) {}

  /**
   * Implicitly converts a function that takes no parameters and results in <code>PendingNothing</code> to
   * a function from <code>FixtureParam</code> to <code>Any</code>, to enable pending tests to registered as by-name parameters
   * by methods that require a test function that takes a <code>FixtureParam</code>.
   *
   * <p>
   * This method makes it possible to write pending tests as simply <code>(pending)</code>, without needing
   * to write <code>(fixture => pending)</code>.
   * </p>
   */
  protected implicit def convertPendingToFixtureFunction(f: => PendingNothing): (FixtureParam => Any) = {
    fixture => f
  }

  /**
   * Implicitly converts a function that takes no parameters and results in <code>Any</code> to
   * a function from <code>FixtureParam</code> to <code>Any</code>, to enable no-arg tests to registered
   * by methods that require a test function that takes a <code>FixtureParam</code>.
   */
  protected implicit def convertNoArgToFixtureFunction(fun: () => Any): (FixtureParam => Any) =
    new NoArgTestWrapper(fun)
  
  /**
   * Suite style name.
   */
  final override val styleName: String = "org.scalatest.fixture.PropSpec"
    
  override def testDataFor(testName: String, theConfigMap: Map[String, Any] = Map.empty): TestData = createTestDataFor(testName, theConfigMap, this)
}

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
import FixtureNodeFamily._
import scala.collection.immutable.ListSet
import org.scalatest.exceptions.StackDepthExceptionHelper.getStackDepthFun
import java.util.concurrent.atomic.AtomicReference
import java.util.ConcurrentModificationException
import org.scalatest.events._
import org.scalatest.Suite.anErrorThatShouldCauseAnAbort
import org.scalatest.Suite.autoTagClassAnnotations
import verb.BehaveWord
import FunSuite.IgnoreTagName 
import org.scalatest.exceptions.TestRegistrationClosedException

/**
 * A sister trait to <code>org.scalatest.FunSpec</code> that can pass a fixture object into its tests.
 *
 * <table><tr><td class="usage">
 * <strong>Recommended Usage</strong>:
 * Use trait <code>fixture.FunSpec</code> in situations for which <a href="../FunSpec.html"><code>FunSpec</code></a>
 * would be a good choice, when all or most tests need the same fixture objects
 * that must be cleaned up afterwords. <em>Note: <code>fixture.FunSpec</code> is intended for use in special situations, with trait <code>FunSpec</code> used for general needs. For
 * more insight into where <code>fixture.FunSpec</code> fits in the big picture, see the <a href="../FunSpec.html#withFixtureOneArgTest"><code>withFixture(OneArgTest)</code></a> subsection of the <a href="../FunSpec.html#sharedFixtures">Shared fixtures</a> section in the documentation for trait <code>FunSpec</code>.</em>
 * </td></tr></table>
 * 
 * <p>
 * Trait <code>fixture.FunSpec</code> behaves similarly to trait <code>org.scalatest.FunSpec</code>, except that tests may have a
 * fixture parameter. The type of the
 * fixture parameter is defined by the abstract <code>FixtureParam</code> type, which is declared as a member of this trait.
 * This trait also declares an abstract <code>withFixture</code> method. This <code>withFixture</code> method
 * takes a <code>OneArgTest</code>, which is a nested trait defined as a member of this trait.
 * <code>OneArgTest</code> has an <code>apply</code> method that takes a <code>FixtureParam</code>.
 * This <code>apply</code> method is responsible for running a test.
 * This trait's <code>runTest</code> method delegates the actual running of each test to <code>withFixture(OneArgTest)</code>, passing
 * in the test code to run via the <code>OneArgTest</code> argument. The <code>withFixture(OneArgTest)</code> method (abstract in this trait) is responsible
 * for creating the fixture argument and passing it to the test function.
 * </p>
 * 
 * <p>
 * Subclasses of this trait must, therefore, do three things differently from a plain old <code>org.scalatest.FunSpec</code>:
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
 * If the fixture you want to pass into your tests consists of multiple objects, you will need to combine
 * them into one object to use this trait. One good approach to passing multiple fixture objects is
 * to encapsulate them in a case class. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * case class F(file: File, writer: FileWriter)
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
 * package org.scalatest.examples.funspec.oneargtest
 * 
 * import org.scalatest.fixture
 * import java.io._
 * 
 * class ExampleSpec extends fixture.FunSpec {
 * 
 *   case class F(file: File, writer: FileWriter)
 *   type FixtureParam = F
 * 
 *   def withFixture(test: OneArgTest) {
 * 
 *     // create the fixture
 *     val file = File.createTempFile("hello", "world")
 *     val writer = new FileWriter(file)
 *     val theFixture = F(file, writer)
 * 
 *     try {
 *       writer.write("ScalaTest is ") // set up the fixture
 *       withFixture(test.toNoArgTest(theFixture)) // "loan" the fixture to the test
 *     }
 *     finally writer.close() // clean up the fixture
 *   }
 * 
 *   describe("Testing") {
 *     it("should be easy") { f =&gt;
 *       f.writer.write("easy!")
 *       f.writer.flush()
 *       assert(f.file.length === 18)
 *     }
 * 
 *     it("should be fun") { f =&gt;
 *       f.writer.write("fun!")
 *       f.writer.flush()
 *       assert(f.file.length === 17)
 *     }
 *   } 
 * }
 * </pre>
 *
 * <p>
 * If a test fails, the <code>OneArgTest</code> function will complete abruptly with an exception describing the failure.
 * To ensure clean up happens even if a test fails, you should invoke the test function from inside a <code>try</code> block and do the cleanup in a
 * <code>finally</code> clause, as shown in the previous example.
 * </p>
 *
 * <a name="sharingFixturesAcrossClasses"></a><h2>Sharing fixtures across classes</h2>
 *
 * <p>
 * If multiple test classes need the same fixture, you can define the <code>FixtureParam</code> and <code>withFixture(OneArgTest)</code> implementations
 * in a trait, then mix that trait into the test classes that need it. For example, if your application requires a database and your integration tests
 * use that database, you will likely have many test classes that need a database fixture. You can create a "database fixture" trait that creates a
 * database with a unique name, passes the connector into the test, then removes the database once the test completes. This is shown in the following example:
 * </p>
 * 
 * <pre class="stHighlight">
 * package org.scalatest.examples.fixture.funspec.sharing
 * 
 * import java.util.concurrent.ConcurrentHashMap
 * import org.scalatest.fixture
 * import DbServer._
 * import java.util.UUID.randomUUID
 * 
 * object DbServer { // Simulating a database server
 *   type Db = StringBuffer
 *   private val databases = new ConcurrentHashMap[String, Db]
 *   def createDb(name: String): Db = {
 *     val db = new StringBuffer
 *     databases.put(name, db)
 *     db
 *   }
 *   def removeDb(name: String) {
 *     databases.remove(name)
 *   }
 * }
 * 
 * trait DbFixture { this: fixture.Suite =&gt;
 * 
 *   type FixtureParam = Db
 * 
 *   // Allow clients to populate the database after
 *   // it is created
 *   def populateDb(db: Db) {}
 * 
 *   def withFixture(test: OneArgTest) {
 *     val dbName = randomUUID.toString
 *     val db = createDb(dbName) // create the fixture
 *     try {
 *       populateDb(db) // setup the fixture
 *       withFixture(test.toNoArgTest(db)) // "loan" the fixture to the test
 *     }
 *     finally removeDb(dbName) // clean up the fixture
 *   }
 * }
 * 
 * class ExampleSpec extends fixture.FunSpec with DbFixture {
 * 
 *   override def populateDb(db: Db) { // setup the fixture
 *     db.append("ScalaTest is ")
 *   }
 * 
 *   describe("Testing") {
 *     it("should be easy") { db =&gt;
 *       db.append("easy!")
 *       assert(db.toString === "ScalaTest is easy!")
 *     }
 *     
 *     it("should be fun") { db =&gt;
 *       db.append("fun!")
 *       assert(db.toString === "ScalaTest is fun!")
 *     }
 *   }
 *   
 *   // This test doesn't need a Db
 *   describe("Test code") {
 *     it("should be clear") { () =&gt;
 *       val buf = new StringBuffer
 *       buf.append("ScalaTest code is ")
 *       buf.append("clear!")
 *       assert(buf.toString === "ScalaTest code is clear!")
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * Often when you create fixtures in a trait like <code>DbFixture</code>, you'll still need to enable individual test classes
 * to "setup" a newly created fixture before it gets passed into the tests. A good way to accomplish this is to pass the newly
 * created fixture into a setup method, like <code>populateDb</code> in the previous example, before passing it to the test
 * function. Classes that need to perform such setup can override the method, as does <code>ExampleSpec</code>.
 * </p>
 *
 * <p>
 * If a test doesn't need the fixture, you can indicate that by providing a no-arg instead of a one-arg function, as is done in the
 * third test in the previous example, &ldquo;<code>Test code should be clear</code>&rdquo;. In other words, instead of starting your function literal
 * with something like &ldquo;<code>db =&gt;</code>&rdquo;, you'd start it with &ldquo;<code>() =&gt;</code>&rdquo;. For such tests, <code>runTest</code>
 * will not invoke <code>withFixture(OneArgTest)</code>. It will instead directly invoke <code>withFixture(NoArgTest)</code>.
 * </p>
 *
 * <p>
 * Both examples shown above demonstrate the technique of giving each test its own "fixture sandbox" to play in. When your fixtures
 * involve external side-effects, like creating files or databases, it is a good idea to give each file or database a unique name as is
 * done in these examples. This keeps tests completely isolated, allowing you to run them in parallel if desired. You could mix
 * <code>ParallelTestExecution</code> into either of these <code>ExampleSpec</code> classes, and the tests would run in parallel just fine.
 * </p>
 *
 * @author Bill Venners
 */
@Finders(Array("org.scalatest.finders.FunSpecFinder"))
trait FunSpec extends Suite { thisSuite =>

  private final val engine = new FixtureEngine[FixtureParam]("concurrentFixtureSpecMod", "FixtureFunSpec")
  import engine._
  
  private[scalatest] val sourceFileName = "FunSpec.scala"

  /**
   * Returns an <code>Informer</code> that during test execution will forward strings (and other objects) passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked while this
   * <code>FunSpec</code> is being executed, such as from inside a test function, it will forward the information to
   * the current reporter immediately. If invoked at any other time, it will
   * throw an exception. This method can be called safely by any thread.
   */
  implicit protected def info: Informer = atomicInformer.get

  /**
   * Class that, via an instance referenced from the <code>it</code> field,
   * supports test (and shared test) registration in <code>FunSpec</code>s.
   *
   * <p>
   * This class supports syntax such as the following:
   * </p>
   *
   * <pre class="stHighlight">
   * it("should be empty")
   * ^
   * </pre>
   *
   * <pre class="stHighlight">
   * it should behave like nonFullStack(stackWithOneItem)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples, see the <a href="../FunSpec.html">main documentation for <code>FunSpec</code></a>.
   * </p>
   */
  protected final class ItWord {

    /**
     * Register a test with the given spec text, optional tags, and test function value that takes no arguments.
     * An invocation of this method is called an &#8220;example.&#8221;
     *
     * This method will register the test for later execution via an invocation of one of the <code>execute</code>
     * methods. The name of the test will be a concatenation of the text of all surrounding describers,
     * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
     * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
     * this <code>FunSpec</code> instance.
     *
     * @param specText the specification text, which will be combined with the descText of any surrounding describers
     * to form the test name
     * @param testTags the optional list of tags for this test
     * @param testFun the test function
     * @throws DuplicateTestNameException if a test with the same name has been registered previously
     * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
     * @throws NullPointerException if <code>specText</code> or any passed test tag is <code>null</code>
     */
    def apply(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) {
      registerTest(specText, testFun, "itCannotAppearInsideAnotherIt", sourceFileName, "apply", 3, -2, None, None, None, testTags: _*)
    }

    /**
     * Supports the registration of shared tests.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it should behave like nonFullStack(stackWithOneItem)
     *    ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="../Spec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FunSpec</code>.
     * </p>
     */
    def should(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must behave like nonFullStack(stackWithOneItem)
     *    ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="../Spec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FunSpec</code>.
     * </p>
     */
    def must(behaveWord: BehaveWord) = behaveWord
  }

  /**
   * Supports test (and shared test) registration in <code>FunSpec</code>s.
   *
   * <p>
   * This field supports syntax such as the following:
   * </p>
   *
   * <pre class="stHighlight">
   * it("should be empty")
   * ^
   * </pre>
   *
   * <pre class="stHighlight">
   * it should behave like nonFullStack(stackWithOneItem)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see
   * the <a href="../FunSpec.html">main documentation for <code>FunSpec</code></a>.
   * </p>
   */
  protected val it = new ItWord
  
  /**
   * Class that, via an instance referenced from the <code>they</code> field,
   * supports test (and shared test) registration in <code>FunSpec</code>s.
   *
   * <p>
   * This class supports syntax such as the following:
   * </p>
   *
   * <pre class="stHighlight">
   * they("should be empty")
   * ^
   * </pre>
   *
   * <pre class="stHighlight">
   * they should behave like nonFullStack(stackWithOneItem)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples, see the <a href="../FunSpec.html">main documentation for <code>FunSpec</code></a>.
   * </p>
   */
  protected final class TheyWord {

    /**
     * Register a test with the given spec text, optional tags, and test function value that takes no arguments.
     * An invocation of this method is called an &#8220;example.&#8221;
     *
     * This method will register the test for later execution via an invocation of one of the <code>execute</code>
     * methods. The name of the test will be a concatenation of the text of all surrounding describers,
     * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
     * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
     * this <code>FunSpec</code> instance.
     *
     * @param specText the specification text, which will be combined with the descText of any surrounding describers
     * to form the test name
     * @param testTags the optional list of tags for this test
     * @param testFun the test function
     * @throws DuplicateTestNameException if a test with the same name has been registered previously
     * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
     * @throws NullPointerException if <code>specText</code> or any passed test tag is <code>null</code>
     */
    def apply(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) {
      registerTest(specText, testFun, "theyCannotAppearInsideAnotherThey", sourceFileName, "apply", 3, -2, None, None, None, testTags: _*)
    }

    /**
     * Supports the registration of shared tests.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they should behave like nonFullStack(stackWithOneItem)
     *      ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="../Spec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FunSpec</code>.
     * </p>
     */
    def should(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must behave like nonFullStack(stackWithOneItem)
     *      ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="../Spec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FunSpec</code>.
     * </p>
     */
    def must(behaveWord: BehaveWord) = behaveWord
  }

  /**
   * Supports test (and shared test) registration in <code>FunSpec</code>s.
   *
   * <p>
   * This field supports syntax such as the following:
   * </p>
   *
   * <pre class="stHighlight">
   * they("should be empty")
   * ^
   * </pre>
   *
   * <pre class="stHighlight">
   * they should behave like nonFullStack(stackWithOneItem)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see
   * the <a href="../FunSpec.html">main documentation for <code>FunSpec</code></a>.
   * </p>
   */
  protected val they = new TheyWord

  /**
   * Register a test to ignore, which has the given spec text, optional tags, and test function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>execute</code>
   * methods. This method exists to make it easy to ignore an existing test by changing the call to <code>it</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be executed, but a
   * report will be sent that indicates the test was ignored. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>FunSpec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testTags the optional list of tags for this test
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullPointerException if <code>specText</code> or any passed test tag is <code>null</code>
   */
  protected def ignore(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    registerIgnoredTest(specText, testFun, "ignoreCannotAppearInsideAnIt", sourceFileName, "ignore", 6, -2, None, testTags: _*)
  }

  /**
   * Register a test to ignore, which has the given spec text and test function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>execute</code>
   * methods. This method exists to make it easy to ignore an existing test by changing the call to <code>it</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be executed, but a
   * report will be sent that indicates the test was ignored. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>FunSpec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullPointerException if <code>specText</code> or any passed test tag is <code>null</code>
   */
  protected def ignore(specText: String)(testFun: FixtureParam => Any) {
    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("ignoreCannotAppearInsideAnIt"), getStackDepthFun(sourceFileName, "ignore"))
    ignore(specText, Array[Tag](): _*)(testFun)
  }

  /**
   * Describe a &#8220;subject&#8221; being specified and tested by the passed function value. The
   * passed function value may contain more describers (defined with <code>describe</code>) and/or tests
   * (defined with <code>it</code>). This trait's implementation of this method will register the
   * description string and immediately invoke the passed function.
   */
  protected def describe(description: String)(fun: => Unit) {
    registerNestedBranch(description, None, fun, "describeCannotAppearInsideAnIt", sourceFileName, "describe", 4, -2, None)
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> tag names to which tests in this <code>FunSpec</code> belong, and values
   * the <code>Set</code> of test names that belong to each tag. If this <code>FunSpec</code> contains no tags, this method returns an empty <code>Map</code>.
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

  /**
   * Run a test. This trait's implementation runs the test registered with the name specified by
   * <code>testName</code>. Each test's name is a concatenation of the text of all describers surrounding a test,
   * from outside in, and the test's  spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.)
   *
   * @param testName the name of one test to execute.
   * @param args the <code>Args</code> for this run
   *
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, or <code>configMap</code>
   *     is <code>null</code>.
   */
  protected override def runTest(testName: String, args: Args) {

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
   * <p>
   * Run zero to many of this <code>FunSpec</code>'s tests.
   * </p>
   *
   * <p>
   * This method takes a <code>testName</code> parameter that optionally specifies a test to invoke.
   * If <code>testName</code> is <code>Some</code>, this trait's implementation of this method
   * invokes <code>runTest</code> on this object, passing in:
   * </p>
   *
   * <ul>
   * <li><code>testName</code> - the <code>String</code> value of the <code>testName</code> <code>Option</code> passed
   *   to this method</li>
   * <li><code>reporter</code> - the <code>Reporter</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>stopper</code> - the <code>Stopper</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>configMap</code> - the <code>configMap</code> passed to this method, or one that wraps and delegates to it</li>
   * </ul>
   *
   * <p>
   * This method takes a <code>Set</code> of tag names that should be included (<code>tagsToInclude</code>), and a <code>Set</code>
   * that should be excluded (<code>tagsToExclude</code>), when deciding which of this <code>Suite</code>'s tests to execute.
   * If <code>tagsToInclude</code> is empty, all tests will be executed
   * except those those belonging to tags listed in the <code>tagsToExclude</code> <code>Set</code>. If <code>tagsToInclude</code> is non-empty, only tests
   * belonging to tags mentioned in <code>tagsToInclude</code>, and not mentioned in <code>tagsToExclude</code>
   * will be executed. However, if <code>testName</code> is <code>Some</code>, <code>tagsToInclude</code> and <code>tagsToExclude</code> are essentially ignored.
   * Only if <code>testName</code> is <code>None</code> will <code>tagsToInclude</code> and <code>tagsToExclude</code> be consulted to
   * determine which of the tests named in the <code>testNames</code> <code>Set</code> should be run. For more information on trait tags, see the main documentation for this trait.
   * </p>
   *
   * <p>
   * If <code>testName</code> is <code>None</code>, this trait's implementation of this method
   * invokes <code>testNames</code> on this <code>Suite</code> to get a <code>Set</code> of names of tests to potentially execute.
   * (A <code>testNames</code> value of <code>None</code> essentially acts as a wildcard that means all tests in
   * this <code>Suite</code> that are selected by <code>tagsToInclude</code> and <code>tagsToExclude</code> should be executed.)
   * For each test in the <code>testName</code> <code>Set</code>, in the order
   * they appear in the iterator obtained by invoking the <code>elements</code> method on the <code>Set</code>, this trait's implementation
   * of this method checks whether the test should be run based on the <code>tagsToInclude</code> and <code>tagsToExclude</code> <code>Set</code>s.
   * If so, this implementation invokes <code>runTest</code>, passing in:
   * </p>
   *
   * <ul>
   * <li><code>testName</code> - the <code>String</code> name of the test to run (which will be one of the names in the <code>testNames</code> <code>Set</code>)</li>
   * <li><code>reporter</code> - the <code>Reporter</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>stopper</code> - the <code>Stopper</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>configMap</code> - the <code>configMap</code> passed to this method, or one that wraps and delegates to it</li>
   * </ul>
   *
   * @param testName an optional name of one test to execute. If <code>None</code>, all relevant tests should be executed.
   *                 I.e., <code>None</code> acts like a wildcard that means execute all relevant tests in this <code>FunSpec</code>.
   * @param args the <code>Args</code> to which results will be reported
   *
   * @throws NullPointerException if any of <code>testName</code> or <code>args</code> is <code>null</code>.
   */
  protected override def runTests(testName: Option[String], args: Args) {

    runTestsImpl(thisSuite, testName, args, info, true, runTest)
  }

  /**
   * An immutable <code>Set</code> of test names. If this <code>FunSpec</code> contains no tests, this method returns an
   * empty <code>Set</code>.
   *
   * <p>
   * This trait's implementation of this method will return a set that contains the names of all registered tests. The set's
   * iterator will return those names in the order in which the tests were registered. Each test's name is composed
   * of the concatenation of the text of each surrounding describer, in order from outside in, and the text of the
   * example itself, with all components separated by a space.
   * </p>
   */
  override def testNames: Set[String] = {
    // I'm returning a ListSet here so that they tests will be run in registration order
    ListSet(atomic.get.testNamesList.toArray: _*)
  }

  override def run(testName: Option[String], args: Args) {
    runImpl(thisSuite, testName, args, super.run)
  }

  /**
   * Supports shared test registration in <code>FunSpec</code>s.
   *
   * <p>
   * This field supports syntax such as the following:
   * </p>
   *
   * <pre class="stHighlight">
   * it should behave like nonFullStack(stackWithOneItem)
   *           ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of <cod>behave</code>, see the <a href="../FunSpec.html#SharedTests">Shared tests section</a>
   * in the main documentation for trait <code>FunSpec</code>.
   * </p>
   */
  protected val behave = new BehaveWord

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
  protected implicit def convertPendingToFixtureFunction(f: => PendingNothing): FixtureParam => Any = {
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
  final override val styleName: String = "org.scalatest.fixture.FunSpec"
}

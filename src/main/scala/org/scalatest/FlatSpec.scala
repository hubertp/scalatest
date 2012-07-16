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
package org.scalatest

import verb.{ResultOfTaggedAsInvocation, ResultOfStringPassedToVerb, BehaveWord, ShouldVerb, MustVerb, CanVerb}
import NodeFamily._
import scala.collection.immutable.ListSet
import org.scalatest.exceptions.StackDepthExceptionHelper.getStackDepth
import java.util.concurrent.atomic.AtomicReference
import java.util.ConcurrentModificationException
import org.scalatest.events._
import Suite.anErrorThatShouldCauseAnAbort

/**
 * Trait that facilitates a &#8220;behavior-driven&#8221; style of development (BDD), in which tests
 * are combined with text that specifies the behavior the tests verify.
 * (In BDD, the word <em>example</em> is usually used instead of <em>test</em>. The word test will not appear
 * in your code if you use <code>FlatSpec</code>, so if you prefer the word <em>example</em> you can use it. However, in this documentation
 * the word <em>test</em> will be used, for clarity and to be consistent with the rest of ScalaTest.)
 * Trait <code>FlatSpec</code> is so named because
 * your specification text and tests line up flat against the left-side indentation level, with no nesting needed.
 * </p>
 *
 * <p>
 * <code>FlatSpec</code>'s no-nesting approach contrasts with traits <code>FunSpec</code> and <code>WordSpec</code>, which use nesting
 * to reduce duplication of specification text. Although nesting does have the advantage of reducing text duplication,
 * figuring out the full specification text for one test can require back-tracking out of several levels of nesting, mentally prepending
 * each fragment of text encountered. Thus the tradeoff with the nesting approach of <code>FunSpec</code> and <code>WordSpec</code> is that
 * they have less duplicated text at the cost of being a bit challenging to read. Trait <code>FlatSpec</code> offers the opposite
 * tradeoff. In a <code>FlatSpec</code> text is duplicated more, but figuring out the full specification text for a particular test is
 * easier. Here's an example <code>FlatSpec</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 * import scala.collection.mutable.Stack
 *
 * class StackSpec extends FlatSpec {
 *
 *   behavior of "A Stack"
 *
 *   it should "pop values in last-in-first-out order" in {
 *     val stack = new Stack[Int]
 *     stack.push(1)
 *     stack.push(2)
 *     assert(stack.pop() === 2)
 *     assert(stack.pop() === 1)
 *   }
 *
 *   it should "throw NoSuchElementException if an empty stack is popped" in {
 *     val emptyStack = new Stack[String]
 *     intercept[NoSuchElementException] {
 *       emptyStack.pop()
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * Note: you can you <code>must</code> or <code>can</code> as well as <code>should</code> in a <code>FlatSpec</code>. For example, instead of
 * <code>it should "pop</code>..., you could write <code>it must "pop</code>... or <code>it can "pop</code>....
 * </p>
 *
 * <p>
 * Instead of using a <code>behavior of</code> clause, you can alternatively use a shorthand syntax in which you replace
 * the first <code>it</code> with the subject string, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 * import scala.collection.mutable.Stack
 *
 * class StackSpec extends FlatSpec {
 *
 *   "A Stack" should "pop values in last-in-first-out order" in {
 *     val stack = new Stack[Int]
 *     stack.push(1)
 *     stack.push(2)
 *     assert(stack.pop() === 2)
 *     assert(stack.pop() === 1)
 *   }
 *
 *   it should "throw NoSuchElementException if an empty stack is popped" in {
 *     val emptyStack = new Stack[String]
 *     intercept[NoSuchElementException] {
 *       emptyStack.pop()
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * Running either of the two previous three versions of <code>StackSpec</code> in the Scala interpreter would yield:
 * </p>
 * 
 * <pre class="stREPL">
 * <span class="stGreen">A Stack
 * - should pop values in last-in-first-out order
 * - should throw NoSuchElementException if an empty stack is popped</span>
 * </pre>
 *
 * <p>
 * In a <code>FlatSpec</code> you write a one (or more) sentence specification for each bit of behavior you wish to
 * specify and test. Each specification sentence has a
 * "subject," which is sometimes called the <em>system under test</em> (or SUT). The 
 * subject is the entity being specified and tested and also serves as the subject of the sentences you write for each test.
 * Often you will want to write multiple tests for the same subject. In a <code>FlatSpec</code>, you name the subject once,
 * with a <code>behavior of</code> clause or its shorthand, then write tests for that subject with <code>it should</code>/<code>must</code><code>can "do something"</code> phrases.
 * Each <code>it</code> refers to the most recently declared subject. For example, the four tests shown in this snippet are all testing
 * a stack that contains one item:
 * </p>
 * 
 * <pre class="stHighlight">
 * behavior of "A Stack (with one item)"
 *
 * it should "be non-empty" in {}
 *
 * it should "return the top item on peek" in {}
 *
 * it should "not remove the top item on peek" in {}
 *
 * it should "remove the top item on pop" in {}
 * </pre>
 * 
 * <p>
 * The same is true if the tests are written using the shorthand notation:
 * </p>
 *
 * <pre class="stHighlight">
 * "A Stack (with one item)" should "be non-empty" in {}
 *
 * it should "return the top item on peek" in {}
 *
 * it should "not remove the top item on peek" in {}
 *
 * it should "remove the top item on pop" in {}
 * </pre>
 * 
 * <p>
 * In a <code>FlatSpec</code>, therefore, to figure out what "<code>it</code>" means, you just scan vertically until you find the most
 * recent use of <code>behavior of</code> or the shorthand notation.
 * </p>
 *
 * <p>
 * A <code>FlatSpec</code>'s lifecycle has two phases: the <em>registration</em> phase and the
 * <em>ready</em> phase. It starts in registration phase and enters ready phase the first time
 * <code>run</code> is called on it. It then remains in ready phase for the remainder of its lifetime.
 * </p>
 *
 * <p>
 * Tests can only be registered while the <code>FlatSpec</code> is
 * in its registration phase. Any attempt to register a test after the <code>FlatSpec</code> has
 * entered its ready phase, <em>i.e.</em>, after <code>run</code> has been invoked on the <code>FlatSpec</code>,
 * will be met with a thrown <code>TestRegistrationClosedException</code>. The recommended style
 * of using <code>FlatSpec</code> is to register tests during object construction as is done in all
 * the examples shown here. If you keep to the recommended style, you should never see a
 * <code>TestRegistrationClosedException</code>.
 * </p>
 *
 * <p>
 * See also: <a href="http://www.scalatest.org/getting_started_with_flat_spec" target="_blank">Getting started with <code>FlatSpec</code>.</a>
 * </p>
 *
 * <a name="IgnoredTests"></a><h2>Ignored tests</h2>
 *
 * To support the common use case of &#8220;temporarily&#8221; disabling a test, with the
 * good intention of resurrecting the test at a later time, <code>FlatSpec</code> provides a method
 * <code>ignore</code> that can be used instead of <code>it</code> to register a test. For example, to temporarily
 * disable the test with the name <code>"A Stack should throw NoSuchElementException if an empty stack is popped"</code>, just
 * change &#8220;<code>it</code>&#8221; into &#8220;<code>ignore</code>,&#8221; like this:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 * import scala.collection.mutable.Stack
 *
 * class StackSpec extends FlatSpec {
 *
 *   "A Stack" should "pop values in last-in-first-out order" in {
 *       val stack = new Stack[Int]
 *       stack.push(1)
 *       stack.push(2)
 *       assert(stack.pop() === 2)
 *       assert(stack.pop() === 1)
 *     }
 *
 *   ignore should "throw NoSuchElementException if an empty stack is popped" in {
 *     val emptyStack = new Stack[String]
 *     intercept[NoSuchElementException] {
 *       emptyStack.pop()
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you run this version of <code>StackSpec</code> with:
 * </p>
 *
 * <pre class="stREPL">
 * scala> (new StackSpec).execute()
 * </pre>
 *
 * <p>
 * It will run only the first test and report that the second test was ignored:
 * </p>
 *
 * <pre class="stREPL">
 * <span class="stGreen">A Stack
 * - should pop values in last-in-first-out order</span>
 * <span class="stYellow">- should throw NoSuchElementException if an empty stack is popped !!! IGNORED !!!</span>
 * </pre>
 *
 * <p>
 * When using shorthand notation, you won't have an <code>it</code> to change into <code>ignore</code> for
 * the first test of each new subject. To ignore such tests, you must instead change <code>in</code> to <code>ignore</code>.
 * For example, to temporarily disable the test with the name <code>"A Stack should pop values in last-in-first-out order"</code>,
 * change &#8220;<code>in</code>&#8221; into &#8220;<code>ignore</code>&#8221; like this:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 * import scala.collection.mutable.Stack
 *
 * class StackSpec extends FlatSpec {
 *
 *   "A Stack" should "pop values in last-in-first-out order" ignore {
 *       val stack = new Stack[Int]
 *       stack.push(1)
 *       stack.push(2)
 *       assert(stack.pop() === 2)
 *       assert(stack.pop() === 1)
 *     }
 *
 *   it should "throw NoSuchElementException if an empty stack is popped" in {
 *     val emptyStack = new Stack[String]
 *     intercept[NoSuchElementException] {
 *       emptyStack.pop()
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you run this version of <code>StackSpec</code> with:
 * </p>
 *
 * <pre class="stREPL">
 * scala> (new StackSpec).execute()
 * </pre>
 *
 * <p>
 * It will run only the second test and report that the first test was ignored:
 * </p>
 *
 * <pre class="stREPL">
 * <span class="stGreen">A Stack</span>
 * <span class="stYellow">- should pop values in last-in-first-out order !!! IGNORED !!!</span>
 * <span class="stGreen">- should throw NoSuchElementException if an empty stack is popped</span>
 * </pre>
 *
 * <h2>Informers</h2>
 *
 * <p>
 * One of the parameters to the <code>run</code> method is a <code>Reporter</code>, which
 * will collect and report information about the running suite of tests.
 * Information about suites and tests that were run, whether tests succeeded or failed, 
 * and tests that were ignored will be passed to the <code>Reporter</code> as the suite runs.
 * Most often the reporting done by default by <code>FlatSpec</code>'s methods will be sufficient, but
 * occasionally you may wish to provide custom information to the <code>Reporter</code> from a test.
 * For this purpose, an <code>Informer</code> that will forward information to the current <code>Reporter</code>
 * is provided via the <code>info</code> parameterless method.
 * You can pass the extra information to the <code>Informer</code> via its <code>apply</code> method.
 * The <code>Informer</code> will then pass the information to the <code>Reporter</code> via an <code>InfoProvided</code> event.
 * Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 *
 * class ArithmeticSpec extends FlatSpec {
 *
 *  "The Scala language" must "add correctly" in {
 *     val sum = 2 + 3
 *     assert(sum === 5)
 *     info("addition seems to work")
 *   }
 *
 *   it must "subtract correctly" in {
 *     val diff = 7 - 2
 *     assert(diff === 5)
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you run this <code>FlatSpec</code> from the interpreter, you will see the following message
 * included in the printed report:
 * </p>
 *
 * <pre class="stREPL">
 * scala> (new ArithmeticSpec).execute()
 * <span class="stGreen">The Scala language 
 * - must add correctly
 *   + addition seems to work 
 * - must subtract correctly</span>
 * </pre>
 *
 * <p>
 * One use case for the <code>Informer</code> is to pass more information about a specification to the reporter. For example,
 * the <code>GivenWhenThen</code> trait provides methods that use the implicit <code>info</code> provided by <code>FlatSpec</code>
 * to pass such information to the reporter. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 * import org.scalatest.GivenWhenThen
 * 
 * class ArithmeticSpec extends FlatSpec with GivenWhenThen {
 * 
 *  "The Scala language" must "add correctly" in { 
 * 
 *     given("two integers")
 *     val x = 2
 *     val y = 3
 * 
 *     when("they are added")
 *     val sum = x + y
 * 
 *     then("the result is the sum of the two numbers")
 *     assert(sum === 5)
 *   }
 * 
 *   it must "subtract correctly" in {
 * 
 *     given("two integers")
 *     val x = 7
 *     val y = 2
 * 
 *     when("one is subtracted from the other")
 *     val diff = x - y
 * 
 *     then("the result is the difference of the two numbers")
 *     assert(diff === 5)
 *   }
 * }
 * </pre>
 *
 * <pre class="stREPL">
 * scala> (new ArithmeticSpec).execute()
 * <span class="stGreen">The Scala language 
 * - must add correctly
 *   + Given two integers 
 *   + When they are added 
 *   + Then the result is the sum of the two numbers 
 * - must subtract correctly
 *   + Given two integers 
 *   + When one is subtracted from the other 
 *   + Then the result is the difference of the two numbers</span> 
 * </pre>
 *
 * <a name="PendingTests"><h2>Pending tests</h2></a>
 *
 * <p>
 * A <em>pending test</em> is one that has been given a name but is not yet implemented. The purpose of
 * pending tests is to facilitate a style of testing in which documentation of behavior is sketched
 * out before tests are written to verify that behavior (and often, before the behavior of
 * the system being tested is itself implemented). Such sketches form a kind of specification of
 * what tests and functionality to implement later.
 * </p>
 *
 * <p>
 * To support this style of testing, a test can be given a name that specifies one
 * bit of behavior required by the system being tested. The test can also include some code that
 * sends more information about the behavior to the reporter when the tests run. At the end of the test,
 * it can call method <code>pending</code>, which will cause it to complete abruptly with <code>TestPendingException</code>.
 * </p>
 *
 * <p>
 * Because tests in ScalaTest can be designated as pending with <code>TestPendingException</code>, both the test name and any information
 * sent to the reporter when running the test can appear in the report of a test run. (In other words,
 * the code of a pending test is executed just like any other test.) However, because the test completes abruptly
 * with <code>TestPendingException</code>, the test will be reported as pending, to indicate
 * the actual test, and possibly the functionality it is intended to test, has not yet been implemented.
 * You can mark tests as pending in <code>FlatSpec</code> like this:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 *
 * class ArithmeticSpec extends FlatSpec {
 *
 *   // Sharing fixture objects via instance variables
 *   val shared = 5
 *
 *  "The Scala language" must "add correctly" in {
 *     val sum = 2 + 3
 *     assert(sum === shared)
 *   }
 *
 *   it must "subtract correctly" is (pending)
 * }
 * </pre>
 *
 * <p>
 * If you run this version of <code>ArithmeticSpec</code> with:
 * </p>
 *
 * <pre class="stREPL">
 * scala> (new ArithmeticSpec).execute()
 * </pre>
 *
 * <p>
 * It will run both tests but report that <code>The Scala language must subtract correctly</code> is pending. You'll see:
 * </p>
 *
 * <pre class="stREPL">
 * <span class="stGreen">The Scala language
 * - must add correctly</span>
 * <span class="stYellow">- must subtract correctly (pending)</span>
 * </pre>
 * 
 * <p>
 * One difference between an ignored test and a pending one is that an ignored test is intended to be used during a
 * significant refactorings of the code under test, when tests break and you don't want to spend the time to fix
 * all of them immediately. You can mark some of those broken tests as ignored temporarily, so that you can focus the red
 * bar on just failing tests you actually want to fix immediately. Later you can go back and fix the ignored tests.
 * In other words, by ignoring some failing tests temporarily, you can more easily notice failed tests that you actually
 * want to fix. By contrast, a pending test is intended to be used before a test and/or the code under test is written.
 * Pending indicates you've decided to write a test for a bit of behavior, but either you haven't written the test yet, or
 * have only written part of it, or perhaps you've written the test but don't want to implement the behavior it tests
 * until after you've implemented a different bit of behavior you realized you need first. Thus ignored tests are designed
 * to facilitate refactoring of existing code whereas pending tests are designed to facilitate the creation of new code.
 * </p>
 *
 * <p>
 * One other difference between ignored and pending tests is that ignored tests are implemented as a test tag that is
 * excluded by default. Thus an ignored test is never executed. By contrast, a pending test is implemented as a
 * test that throws <code>TestPendingException</code> (which is what calling the <code>pending</code> method does). Thus
 * the body of pending tests are executed up until they throw <code>TestPendingException</code>. The reason for this difference
 * is that it enables your unfinished test to send <code>InfoProvided</code> messages to the reporter before it completes
 * abruptly with <code>TestPendingException</code>, as shown in the previous example on <code>Informer</code>s
 * that used the <code>GivenWhenThen</code> trait. For example, the following snippet in a <code>FlatSpec</code>:
 * </p>
 *
 * <pre class="stHighlight">
 *  "The Scala language" must "add correctly" in { 
 *     given("two integers")
 *     when("they are added")
 *     then("the result is the sum of the two numbers")
 *     pending
 *   }
 *   // ...
 * </pre>
 *
 * <p>
 * Would yield the following output when run in the interpreter:
 * </p>
 *
 * <pre class="stREPL">
 * <span class="stGreen">The Scala language</span>
 * <span class="stYellow">- must add correctly (pending)
 *   + Given two integers 
 *   + When they are added 
 *   + Then the result is the sum of the two numbers</span> 
 * </pre>
 *
 * <a name="TaggingTests"></a><h2>Tagging tests</h2>
 *
 * A <code>FlatSpec</code>'s tests may be classified into groups by <em>tagging</em> them with string names.
 * As with any suite, when executing a <code>FlatSpec</code>, groups of tests can
 * optionally be included and/or excluded. To tag a <code>FlatSpec</code>'s tests,
 * you pass objects that extend abstract class <code>org.scalatest.Tag</code> to <code>taggedAs</code> method
 * invoked on the string that describes the test you want to tag. Class <code>Tag</code> takes one parameter,
 * a string name.  If you have
 * created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>FlatSpec</code>s that match. To do so, simply 
 * pass the fully qualified names of the Java interfaces to the <code>Tag</code> constructor. For example, if you've
 * defined Java annotation interfaces with fully qualified names, <code>com.mycompany.tags.SlowTest</code> and <code>com.mycompany.tags.DbTest</code>, then you could
 * create matching groups for <code>FlatSpec</code>s like this:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.Tag
 *
 * object SlowTest extends Tag("com.mycompany.tags.SlowTest")
 * object DbTest extends Tag("com.mycompany.tags.DbTest")
 * </pre>
 *
 * <p>
 * Given these definitions, you could place <code>FlatSpec</code> tests into groups like this:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 *
 * class ExampleSpec extends FlatSpec {
 *
 *   "The Scala language" must "add correctly" taggedAs(SlowTest) in {
 *       val sum = 1 + 1
 *       assert(sum === 2)
 *     }
 *
 *   it must "subtract correctly" taggedAs(SlowTest, DbTest) in {
 *     val diff = 4 - 1
 *     assert(diff === 3)
 *   }
 * }
 * </pre>
 *
 * <p>
 * This code marks both tests with the <code>com.mycompany.tags.SlowTest</code> tag, 
 * and test <code>"The Scala language should subtract correctly"</code> with the <code>com.mycompany.tags.DbTest</code> tag.
 * </p>
 *
 * <p>
 * The <code>run</code> method takes a <code>Filter</code>, whose constructor takes an optional
 * <code>Set[String]</code> called <code>tagsToInclude</code> and a <code>Set[String]</code> called
 * <code>tagsToExclude</code>. If <code>tagsToInclude</code> is <code>None</code>, all tests will be run
 * except those those belonging to tags listed in the
 * <code>tagsToExclude</code> <code>Set</code>. If <code>tagsToInclude</code> is defined, only tests
 * belonging to tags mentioned in the <code>tagsToInclude</code> set, and not mentioned in <code>tagsToExclude</code>,
 * will be run.
 * </p>
 *
 * <a name="sharedFixtures"></a><h2>Shared fixtures</h2>
 *
 * <p>
 * A test <em>fixture</em> is composed of the objects and other artifacts (files, sockets, database
 * connections, <em>etc.</em>) tests use to do their work.
 * When multiple tests need to work with the same fixtures, it is important to try and avoid
 * duplicating the fixture code across those tests. The more code duplication you have in your
 * tests, the greater drag the tests will have on refactoring the actual production code.
 * ScalaTest recommends several techniques to eliminate such code duplication, and provides several
 * traits to help. Each technique is geared towards helping you reduce code duplication without introducing
 * instance <code>var</code>s, shared mutable objects, or other dependencies between tests. Eliminating shared
 * mutable state across tests will make your test code easier to reason about and more amenable for parallel
 * test execution.
 * </p>
 *
 * <p>
 * The following sections
 * describe these techniques, including explaining the recommended usage
 * for each. But first, here's a table summarizing the options:
 * </p>
 *
 * <table style="border-collapse: collapse; border: 1px solid black">
 * <tr><th style="background-color: #CCCCCC; border-width: 1px; padding: 3px; text-align: center; border: 1px solid black">Technique</th><th style="background-color: #CCCCCC; border-width: 1px; padding: 3px; text-align: center; border: 1px solid black">Recommended uses</th></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right"><a href="#getFixtureMethods">get-fixture methods</a></td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">Use when you need the same mutable fixture objects in multiple tests, and don't need to clean up after.</td></td></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right"><a href="#fixtureContextObjects">fixture-context objects</a></td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">Use when you need different combinations of mutable fixture objects in different tests, and don't need to clean up after. </td></td></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right"><a href="#oneInstancePerTest"><code>OneInstancePerTest</code></a></td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">Use when porting JUnit tests to ScalaTest, or if you prefer JUnit's approach to test isolation: running each test in its own instance of the test class.</td></td></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right"><a href="#withFixtureNoArgTest"><code>withFixture(NoArgTest)</code></a></td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">Use when you need to perform side effects at the beginning and end of all or most tests, or want to stack traits that perform such side-effects.</td></td></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right"><a href="#loanFixtureMethods">loan-fixture methods</a></td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">Use when different tests need different fixtures that must be cleaned up afterwords.</td></td></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right"><a href="#withFixtureOneArgTest"><code>withFixture(OneArgTest)</code></a></td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">Use when all or most tests need the same fixtures that must be cleaned up afterwords.</td></td></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right"><a href="#beforeAndAfter"><code>BeforeAndAfter</code></a></td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">Use when you need to perform the same side-effects before and/or after tests, rather than at the beginning or end of tests.</td></td></tr>
 * <tr><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right"><a href="#composingFixtures"><code>BeforeAndAfterEach</code></a></td><td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">Use when you want to stack traits that perform the same side-effects before and/or after tests, rather than at the beginning or end of tests.</td></td></tr>
 * </table>
 *
 * <a name="getFixtureMethods"></a>
 * <h4>Calling get-fixture methods</h4>
 *
 * <p>
 * If you need to create the same mutable fixture objects in multiple tests, and don't need to clean them up after using them, the simplest approach is to write one or
 * more <em>get-fixture</em> methods. A get-fixture method returns a new instance of a needed fixture object (or an holder object containing
 * multiple fixture objects) each time it is called. You can call a get-fixture method at the beginning of each
 * test that needs the fixture, storing the returned object or objects in local variables. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.suite.getfixture
 *
 * import org.scalatest.Suite
 * import collection.mutable.ListBuffer
 *
 * class ExampleSuite extends Suite {
 * 
 *   def fixture =
 *     new {
 *       val builder = new StringBuilder("ScalaTest is ")
 *       val buffer = new ListBuffer[String]
 *     }
 * 
 *   def &#96;test: testing should be easy&#96; {
 *     val f = fixture
 *     f.builder.append("easy!")
 *     assert(f.builder.toString === "ScalaTest is easy!")
 *     assert(f.buffer.isEmpty)
 *     f.buffer += "sweet"
 *   }
 * 
 *   def &#96;test: testing should be fun&#96; {
 *     val f = fixture
 *     f.builder.append("fun!")
 *     assert(f.builder.toString === "ScalaTest is fun!")
 *     assert(f.buffer.isEmpty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * The &ldquo;<code>f.</code>&rdquo; in front of each use of a fixture object provides a visual indication of which objects 
 * are part of the fixture, but if you prefer, you can import the the members with &ldquo;<code>import f._</code>&rdquo; and use the names directly.
 * </p>
 *
 * <p>
 * If you need to configure fixture objects differently in different tests, you can pass configuration into the get-fixture method. For example, if you could pass
 * in an initial value for a mutable fixture object as a parameter to the get-fixture method.
 * </p>
 *
 * <a name="fixtureContextObjects"></a>
 * <h4>Instantiating fixture-context objects </h4>
 *
 * <p>
 * A alternate technique that is especially useful when different tests need different combinations of fixture objects is to define the fixture objects as instance variables
 * of <em>fixture-context objects</em> whose instantiation forms the body of tests. Like get-fixture methods, fixture-context objects are anly
 * appropriate if you don't need to clean up the fixtures after using them.
 * </p>
 *
 * To use this technique, you define instance variables intialized with fixture objects in traits and/or classes, then in each test instantiate an object that
 * contains just the fixture objects needed by the test. Keep in mind that traits allow you to mix together just the fixture objects needed by each test, whereas classes
 * allow you to pass data in via a constructor to configure the fixture objects. Here's an example in which fixture objects are partitioned into two traits
 * and each test just gets mixes together the traits it needs:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.suite.fixturecontext
 *
 * import collection.mutable.ListBuffer
 * import org.scalatest.Suite
 * 
 * class ExampleSuite extends Suite {
 * 
 *   trait Builder {
 *     val builder = new StringBuilder("ScalaTest is ")
 *   }
 * 
 *   trait Buffer {
 *     val buffer = ListBuffer("ScalaTest", "is")
 *   }
 * 
 *   // This test needs the StringBuilder fixture
 *   def &#96;test: testing should be productive&#96; {
 *     new Builder {
 *       builder.append("productive!")
 *       assert(builder.toString === "ScalaTest is productive!")
 *     }
 *   }
 * 
 *   // This test needs the ListBuffer[String] fixture
 *   def &#96;test: test code should be readable&#96; {
 *     new Buffer {
 *       buffer += ("readable!")
 *       assert(buffer === List("ScalaTest", "is", "readable!"))
 *     }
 *   }
 * 
 *   // This test needs both the StringBuilder and ListBuffer
 *   def &#96;test: test code should be clear and concise&#96; {
 *     new Builder with Buffer {
 *       builder.append("clear!")
 *       buffer += ("concise!")
 *       assert(builder.toString === "ScalaTest is clear!")
 *       assert(buffer === List("ScalaTest", "is", "concise!"))
 *     }
 *   }
 * }
 * </pre>
 *
 * <a name="oneInstancePerTest"></a>
 * <h4>Mixing in <code>OneInstancePerTest</code></h4>
 *
 * <p>
 * If every test method requires the same set of
 * mutable fixture objects, and none require cleanup, one other approach you can take is make them simply <code>val</code>s and mix in trait
 * <a href="OneInstancePerTest.html"><code>OneInstancePerTest</code></a>.  If you mix in <code>OneInstancePerTest</code>, each test
 * will be run in its own instance of the <code>Suite</code>, similar to the way JUnit tests are executed. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.suite.oneinstancepertest
 *
 * import org.scalatest._
 * import collection.mutable.ListBuffer
 * 
 * class ExampleSuite extends Suite with OneInstancePerTest {
 * 
 *   val builder = new StringBuilder("ScalaTest is ")
 *   val buffer = new ListBuffer[String]
 * 
 *   def &#96;test: testing should be easy&#96; {
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 * 
 *   def &#96;test: testing should be fun&#96; {
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(buffer.isEmpty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * One way to think of <a href="OneInstancePerTest.html"><code>OneInstancePerTest</code></a> is that the entire <code>Suite</code> instance is like a fixture-context object,
 * but with the difference that the test code doesn't run during construction as it does with the real fixture-context object technique. Because this trait emulates JUnit's manner
 * of running tests, this trait can be helpful when porting JUnit tests to ScalaTest. The primary intended use of <code>OneInstancePerTest</code> is to serve as a supertrait for
 * <a href="ParallelTestExecution.html"><code>ParallelTestExecution</code></a> and the <a href="path/package.html">path traits</a>, but you can also mix it in
 * directly to help you port JUnit tests to ScalaTest or if you prefer JUnit's approach to test isolation.
 * </p>
 *
 * <a name="withFixtureNoArgTest"></a>
 * <h4>Overriding <code>withFixture(NoArgTest)</code></h4>
 *
 * <p>
 * Although the get-fixture method, fixture-context object, and <code>OneInstancePerTest</code> approaches take care of setting up a fixture at the beginning of each
 * test, they don't address the problem of cleaning up a fixture at the end of the test. If you just need to perform a side-effect at the beginning or end of
 * a test, and don't need to actually pass any fixture objects into the test, you can override <code>withFixture(NoArgTest)</code>, one of ScalaTest's
 * lifecycle methods defined in trait <a href="AbstractSuite.html"><code>AbstractSuite</code></a>.
 * </p>
 *
 * <p>
 * Trait <code>Suite</code>'s implementation of <code>runTest</code> passes a no-arg test function to <code>withFixture(NoArgTest)</code>. It is <code>withFixture</code>'s
 * responsibility to invoke that test function. <code>Suite</code>'s implementation of <code>withFixture</code> simply
 * invokes the function, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * // Default implementation in trait Suite
 * protected def withFixture(test: NoArgTest) {
 *   test()
 * }
 * </pre>
 *
 * <p>
 * You can, therefore, override <code>withFixture</code> to perform setup before and/or cleanup after invoking the test function. If
 * you have cleanup to perform, you should invoke the test function inside a <code>try</code> block and perform the cleanup in
 * a <code>finally</code> clause, because the exception that causes a test to fail will propagate through <code>withFixture</code> back
 * to <code>runTest</code>. (In other words, if the test fails, the test function invoked by <code>withFixture</code> will throw an exception.)
 * </p>
 *
 * <p>
 * The <code>withFixture</code> method is designed to be stacked, and to enable this, you should always call the <code>super</code> implementation
 * of <code>withFixture</code>, and let it invoke the test function rather than invoking the test function directly. In other words, instead of writing
 * &ldquo;<code>test()</code>&rdquo;, you should write &ldquo;<code>super.withFixture(test)</code>&rdquo;, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * // Your implementation
 * override def withFixture(test: NoArgTest) {
 *   // Perform setup
 *   try {
 *     super.withFixture(test) // Invoke the test function
 *   }
 *   finally {
 *     // Perform cleanup
 *   }
 * }
 * </pre>
 *
 * <p>
 * Here's an example in which <code>withFixture(NoArgTest)</code> is used to take a snapshot of the working directory if a test fails, and 
 * and send that information to the reporter:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.suite.noargtest
 *
 * import java.io.File
 * import org.scalatest.FunSuite
 *
 * class ExampleSuite extends FunSuite {
 *
 *   final val tmpDir = "tmpDir"
 *
 *   override def withFixture(test: NoArgTest) {
 *     
 *     try {
 *       super.withFixture(test)
 *     }
 *     catch {
 *       case e: Exception =&gt;
 *         val currDir = new File(".")
 *         val fileNames = currDir.list()
 *         info("Dir snapshot: " + fileNames.mkString(", "))
 *         throw e
 *     }
 *   }
 *
 *   test("this test should succeed") {
 *     assert(1 + 1 === 2)
 *   }
 *
 *   test("this test should fail") {
 *     assert(1 + 1 === 3)
 *   }
 * }
 * </pre>
 *
 * <p>
 * Running this version of <code>ExampleSuite</code> in the interpreter in a directory with two files, <code>hello.txt</code> and <code>world.txt</code>
 * would give the following output:
 * </p>
 *
 * <pre class="stREPL">
 * scala&gt; new ExampleSuite execute
 * <span class="stGreen">ExampleSuite:
 * - this test should succeed
 * <span class="stRed">- this test should fail *** FAILED ***
 *   2 did not equal 3 (<console>:33)
 *   + Dir snapshot: hello.txt, world.txt </span>
 * </pre>
 *
 * <p>
 * Note that the <a href="Suite$NoArgTest.html"><code>NoArgTest</code></a> passed to <code>withFixture</code>, in addition to
 * an <code>apply</code> method that executes the test, also includes the test name and the <a href="#configMapSection">config
 * map</a> passed to <code>runTest</code>. Thus you can also use the test name and configuration objects in your <code>withFixture</code>
 * implementation.
 * </p>
 *
 * <a name="loanFixtureMethods"></a>
 * <h4>Calling loan-fixture methods</h4>
 *
 * <p>
 * If you need to both pass a fixture object into a test <em>and</em> and perform cleanup at the end of the test, you'll need to use the <em>loan pattern</em>.
 * If different tests need different fixtures that require cleanup, you can implement the loan pattern directly by writing <em>loan-fixture</em> methods.
 * A loan-fixture method takes a function whose body forms part or all of a test's code. It creates a fixture, passes it to the test code by invoking the
 * function, then cleans up the fixture after the function returns.
 * </p>
 *
 * <p>
 * The following example shows three tests that use two fixtures, a database and a file. Both require cleanup after, so each is provided via a
 * loan-fixture method. (In this example, the database is simulated with a <code>StringBuffer</code>.)
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.suite.loanfixture
 *
 * import java.util.concurrent.ConcurrentHashMap
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
 * import org.scalatest.Suite
 * import DbServer._
 * import java.util.UUID.randomUUID
 * import java.io._
 * 
 * class ExampleSuite extends Suite {
 * 
 *   def withDatabase(testCode: Db =&gt; Any) {
 *     val dbName = randomUUID.toString
 *     val db = createDb(dbName) // create the fixture
 *     try {
 *       db.append("ScalaTest is ") // perform setup
 *       testCode(db) // "loan" the fixture to the test
 *     }
 *     finally {
 *       removeDb(dbName) // clean up the fixture
 *     }
 *   }
 * 
 *   def withFile(testCode: (File, FileWriter) =&gt; Any) {
 *     val file = File.createTempFile("hello", "world") // create the fixture
 *     val writer = new FileWriter(file)
 *     try {
 *       writer.write("ScalaTest is ") // set up the fixture
 *       testCode(file, writer) // "loan" the fixture to the test
 *     }
 *     finally {
 *       writer.close() // clean up the fixture
 *     }
 *   }
 * 
 *   // This test needs the file fixture
 *   def &#96;test: testing should be productive&#96; {
 *     withFile { (file, writer) =&gt;
 *       writer.write("productive!")
 *       writer.flush()
 *       assert(file.length === 24)
 *     }
 *   }
 * 
 *   // This test needs the database fixture
 *   def &#96;test: test code should be readable&#96; {
 *     withDatabase { db =&gt;
 *       db.append("readable!")
 *       assert(db.toString === "ScalaTest is readable!")
 *     }
 *   }
 * 
 *   // This test needs both the file and the database
 *   def &#96;test: test code should be clear and concise&#96; {
 *     withDatabase { db =&gt;
 *       withFile { (file, writer) =&gt; // loan-fixture methods compose
 *         db.append("clear!")
 *         writer.write("concise!")
 *         writer.flush()
 *         assert(db.toString === "ScalaTest is clear!")
 *         assert(file.length === 21)
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * As demonstrated by the last test, loan-fixture methods compose. Not only do loan-fixture methods allow you to
 * give each test the fixture it needs, they allow you to give a test multiple fixtures and clean everything up afterwords.
 * </p>
 *
 * <p>
 * Also demonstrated in this example is the technique of giving each test its own "fixture sandbox" to play in. When your fixtures
 * involve external side-effects, like creating files or databases, it is a good idea to give each file or database a unique name as is
 * done in this example. This keeps tests completely isolated, allowing you to run them in parallel if desired.
 * </p>
 *
 * </pre>
 * <a name="withFixtureOneArgTest"></a>
 * <h4>Overriding <code>withFixture(OneArgTest)</code></h4>
 *
 * <p>
 * If all or most tests need the same fixture, you can avoid some of the boilerplate of the loan-fixture method approach by using a <code>fixture.Suite</code>
 * and overriding <code>withFixture(OneArgTest)</code>.
 * Each test in a <code>fixture.Suite</code> takes a fixture as a parameter, allowing you to pass the fixture into
 * the test. You must indicate the type of the fixture parameter by specifying <code>FixtureParam</code>, and implement a
 * <code>withFixture</code> method that takes a <code>OneArgTest</code>. This <code>withFixture</code> method is responsible for
 * invoking the one-arg test function, so you can perform fixture set up before, and clean up after, invoking and passing
 * the fixture into the test function.
 * </p>
 *
 * <p>
 * As with <code>withFixture(NoArgTest)</code>, to enable trait stacking it is a good idea to always delegate to the <code>super</code> implementation
 * of <code>withFixture(NoArgTest)</code> instead of invoking the test function directly. In this case, however, you'll need to convert the
 * <code>OneArgTest</code> to a <code>NoArgTest</code>. You can do that by supplying the fixture object to <code>toNoArgTest</code> method of
 * <code>OneArgTest</code>. In other words, instead of writing &ldquo;<code>test("fixture")</code>&rdquo;, you'd write
 * &ldquo;<code>super.withFixture(test.toNoArgTest("fixture"))</code>&rdquo;.  Here's a complete example:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.suite.oneargtest
 *
 * import org.scalatest.fixture
 * import java.io._
 * 
 * class ExampleSuite extends fixture.Suite {
 * 
 *   case class F(file: File, writer: FileWriter)
 *   type FixtureParam = F
 * 
 *   def withFixture(test: OneArgTest) {
 *     val file = File.createTempFile("hello", "world") // create the fixture
 *     val writer = new FileWriter(file)
 *     try {
 *       writer.write("ScalaTest is ") // set up the fixture
 *       super.withFixture(test.toNoArgTest(F(file, writer))) // "loan" the fixture to the test
 *     }
 *     finally {
 *       writer.close() // clean up the fixture
 *     }
 *   }
 * 
 *   def &#96;test: testing should be easy&#96; (f: F) {
 *     f.writer.write("easy!")
 *     f.writer.flush()
 *     assert(f.file.length === 12)
 *   }
 * 
 *   def &#96;test: testing should be fun&#96; (f: F) {
 *     f.writer.write("fun!")
 *     f.writer.flush()
 *     assert(f.file.length === 9)
 *   }
 * }
 * </pre>
 *
 * <p>
 * In this example, the tests actually required two fixture objects, a <code>File</code> and a <code>FileWriter</code>. In such situations you can
 * simply define the <code>FixtureParam</code> type to be a tuple containing the objects, or as is done in this example, a case class containing
 * the objects.  For more information on the <code>withFixture(OneArgTest)</code> technique, see the <a href="fixture/Suite.html">documentation for <code>fixture.Suite</code></a>.
 * </p>
 *
 * <a name="beforeAndAfter"></a>
 * <h4>Mixing in <code>BeforeAndAfter</code></h4>
 *
 * <p>
 * In all the shared fixture examples shown so far, the activities of creating, setting up, and cleaning up the fixture objects have been
 * performed <em>during</em> the test.  This means that if an exception occurs during any of these activities, it will be reported as a test failure.
 * Sometimes, however, you may want setup to happen <em>before</em> the test starts, and cleanup <em>after</em> the test has completed, so that if an
 * exception occurs during setup or cleanup, the entire suite aborts and no more tests are attempted. The simplest way to accomplish this in ScalaTest is
 * to mix in trait <a href="BeforeAndAfter.html"><code>BeforeAndAfter</code></a>.  With this trait you can denote a bit of code to run before each test
 * with <code>before</code> and/or after each test each test with <code>after</code>, like this:
 * </p>
 * 
 * <pre class="stHighlight">
 * package org.scalatest.examples.suite.beforeandafter
 *
 * import org.scalatest.Suite
 * import org.scalatest.BeforeAndAfter
 * import collection.mutable.ListBuffer
 *
 * class ExampleSuite extends Suite with BeforeAndAfter {
 *
 *   val builder = new StringBuilder
 *   val buffer = new ListBuffer[String]
 *
 *   before {
 *     builder.append("ScalaTest is ")
 *   }
 *
 *   after {
 *     builder.clear()
 *     buffer.clear()
 *   }
 *
 *   def &#96;test: testing should be easy&#96; {
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 *
 *   def &#96;test: testing should be fun&#96; {
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(buffer.isEmpty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * Note that the only way <code>before</code> and <code>after</code> code can communicate with test code is via some side-effecting mechanism, commonly by
 * reassigning instance <code>var</code>s or by changing the state of mutable objects held from instance <code>val</code>s (as in this example). If using
 * instance <code>var</code>s or mutable objects held from instance <code>val</code>s you wouldn't be able to run tests in parallel in the same instance
 * of the test class unless you synchronized access to the shared, mutable state. This is why ScalaTest's <code>ParallelTestExecution</code> trait extends
 * <code>OneInstancePerTest</code>. By running each test in its own instance of the class, each test has its own copy of the instance variables, so you
 * don't need to synchronize. If you mixed <code>ParallelTestExecution</code> into the <code>ExampleSuite</code> above, the tests would run in parallel just fine
 * without any synchronization needed on the mutable <code>StringBuilder</code> and <code>ListBuffer[String]</code> objects.
 * </p>
 *
 * <p>
 * Although <code>BeforeAndAfter</code> provides a minimal-boilerplate way to execute code before and after tests, it isn't designed to enable stackable
 * traits, because the order of execution would be non-obvious.  If you want to factor out before and after code that is common to multiple test suites, you 
 * should use trait <code>BeforeAndAfterEach</code> instead, as shown later in the next section,
 * <a href="#composingFixtures.html">composing fixtures by stacking traits</a>.
 * </p>
 *
 * <a name="composingFixtures"></a><h2>Composing fixtures by stacking traits</h2>
 *
 * <p>
 * In larger projects, teams often end up with several different fixtures that test classes need in different combinations,
 * and possibly initialized (and cleaned up) in different orders. A good way to accomplish this in ScalaTest is to factor the individual
 * fixtures into traits that can be composed using the <em>stackable trait</em> pattern. This can be done, for example, by placing
 * <code>withFixture</code> methods in several traits, each of which call <code>super.withFixture</code>. Here's an example in
 * which the <code>StringBuilder</code> and <code>ListBuffer[String]</code> fixtures used in the previous examples have been
 * factored out into two <em>stackable fixture traits</em> named <code>Builder</code> and <code>Buffer</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.suite.composingwithfixture
 *
 * import org.scalatest.Suite
 * import org.scalatest.AbstractSuite
 * import collection.mutable.ListBuffer
 * 
 * trait Builder extends AbstractSuite { this: Suite =>
 * 
 *   val builder = new StringBuilder
 * 
 *   abstract override def withFixture(test: NoArgTest) {
 *     builder.append("ScalaTest is ")
 *     try {
 *       super.withFixture(test) // To be stackable, must call super.withFixture
 *     }
 *     finally {
 *       builder.clear()
 *     }
 *   }
 * }
 * 
 * trait Buffer extends AbstractSuite { this: Suite =>
 * 
 *   val buffer = new ListBuffer[String]
 * 
 *   abstract override def withFixture(test: NoArgTest) {
 *     try {
 *       super.withFixture(test) // To be stackable, must call super.withFixture
 *     }
 *     finally {
 *       buffer.clear()
 *     }
 *   }
 * }
 * 
 * class ExampleSuite extends Suite with Builder with Buffer {
 * 
 *   def &#96;test: testing should be easy&#96; {
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 * 
 *   def &#96;test: testing should be fun&#96; {
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(buffer.isEmpty)
 *     buffer += "clear"
 *   }
 * }
 * </pre>
 *
 * <p>
 * By mixing in both the <code>Builder</code> and <code>Buffer</code> traits, <code>ExampleSuite</code> gets both fixtures, which will be
 * initialized before each test and cleaned up after. The order the traits are mixed together determines the order of execution.
 * In this case, <code>Builder</code> is "super" to </code>Buffer</code>. If you wanted <code>Buffer</code> to be "super"
 * to <code>Builder</code>, you need only switch the order you mix them together, like this: 
 * </p>
 *
 * <pre class="stHighlight">
 * class Example2Suite extends Suite with Buffer with Builder
 * </pre>
 *
 * <p>
 * And if you only need one fixture you mix in only that trait:
 * </p>
 *
 * <pre class="stHighlight">
 * class Example3Suite extends Suite with Builder
 * </pre>
 *
 * <p>
 * Another way to create stackable fixture traits is by extending the <a href="BeforeAndAfterEach.html"><code>BeforeAndAfterEach</code></a>
 * and/or <a href="BeforeAndAfterAll.html"><code>BeforeAndAfterAll</code></a> traits.
 * <code>BeforeAndAfterEach</code> has a <code>beforeEach</code> method that will be run before each test (like JUnit's <code>setUp</code>),
 * and an <code>afterEach</code> method that will be run after (like JUnit's <code>tearDown</code>).
 * Similarly, <code>BeforeAndAfterAll</code> has a <code>beforeAll</code> method that will be run before all tests,
 * and an <code>afterAll</code> method that will be run after all tests. Here's what the previously shown example would look like if it
 * were rewritten to use the <code>BeforeAndAfterEach</code> methods instead of <code>withFixture</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.suite.composingbeforeandaftereach
 *
 * import org.scalatest.Suite
 * import org.scalatest.BeforeAndAfterEach
 * import collection.mutable.ListBuffer
 * 
 * trait Builder extends BeforeAndAfterEach { this: Suite =>
 * 
 *   val builder = new StringBuilder
 * 
 *   override def beforeEach() {
 *     builder.append("ScalaTest is ")
 *     super.beforeEach() // To be stackable, must call super.beforeEach
 *   }
 * 
 *   override def afterEach() {
 *     try {
 *       super.afterEach() // To be stackable, must call super.afterEach
 *     }
 *     finally {
 *       builder.clear()
 *     }
 *   }
 * }
 * 
 * trait Buffer extends BeforeAndAfterEach { this: Suite =>
 * 
 *   val buffer = new ListBuffer[String]
 * 
 *   override def afterEach() {
 *     try {
 *       super.afterEach() // To be stackable, must call super.afterEach
 *     }
 *     finally {
 *       buffer.clear()
 *     }
 *   }
 * }
 * 
 * class ExampleSuite extends Suite with Builder with Buffer {
 * 
 *   def &#96;test: testing should be easy&#96; {
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 * 
 *   def &#96;test: testing should be fun&#96; {
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(buffer.isEmpty)
 *     buffer += "clear"
 *   }
 * }
 * </pre>
 *
 * <p>
 * To get the same ordering as <code>withFixture</code>, place your <code>super.beforeEach</code> call at the end of each
 * <code>beforeEach</code> method, and the <code>super.afterEach</code> call at the beginning of each <code>afterEach</code>
 * method, as shown in the previous example. It is a good idea to invoke <code>super.afterEach</code> in a <code>try</code>
 * block and perform cleanup in a <code>finally</code> clause, as shown in the previous example, because this ensures the
 * cleanup code is performed even if <code>super.afterEach</code> throws an exception.
 * </p>
 *
 * <p>
 * The difference between stacking traits that extend <code>BeforeAndAfterEach</code> versus traits that implement <code>withFixture</code> is
 * that setup and cleanup code happens before and after the test in <code>BeforeAndAfterEach</code>, but at the beginning and
 * end of the test in <code>withFixture</code>. Thus if a <code>withFixture</code> method completes abruptly with an exception, it is
 * considered a failed test. By contrast, if any of the <code>beforeEach</code> or <code>afterEach</code> methods of <code>BeforeAndAfterEach</code> 
 * complete abruptly, it is considered a failed suite, which will result in a <a href="events/SuiteAborted.html"><code>SuiteAborted</code></a> event.
 * </p>
 * 
 * <a name="sharedFixtures"></a><h2>OLD ONE FOR REFERENCE Shared fixtures</h2>
 *
 * <p>
 * A <em>fixture</em> is objects or other artifacts (such as files, sockets, database
 * connections, <em>etc.</em>) used by tests to do their work.
 * If a fixture is used by only one test, then the definitions of the fixture objects can
 * be local to the method, such as the objects assigned to <code>sum</code> and <code>diff</code> in the
 * previous <code>ExampleSpec</code> examples. If multiple tests need to share immutable fixtures, one approach
 * is to assign them to instance variables.
 * </p>
 *
 * <p>
 * In some cases, however, shared <em>mutable</em> fixture objects may be changed by test methods such that
 * they need to be recreated or reinitialized before each test. Shared resources such
 * as files or database connections may also need to 
 * be created and initialized before, and cleaned up after, each test. JUnit 3 offered methods <code>setUp</code> and
 * <code>tearDown</code> for this purpose. In ScalaTest, you can use the <code>BeforeAndAfterEach</code> trait,
 * which will be described later, to implement an approach similar to JUnit's <code>setUp</code>
 * and <code>tearDown</code>, however, this approach usually involves reassigning <code>var</code>s or mutating objects
 * between tests. Before going that route, you may wish to consider some more functional approaches that
 * avoid side effects.
 * </p>
 *
 * <h4>Calling create-fixture methods</h4>
 *
 * <p>
 * One approach is to write one or more <em>create-fixture</em> methods
 * that return a new instance of a needed fixture object (or an holder object containing multiple needed fixture objects) each time it
 * is called. You can then call a create-fixture method at the beginning of each
 * test method that needs the fixture, storing the returned object or objects in local variables. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 * import collection.mutable.ListBuffer
 *
 * class ExampleSpec extends FlatSpec {
 * 
 *   def fixture =
 *     new {
 *       val builder = new StringBuilder("ScalaTest is ")
 *       val buffer = new ListBuffer[String]
 *     }
 * 
 *   "Testing" should "be easy" in {
 *     val f = fixture
 *     f.builder.append("easy!")
 *     assert(f.builder.toString === "ScalaTest is easy!")
 *     assert(f.buffer.isEmpty)
 *     f.buffer += "sweet"
 *   }
 * 
 *   it should "be fun" in {
 *     val f = fixture
 *     f.builder.append("fun!")
 *     assert(f.builder.toString === "ScalaTest is fun!")
 *     assert(f.buffer.isEmpty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * The &ldquo;<code>f.</code>&rdquo; in front of each use of a fixture object provides a visual indication of which objects 
 * are part of the fixture, but if you prefer, you can import the the members with &ldquo;<code>import f._</code>&rdquo; and use the names directly.
 * </p>
 *
 * <h4>Instantiating fixture traits</h4>
 *
 * <p>
 * A related technique is to place
 * the fixture objects in a <em>fixture trait</em> and run your test code in the context of a new anonymous class instance that mixes in
 * the fixture trait, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 * import collection.mutable.ListBuffer
 * 
 * class ExampleSpec extends FlatSpec {
 * 
 *   trait Fixture {
 *     val builder = new StringBuilder("ScalaTest is ")
 *     val buffer = new ListBuffer[String]
 *   }
 * 
 *   "Testing" should "be easy" in {
 *     new Fixture {
 *       builder.append("easy!")
 *       assert(builder.toString === "ScalaTest is easy!")
 *       assert(buffer.isEmpty)
 *       buffer += "sweet"
 *     }
 *   }
 * 
 *   it should "be fun" in {
 *     new Fixture {
 *       builder.append("fun!")
 *       assert(builder.toString === "ScalaTest is fun!")
 *       assert(buffer.isEmpty)
 *     }
 *   }
 * }
 * </pre>
 *
 * <h4>Mixing in <code>OneInstancePerTest</code></h4>
 *
 * <p>
 * If every test method requires the same set of
 * mutable fixture objects, one other approach you can take is make them simply <code>val</code>s and mix in trait
 * <a href="OneInstancePerTest.html"><code>OneInstancePerTest</code></a>.  If you mix in <code>OneInstancePerTest</code>, each test
 * will be run in its own instance of the <code>Suite</code>, similar to the way JUnit tests are executed. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 * import org.scalatest.OneInstancePerTest
 * import collection.mutable.ListBuffer
 * 
 * class ExampleSpec extends FlatSpec with OneInstancePerTest {
 * 
 *   val builder = new StringBuilder("ScalaTest is ")
 *   val buffer = new ListBuffer[String]
 * 
 *   "Testing" should "be easy" in {
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 * 
 *   it should "be fun" in {
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(buffer.isEmpty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * Although the create-fixture, fixture-trait, and <code>OneInstancePerTest</code> approaches take care of setting up a fixture before each
 * test, they don't address the problem of cleaning up a fixture after the test completes. In this situation, you'll need to either
 * use side effects or the <em>loan pattern</em>.
 * </p>
 *
 * <h4>Mixing in <code>BeforeAndAfter</code></h4>
 *
 * <p>
 * One way to use side effects is to mix in the <a href="BeforeAndAfter.html"><code>BeforeAndAfter</code></a> trait.
 * With this trait you can denote a bit of code to run before each test with <code>before</code> and/or after each test
 * each test with <code>after</code>, like this:
 * </p>
 * 
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 * import org.scalatest.BeforeAndAfter
 * import collection.mutable.ListBuffer
 * 
 * class ExampleSpec extends FlatSpec with BeforeAndAfter {
 * 
 *   val builder = new StringBuilder
 *   val buffer = new ListBuffer[String]
 * 
 *   before {
 *     builder.append("ScalaTest is ")
 *   }
 * 
 *   after {
 *     builder.clear()
 *     buffer.clear()
 *   }
 * 
 *   "Testing" should "be easy" in {
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 * 
 *   it should "be fun" in {
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(buffer.isEmpty)
 *   }
 * }
 * </pre>
 * 
 * <h4>Overriding <code>withFixture(NoArgTest)</code></h4>
 *
 * <p>
 * An alternate way to take care of setup and cleanup via side effects
 * is to override <code>withFixture</code>. Trait <code>Suite</code>'s implementation of
 * <code>runTest</code>, which is inherited by this trait, passes a no-arg test function to <code>withFixture</code>. It is <code>withFixture</code>'s
 * responsibility to invoke that test function.  <code>Suite</code>'s implementation of <code>withFixture</code> simply
 * invokes the function, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * // Default implementation
 * protected def withFixture(test: NoArgTest) {
 *   test()
 * }
 * </pre>
 *
 * <p>
 * You can, therefore, override <code>withFixture</code> to perform setup before, and cleanup after, invoking the test function. If
 * you have cleanup to perform, you should invoke the test function
 * inside a <code>try</code> block and perform the cleanup in a <code>finally</code> clause.
 * Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 * import collection.mutable.ListBuffer
 *
 * class ExampleSpec extends FlatSpec {
 *
 *   val builder = new StringBuilder
 *   val buffer = new ListBuffer[String]
 *
 *   override def withFixture(test: NoArgTest) {
 *     builder.append("ScalaTest is ") // perform setup
 *     try {
 *       test() // invoke the test function
 *     }
 *     finally {
 *       builder.clear() // perform cleanup
 *       buffer.clear()
 *     }
 *   }
 *
 *   "Testing" should "be easy" in {
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 *
 *   it should "be fun" in {
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(buffer.isEmpty)
 *     buffer += "clear"
 *   }
 * }
 * </pre>
 *
 * <p>
 * Note that the <a href="Suite$NoArgTest.html"><code>NoArgTest</code></a> passed to <code>withFixture</code>, in addition to
 * an <code>apply</code> method that executes the test, also includes the test name as well as the <a href="Suite.html#configMapSection">config
 * map</a> passed to <code>runTest</code>. Thus you can also use the test name and configuration objects in <code>withFixture</code>.
 * </p>
 *
 * <p>
 * The reason you should perform cleanup in a <code>finally</code> clause is that <code>withFixture</code> is called by
 * <code>runTest</code>, which expects an exception to be thrown to indicate a failed test. Thus when you invoke
 * the <code>test</code> function inside <code>withFixture</code>, it may complete abruptly with an exception. The <code>finally</code>
 * clause will ensure the fixture cleanup happens as that exception propagates back up the call stack to <code>runTest</code>.
 * </p>
 *
 * <h4>Overriding <code>withFixture(OneArgTest)</code></h4>
 *
 * <p>
 * To use the loan pattern, you can extend <code>fixture.FlatSpec</code> (from the <code>org.scalatest.fixture</code> package) instead of
 * <code>FlatSpec</code>. Each test in a <code>fixture.FlatSpec</code> takes a fixture as a parameter, allowing you to pass the fixture into
 * the test. You must indicate the type of the fixture parameter by specifying <code>FixtureParam</code>, and implement a
 * <code>withFixture</code> method that takes a <code>OneArgTest</code>. This <code>withFixture</code> method is responsible for
 * invoking the one-arg test function, so you can perform fixture set up before, and clean up after, invoking and passing
 * the fixture into the test function. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.fixture
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class ExampleSpec extends fixture.FlatSpec {
 * 
 *   final val tmpFile = "temp.txt"
 * 
 *   type FixtureParam = FileWriter
 * 
 *   def withFixture(test: OneArgTest) {
 * 
 *     val writer = new FileWriter(tmpFile) // set up the fixture
 *     try {
 *       test(writer) // "loan" the fixture to the test
 *     }
 *     finally {
 *       writer.close() // clean up the fixture
 *     }
 *   }
 * 
 *   "Testing" should "be easy" in { writer =>
 *     writer.write("Hello, test!")
 *     writer.flush()
 *     assert(new File(tmpFile).length === 12)
 *   }
 * 
 *   it should "be fun" in { writer =>
 *     writer.write("Hi, test!")
 *     writer.flush()
 *     assert(new File(tmpFile).length === 9)
 *   }
 * }
 * </pre>
 *
 * <p>
 * For more information, see the <a href="fixture/FlatSpec.html">documentation for <code>fixture.FlatSpec</code></a>.
 * </p>
 *
 * <a name="differentFixtures"></a><h2>Providing different fixtures to different tests</h2>
 * 
 * <p>
 * If different tests in the same <code>FlatSpec</code> require different fixtures, you can combine the previous techniques and
 * provide each test with just the fixture or fixtures it needs. Here's an example in which a <code>StringBuilder</code> and a
 * <code>ListBuffer</code> are provided via fixture traits, and file writer (that requires cleanup) is provided via the loan pattern:
 * </p>
 *
 * <pre class="stHighlight">
 * import java.io.FileWriter
 * import java.io.File
 * import collection.mutable.ListBuffer
 * import org.scalatest.FlatSpec
 * 
 * class ExampleSpec extends FlatSpec {
 * 
 *   final val tmpFile = "temp.txt"
 * 
 *   trait Builder {
 *     val builder = new StringBuilder("ScalaTest is ")
 *   }
 * 
 *   trait Buffer {
 *     val buffer = ListBuffer("ScalaTest", "is")
 *   }
 * 
 *   def withWriter(testCode: FileWriter => Any) {
 *     val writer = new FileWriter(tmpFile) // set up the fixture
 *     try {
 *       testCode(writer) // "loan" the fixture to the test
 *     }
 *     finally {
 *       writer.close() // clean up the fixture
 *     }
 *   }
 * 
 *   "Testing" should "be productive" in { // This test needs the StringBuilder fixture
 *     new Builder {
 *       builder.append("productive!")
 *       assert(builder.toString === "ScalaTest is productive!")
 *     }
 *   }
 * 
 *   it should "be readable" in { // This test needs the ListBuffer[String] fixture
 *     new Buffer {
 *       buffer += ("readable!")
 *       assert(buffer === List("ScalaTest", "is", "readable!"))
 *     }
 *   }
 * 
 *   it should "be user-friendly" in { // This test needs the FileWriter fixture
 *     withWriter { writer =>
 *       writer.write("Hello, user!")
 *       writer.flush()
 *       assert(new File(tmpFile).length === 12)
 *     }
 *   }
 * 
 *   "Test code" should "be clear and concise" in { // This test needs the StringBuilder and ListBuffer
 *     new Builder with Buffer {
 *       builder.append("clear!")
 *       buffer += ("concise!")
 *       assert(builder.toString === "ScalaTest is clear!")
 *       assert(buffer === List("ScalaTest", "is", "concise!"))
 *     }
 *   }
 * 
 *   it should "be composable" in { // This test needs all three fixtures
 *     new Builder with Buffer {
 *       builder.append("clear!")
 *       buffer += ("concise!")
 *       assert(builder.toString === "ScalaTest is clear!")
 *       assert(buffer === List("ScalaTest", "is", "concise!"))
 *       withWriter { writer =>
 *         writer.write(builder.toString)
 *         writer.flush()
 *         assert(new File(tmpFile).length === 19)
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * In the previous example, <code>ScalaTest should be productive</code> uses only the <code>StringBuilder</code> fixture, so it just instantiates
 * a <code>new Builder</code>, whereas <code>it should be readable</code> uses only the <code>ListBuffer</code> fixture, so it just intantiates
 * a <code>new Buffer</code>. <code>it should be user-friendly</code> needs just the <code>FileWriter</code> fixture, so it invokes
 * <code>withWriter</code>, which prepares and passes a <code>FileWriter</code> to the test (and takes care of closing it afterwords).
 * </p>
 *
 * <p>
 * Two tests need multiple fixtures: <code>Test code should be clear and concise</code> needs both the <code>StringBuilder</code> and the
 * <code>ListBuffer</code>, so it instantiates a class that mixes in both fixture traits with <code>new Builder with Buffer</code>.
 * <code>it should be composable</code> needs all three fixtures, so in addition to <code>new Builder with Buffer</code> it also invokes
 * <code>withWriter</code>, wrapping just the of the test code that needs the fixture.
 * </p>
 *
 * <p>
 * Note that in this case, the loan pattern is being implemented via the <code>withWriter</code> method that takes a function, not
 * by overriding <code>fixture.FlatSpec</code>'s <code>withFixture(OneArgTest)</code> method. <code>fixture.FlatSpec</code> makes the most sense
 * if all (or at least most) tests need the same fixture, whereas in this <code>Suite</code> only two tests need the
 * <code>FileWriter</code>.
 * </p>
 *
 * <p>
 * In the previous example, the <code>withWriter</code> method passed an object into
 * the tests. Passing fixture objects into tests is generally a good idea when possible, but sometimes a side affect is unavoidable.
 * For example, if you need to initialize a database running on a server across a network, your with-fixture 
 * method will likely have nothing to pass. In such cases, simply create a with-fixture method that takes a by-name parameter and
 * performs setup and cleanup via side effects, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * def withDataInDatabase(test: => Any) {
 *   // initialize the database across the network
 *   try {
 *     test // "loan" the initialized database to the test
 *   }
 *   finally {
 *     // clean up the database
 *   }
 * }
 * </pre>
 * 
 * <p>
 * You can then use it like:
 * </p>
 * 
 * <pre class="stHighlight">
 * "A user" should "be able to log onto the system" in {
 *   withDataInDatabase {
 *     // test user logging in scenario
 *   }
 * }
 * </pre>
 * 
 * <a name="composingFixtures"></a><h2>Composing stackable fixture traits</h2>
 *
 * <p>
 * In larger projects, teams often end up with several different fixtures that test classes need in different combinations,
 * and possibly initialized (and cleaned up) in different orders. A good way to accomplish this in ScalaTest is to factor the individual
 * fixtures into traits that can be composed using the <em>stackable trait</em> pattern. This can be done, for example, by placing
 * <code>withFixture</code> methods in several traits, each of which call <code>super.withFixture</code>. Here's an example in
 * which the <code>StringBuilder</code> and <code>ListBuffer[String]</code> fixtures used in the previous examples have been
 * factored out into two <em>stackable fixture traits</em> named <code>Builder</code> and <code>Buffer</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 * import org.scalatest.AbstractSuite
 * import collection.mutable.ListBuffer
 * 
 * trait Builder extends AbstractSuite { this: Suite =>
 *
 *   val builder = new StringBuilder
 *
 *   abstract override def withFixture(test: NoArgTest) {
 *     builder.append("ScalaTest is ")
 *     try {
 *       super.withFixture(test) // To be stackable, must call super.withFixture
 *     }
 *     finally {
 *       builder.clear()
 *     }
 *   }
 * }
 *
 * trait Buffer extends AbstractSuite { this: Suite =>
 *
 *   val buffer = new ListBuffer[String]
 *
 *   abstract override def withFixture(test: NoArgTest) {
 *     try {
 *       super.withFixture(test) // To be stackable, must call super.withFixture
 *     }
 *     finally {
 *       buffer.clear()
 *     }
 *   }
 * }
 * 
 * class ExampleSpec extends FlatSpec with Builder with Buffer {
 * 
 *   "Testing" should "be easy" in {
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 * 
 *   it should "be fun" in {
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(buffer.isEmpty)
 *     buffer += "clear"
 *   }
 * }
 * </pre>
 *
 * <p>
 * By mixing in both the <code>Builder</code> and <code>Buffer</code> traits, <code>ExampleSpec</code> gets both fixtures, which will be
 * initialized before each test and cleaned up after. The order the traits are mixed together determines the order of execution.
 * In this case, <code>Builder</code> is "super" to </code>Buffer</code>. If you wanted <code>Buffer</code> to be "super"
 * to <code>Builder</code>, you need only switch the order you mix them together, like this: 
 * </p>
 *
 * <pre class="stHighlight">
 * class Example2Spec extends FlatSpec with Buffer with Builder
 * </pre>
 *
 * <p>
 * And if you only need one fixture you mix in only that trait:
 * </p>
 *
 * <pre class="stHighlight">
 * class Example3Spec extends FlatSpec with Builder
 * </pre>
 *
 * <p>
 * Another way to create stackable fixture traits is by extending the <a href="BeforeAndAfterEach.html"><code>BeforeAndAfterEach</code></a>
 * and/or <a href="BeforeAndAfterAll.html"><code>BeforeAndAfterAll</code></a> traits.
 * <code>BeforeAndAfterEach</code> has a <code>beforeEach</code> method that will be run before each test (like JUnit's <code>setUp</code>),
 * and an <code>afterEach</code> method that will be run after (like JUnit's <code>tearDown</code>).
 * Similarly, <code>BeforeAndAfterAll</code> has a <code>beforeAll</code> method that will be run before all tests,
 * and an <code>afterAll</code> method that will be run after all tests. Here's what the previously shown example would look like if it
 * were rewritten to use the <code>BeforeAndAfterEach</code> methods instead of <code>withFixture</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.FlatSpec
 * import org.scalatest.BeforeAndAfterEach
 * import collection.mutable.ListBuffer
 * 
 * trait Builder extends BeforeAndAfterEach { this: Suite =>
 * 
 *   val builder = new StringBuilder
 * 
 *   override def beforeEach() {
 *     builder.append("ScalaTest is ")
 *     super.beforeEach() // To be stackable, must call super.beforeEach
 *   }
 * 
 *   override def afterEach() {
 *     try {
 *       super.afterEach() // To be stackable, must call super.afterEach
 *     }
 *     finally {
 *       builder.clear()
 *     }
 *   }
 * }
 * 
 * trait Buffer extends BeforeAndAfterEach { this: Suite =>
 * 
 *   val buffer = new ListBuffer[String]
 * 
 *   override def afterEach() {
 *     try {
 *       super.afterEach() // To be stackable, must call super.afterEach
 *     }
 *     finally {
 *       buffer.clear()
 *     }
 *   }
 * }
 * 
 * class ExampleSpec extends FlatSpec with Builder with Buffer {
 * 
 *   "Testing" should "be easy" in {
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 * 
 *   it should "be fun" in {
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(buffer.isEmpty)
 *     buffer += "clear"
 *   }
 * }
 * </pre>
 *
 * <p>
 * To get the same ordering as <code>withFixture</code>, place your <code>super.beforeEach</code> call at the end of each
 * <code>beforeEach</code> method, and the <code>super.afterEach</code> call at the beginning of each <code>afterEach</code>
 * method, as shown in the previous example. It is a good idea to invoke <code>super.afterEach</code> in a <code>try</code>
 * block and perform cleanup in a <code>finally</code> clause, as shown in the previous example, because this ensures the
 * cleanup code is performed even if <code>super.afterAll</code> throws an exception.
 * </p>
 *
 * <p>
 * One difference to bear in mind between the before-and-after traits and the <code>withFixture</code> methods, is that if
 * a <code>withFixture</code> method completes abruptly with an exception, it is considered a failed test. By contrast, if any of the
 * methods on the before-and-after traits (<em>i.e.</em>, <code>before</code>  and <code>after</code> of <code>BeforeAndAfter</code>,
 * <code>beforeEach</code> and <code>afterEach</code> of <code>BeforeAndAfterEach</code>,
 * and <code>beforeAll</code> and <code>afterAll</code> of <code>BeforeAndAfterAll</code>) complete abruptly, it is considered a
 * failed suite, which will result in a <a href="events/SuiteAborted.html"><code>SuiteAborted</code></a> event.
 * </p>
 * 
 * <a name="SharedTests"></a><h2>Shared tests</h2>
 *
 * <p>
 * Sometimes you may want to run the same test code on different fixture objects. In other words, you may want to write tests that are "shared"
 * by different fixture objects.  To accomplish this in a <code>FlatSpec</code>, you first place shared tests in <em>behavior functions</em>.
 * These behavior functions will be invoked during the construction phase of any <code>FlatSpec</code> that uses them, so that the tests they
 * contain will be registered as tests in that <code>FlatSpec</code>.  For example, given this stack class:
 * </p>
 *
 * <pre class="stHighlight">
 * import scala.collection.mutable.ListBuffer
 * 
 * class Stack[T] {
 *
 *   val MAX = 10
 *   private val buf = new ListBuffer[T]
 *
 *   def push(o: T) {
 *     if (!full)
 *       buf.prepend(o)
 *     else
 *       throw new IllegalStateException("can't push onto a full stack")
 *   }
 *
 *   def pop(): T = {
 *     if (!empty)
 *       buf.remove(0)
 *     else
 *       throw new IllegalStateException("can't pop an empty stack")
 *   }
 *
 *   def peek: T = {
 *     if (!empty)
 *       buf(0)
 *     else
 *       throw new IllegalStateException("can't pop an empty stack")
 *   }
 *
 *   def full: Boolean = buf.size == MAX
 *   def empty: Boolean = buf.size == 0
 *   def size = buf.size
 *
 *   override def toString = buf.mkString("Stack(", ", ", ")")
 * }
 * </pre>
 *
 * <p>
 * You may want to test the <code>Stack</code> class in different states: empty, full, with one item, with one item less than capacity,
 * <em>etc</em>. You may find you have several tests that make sense any time the stack is non-empty. Thus you'd ideally want to run
 * those same tests for three stack fixture objects: a full stack, a stack with a one item, and a stack with one item less than
 * capacity. With shared tests, you can factor these tests out into a behavior function, into which you pass the
 * stack fixture to use when running the tests. So in your <code>FlatSpec</code> for stack, you'd invoke the
 * behavior function three times, passing in each of the three stack fixtures so that the shared tests are run for all three fixtures. You
 * can define a behavior function that encapsulates these shared tests inside the <code>FlatSpec</code> that uses them. If they are shared
 * between different <code>FlatSpec</code>s, however, you could also define them in a separate trait that is mixed into each <code>FlatSpec</code>
 * that uses them.
 * </p>
 *
 * <p>
 * <a name="StackBehaviors">For</a> example, here the <code>nonEmptyStack</code> behavior function (in this case, a behavior <em>method</em>) is
 * defined in a trait along with another method containing shared tests for non-full stacks:
 * </p>
 * 
 * <pre class="stHighlight">
 * trait StackBehaviors { this: FlatSpec =&gt;
 * 
 *   def nonEmptyStack(newStack: =&gt; Stack[Int], lastItemAdded: Int) {
 *
 *     it should "be non-empty" in {
 *       assert(!newStack.empty)
 *     }
 *
 *     it should "return the top item on peek" in {
 *       assert(newStack.peek === lastItemAdded)
 *     }
 *
 *     it should "not remove the top item on peek" in {
 *       val stack = newStack
 *       val size = stack.size
 *       assert(stack.peek === lastItemAdded)
 *       assert(stack.size === size)
 *     }
 *
 *     it should "remove the top item on pop" in {
 *       val stack = newStack
 *       val size = stack.size
 *       assert(stack.pop === lastItemAdded)
 *       assert(stack.size === size - 1)
 *     }
 *   }
 *
 *   def nonFullStack(newStack: =&gt; Stack[Int]) {
 *
 *     it should "not be full" in {
 *       assert(!newStack.full)
 *     }
 *
 *     it should "add to the top on push" in {
 *       val stack = newStack
 *       val size = stack.size
 *       stack.push(7)
 *       assert(stack.size === size + 1)
 *       assert(stack.peek === 7)
 *     }
 *   }
 * }
 * </pre>
 *
 *
 * <p>
 * Given these behavior functions, you could invoke them directly, but <code>FlatSpec</code> offers a DSL for the purpose,
 * which looks like this:
 * </p>
 *
 * <pre class="stHighlight">
 * it should behave like nonEmptyStack(stackWithOneItem, lastValuePushed)
 * it should behave like nonFullStack(stackWithOneItem)
 * </pre>
 *
 * <p>
 * If you prefer to use an imperative style to change fixtures, for example by mixing in <code>BeforeAndAfterEach</code> and
 * reassigning a <code>stack</code> <code>var</code> in <code>beforeEach</code>, you could write your behavior functions
 * in the context of that <code>var</code>, which means you wouldn't need to pass in the stack fixture because it would be
 * in scope already inside the behavior function. In that case, your code would look like this:
 * </p>
 *
 * <pre class="stHighlight">
 * it should behave like nonEmptyStack // assuming lastValuePushed is also in scope inside nonEmptyStack
 * it should behave like nonFullStack
 * </pre>
 *
 * <p>
 * The recommended style, however, is the functional, pass-all-the-needed-values-in style. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * class SharedTestExampleSpec extends FlatSpec with StackBehaviors {
 * 
 *   // Stack fixture creation methods
 *   def emptyStack = new Stack[Int]
 * 
 *   def fullStack = {
 *     val stack = new Stack[Int]
 *     for (i <- 0 until stack.MAX)
 *       stack.push(i)
 *     stack
 *   }
 * 
 *   def stackWithOneItem = {
 *     val stack = new Stack[Int]
 *     stack.push(9)
 *     stack
 *   }
 * 
 *   def stackWithOneItemLessThanCapacity = {
 *     val stack = new Stack[Int]
 *     for (i <- 1 to 9)
 *       stack.push(i)
 *     stack
 *   }
 * 
 *   val lastValuePushed = 9
 * 
 *   "A Stack (when empty)" should "be empty" in {
 *     assert(emptyStack.empty)
 *   }
 * 
 *   it should "complain on peek" in {
 *     intercept[IllegalStateException] {
 *       emptyStack.peek
 *     }
 *   }
 *
 *   it should "complain on pop" in {
 *     intercept[IllegalStateException] {
 *       emptyStack.pop
 *     }
 *   }
 * 
 *   "A Stack (with one item)" should behave like nonEmptyStack(stackWithOneItem, lastValuePushed)
 *
 *   it should behave like nonFullStack(stackWithOneItem)
 *     
 *   "A Stack (with one item less than capacity)" should behave like nonEmptyStack(stackWithOneItemLessThanCapacity, lastValuePushed)
 *
 *   it should behave like nonFullStack(stackWithOneItemLessThanCapacity)
 * 
 *   "A Stack (full)" should "be full" in {
 *     assert(fullStack.full)
 *   }
 * 
 *   it should behave like nonEmptyStack(fullStack, lastValuePushed)
 * 
 *   it should "complain on a push" in {
 *     intercept[IllegalStateException] {
 *       fullStack.push(10)
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you load these classes into the Scala interpreter (with scalatest's JAR file on the class path), and execute it,
 * you'll see:
 * </p>
 *
 * <pre class="stREPL">
 * scala> (new SharedTestExampleSpec).execute()
 * <span class="stGreen">A Stack (when empty)
 * - should be empty
 * - should complain on peek
 * - should complain on pop
 * A Stack (with one item) 
 * - should be non-empty
 * - should return the top item on peek
 * - should not remove the top item on peek
 * - should remove the top item on pop
 * - should not be full
 * - should add to the top on push
 * A Stack (with one item less than capacity) 
 * - should be non-empty
 * - should return the top item on peek
 * - should not remove the top item on peek
 * - should remove the top item on pop
 * - should not be full
 * - should add to the top on push
 * A Stack (full) 
 * - should be full
 * - should be non-empty
 * - should return the top item on peek
 * - should not remove the top item on peek
 * - should remove the top item on pop
 * - should complain on a push</span>
 * </pre>
 * 
 * <p>
 * One thing to keep in mind when using shared tests is that in ScalaTest, each test in a suite must have a unique name.
 * If you register the same tests repeatedly in the same suite, one problem you may encounter is an exception at runtime
 * complaining that multiple tests are being registered with the same test name. A good way to solve this problem in a <code>WordSpec</code> is to make sure
 * each invocation of a behavior function is in the context of a different set of <code>when</code>, <em>verb</em> (<code>should</code>,
 * <code>must</code>, or </code>can</code>), and <code>that</code> clauses,
 * which will prepend a string to each test name.
 * For example, the following code in a <code>WordSpec</code> would register a test with the name <code>"A Stack (when empty) should be empty"</code>:
 * </p>
 *
 * <pre class="stHighlight">
 *   behavior of "A Stack (when empty)"
 *       
 *   it should "be empty" in {
 *     assert(emptyStack.empty)
 *   }
 *   // ...
 * </pre>
 *
 * <p>
 * Or, using the shorthand notation:
 * </p>
 *
 * <pre class="stHighlight">
 *   "A Stack" when {
 *     "empty" should {
 *       "be empty" in {
 *         assert(emptyStack.empty)
 *       }
 *     }
 *   }
 *   // ...
 * </pre>
 *
 * <p>
 * If the <code>"should be empty"</code> test was factored out into a behavior function, it could be called repeatedly so long
 * as each invocation of the behavior function is in the context of a different combination
 * of <code>when</code>, <em>verb</em>, and <code>that</code> clauses.
 * </p>
 *
 * @author Bill Venners
 */
@Style("org.scalatest.finders.FlatSpecFinder")
trait FlatSpec extends Suite with ShouldVerb with MustVerb with CanVerb { thisSuite =>

  private final val engine = new Engine("concurrentSpecMod", "Spec")
  import engine._

  /**
   * Returns an <code>Informer</code> that during test execution will forward strings passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked while this
   * <code>FlatSpec</code> is being executed, such as from inside a test function, it will forward the information to
   * the current reporter immediately. If invoked at any other time, it will
   * throw an exception. This method can be called safely by any thread.
   */
  implicit protected def info: Informer = atomicInformer.get

  /**
   * Returns a <code>Documenter</code> that during test execution will forward strings passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked while this
   * <code>FlatSpec</code> is being executed, such as from inside a test function, it will forward the information to
   * the current reporter immediately. If invoked at any other time, it will
   * throw an exception. This method can be called safely by any thread.
   */
  implicit protected def markup: Documenter = atomicDocumenter.get

  /**
   * Register a test with the given spec text, optional tags, and test function value that takes no arguments.
   * An invocation of this method is called an &#8220;example.&#8221;
   *
   * This method will register the test for later execution via an invocation of one of the <code>execute</code>
   * methods. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>FlatSpec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param methodName Method name of the caller
   * @param testTags the optional list of tags for this test
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullPointerException if <code>specText</code> or any passed test tag is <code>null</code>
   */
  private def registerTestToRun(specText: String, methodName: String, testTags: List[Tag], testFun: () => Unit) {
    registerTest(specText, testFun, "itCannotAppearInsideAnotherIt", "FlatSpec.scala", methodName, 4, -3, None, None, testTags: _*)
  }

  /**
   * Class that supports the registration of a &#8220;subject&#8221; being specified and tested via the
   * instance referenced from <code>FlatSpec</code>'s <code>behavior</code> field.
   *
   * <p>
   * This field enables syntax such as the following subject registration:
   * </p>
   *
   * <pre class="stHighlight">
   * behavior of "A Stack"
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>behavior</code> field, see the <a href="FlatSpec.html">main documentation</a>
   * for trait <code>FlatSpec</code>.
   * </p>
   */
  protected final class BehaviorWord {

    /**
     * Supports the registration of a &#8220;subject&#8221; being specified and tested via the
     * instance referenced from <code>FlatSpec</code>'s <code>behavior</code> field.
     *
     * <p>
     * This method enables syntax such as the following subject registration:
     * </p>
     *
     * <pre class="stHighlight">
     * behavior of "A Stack"
     *          ^
     * </pre>
     *
     * <p>
     * For more information and examples of the use of this method, see the <a href="FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def of(description: String) {
      registerFlatBranch(description, "describeCannotAppearInsideAnIt", "FlatSpec.scala", "of", 3)
    }
  }

  /**
   * Supports the registration of a &#8220;subject&#8221; being specified and tested.
   *
   * <p>
   * This field enables syntax such as the following subject registration:
   * </p>
   *
   * <pre class="stHighlight">
   * behavior of "A Stack"
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>behavior</code> field, see the main documentation 
   * for this trait.
   * </p>
   */
  protected val behavior = new BehaviorWord

  /**
   * Class that supports the registration of tagged tests via the <code>ItWord</code> instance
   * referenced from <code>FlatSpec</code>'s <code>it</code> field.
   *
   * <p>
   * This class enables syntax such as the following tagged test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                                      ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following registration of an ignored, tagged test:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { ... }
   *                                                                      ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of a pending, tagged test:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
   *                                                                      ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field to register tagged tests, see
   * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
   * For examples of tagged test registration, see
   * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
   * </p>
   */
  protected final class ItVerbStringTaggedAs(verb: String, name: String, tags: List[Tag]) {

    /**
     * Supports the registration of tagged tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see
     * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: => Unit) {
      registerTestToRun(verb + " " + name, "in", tags, testFun _)
    }

    /**
     * Supports the registration of pending, tagged tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of pending test registration, see the <a href="FlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def is(testFun: => PendingNothing) {
      registerTestToRun(verb + " " + name, "is", tags, testFun _)
    }

    /**
     * Supports the registration of ignored, tagged tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { ... }
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: => Unit) {
      registerTestToIgnore(verb + " " + name, tags, "ignore", testFun _)
    }
  }

  /**
   * Class that supports test registration via the <code>ItWord</code> instance referenced from <code>FlatSpec</code>'s <code>it</code> field.
   *
   * <p>
   * This class enables syntax such as the following test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" in { ... }
   *                                                   ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" ignore { ... }
   *                                                   ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of a pending test:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" is (pending)
   *                                                   ^
   * </pre>
   *
   * <p>
   * And finally, it also enables syntax such as the following tagged test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                   ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the <a href="FlatSpec.html">main documentation</a>
   * for trait <code>FlatSpec</code>.
   * </p>
   */
  protected final class ItVerbString(verb: String, name: String) {

    /**
     * Supports the registration of tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" in { ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: => Unit) {
      registerTestToRun(verb + " " + name, "in", List(), testFun _)
    }

    /**
     * Supports the registration of pending tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" is (pending)
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of pending test registration, see the <a href="FlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def is(testFun: => PendingNothing) {
      registerTestToRun(verb + " " + name, "is", List(), testFun _)
    }

    /**
     * Supports the registration of ignored tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" ignore { ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: => Unit) {
      registerTestToIgnore(verb + " " + name, List(), "ignore", testFun _)
    }

    /**
     * Supports the registration of tagged tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new ItVerbStringTaggedAs(verb, name, tagList)
    }
  }

  /**
   * Class that supports test (and shared test) registration via the instance referenced from <code>FlatSpec</code>'s <code>it</code> field.
   *
   * <p>
   * This class enables syntax such as the following test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following shared test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should behave like nonEmptyStack(lastItemPushed)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the main documentation 
   * for this trait.
   * </p>
   */
  protected final class ItWord {

    /**
     * Supports the registration of tests with <code>should</code> in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it should "pop values in last-in-first-out order" in { ... }
     *    ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def should(string: String) = new ItVerbString("should", string)

    /**
     * Supports the registration of tests with <code>must</code> in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" in { ... }
     *    ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def must(string: String) = new ItVerbString("must", string)

    /**
     * Supports the registration of tests with <code>can</code> in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it can "pop values in last-in-first-out order" in { ... }
     *    ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def can(string: String) = new ItVerbString("can", string)

    /**
     * Supports the registration of shared tests with <code>should</code> in a <code>FlatSpec</code>.
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
     * For examples of shared tests, see the <a href="FlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def should(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests with <code>must</code> in a <code>FlatSpec</code>.
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
     * For examples of shared tests, see the <a href="FlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def must(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests with <code>can</code> in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it can behave like nonFullStack(stackWithOneItem)
     *    ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="FlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def can(behaveWord: BehaveWord) = behaveWord
  }

  /**
   * Supports test (and shared test) registration in <code>FlatSpec</code>s.
   *
   * <p>
   * This field enables syntax such as the following test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following shared test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should behave like nonEmptyStack(lastItemPushed)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the main documentation 
   * for this trait.
   * </p>
   */
  protected val it = new ItWord

  /**
   * Class that supports registration of ignored, tagged tests via the <code>IgnoreWord</code> instance referenced
   * from <code>FlatSpec</code>'s <code>ignore</code> field.
   *
   * <p>
   * This class enables syntax such as the following registration of an ignored, tagged test:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                                          ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of an ignored, tagged, pending test:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
   *                                                                          ^
   * </pre>
   *
   * <p>
   * Note: the <code>is</code> method is provided for completeness and design symmetry, given there's no way
   * to prevent changing <code>is</code> to <code>ignore</code> and marking a pending test as ignored that way.
   * Although it isn't clear why someone would want to mark a pending test as ignored, it can be done.
   * </p>
   *
   * <p>
   * For more information and examples of the use of the <code>ignore</code> field, see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
   * in the main documentation for trait <code>FlatSpec</code>. For examples of tagged test registration, see
   * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
   * </p>
   */
  protected final class IgnoreVerbStringTaggedAs(verb: String, name: String, tags: List[Tag]) {

    /**
     * Supports the registration of ignored, tagged tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
     *                                                                        ^
     * </pre>
     *
     * <p>
     * For examples of the registration of ignored tests, see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>. For examples of tagged test registration, see
     * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: => Unit) {
      registerTestToIgnore(verb + " " + name, tags, "in", testFun _)
    }

    /**
     * Supports the registration of ignored, tagged, pending tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
     *                                                                        ^
     * </pre>
     *
     * <p>
     * Note: this <code>is</code> method is provided for completeness and design symmetry, given there's no way
     * to prevent changing <code>is</code> to <code>ignore</code> and marking a pending test as ignored that way.
     * Although it isn't clear why someone would want to mark a pending test as ignored, it can be done.
     * </p>
     *
     * <p>
     * For examples of pending test registration, see the <a href="FlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  For examples of the registration of ignored tests,
     * see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>. For examples of tagged test registration, see
     * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def is(testFun: => PendingNothing) {
      registerTestToIgnore(verb + " " + name, tags, "is", testFun _)
    }
    // Note: no def ignore here, so you can't put two ignores in the same line
  }

  /**
   * Class that supports registration of ignored tests via the <code>IgnoreWord</code> instance referenced
   * from <code>FlatSpec</code>'s <code>ignore</code> field.
   *
   * <p>
   * This class enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" in { ... }
   *                                                       ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of an ignored, pending test:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" is (pending)
   *                                                       ^
   * </pre>
   *
   * <p>
   * Note: the <code>is</code> method is provided for completeness and design symmetry, given there's no way
   * to prevent changing <code>is</code> to <code>ignore</code> and marking a pending test as ignored that way.
   * Although it isn't clear why someone would want to mark a pending test as ignored, it can be done.
   * </p>
   *
   * <p>
   * And finally, it also enables syntax such as the following ignored, tagged test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                       ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>ignore</code> field, see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
   * in the main documentation for trait <code>FlatSpec</code>.
   * </p>
   */
  protected final class IgnoreVerbString(verb: String, name: String) {

    /**
     * Supports the registration of ignored tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" in { ... }
     *                                                     ^
     * </pre>
     *
     * <p>
     * For examples of the registration of ignored tests, see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: => Unit) {
      registerTestToIgnore(verb + " " + name, List(), "in", testFun _)
    }

    /**
     * Supports the registration of ignored, pending tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" is (pending)
     *                                                     ^
     * </pre>
     *
     * <p>
     * Note: this <code>is</code> method is provided for completeness and design symmetry, given there's no way
     * to prevent changing <code>is</code> to <code>ignore</code> and marking a pending test as ignored that way.
     * Although it isn't clear why someone would want to mark a pending test as ignored, it can be done.
     * </p>
     *
     * <p>
     * For examples of pending test registration, see the <a href="FlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  For examples of the registration of ignored tests,
     * see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def is(testFun: => PendingNothing) {
      registerTestToIgnore(verb + " " + name, List(), "is", testFun _)
    }

    /**
     * Supports the registration of ignored, tagged tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
     *                                                     ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  For examples of the registration of ignored tests,
     * see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new IgnoreVerbStringTaggedAs(verb, name, tagList)
    }
  }

  /**
   * Class that supports registration of ignored tests via the <code>ItWord</code> instance
   * referenced from <code>FlatSpec</code>'s <code>ignore</code> field.
   *
   * <p>
   * This class enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>ignore</code> field, see <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
   * in the main documentation for this trait.
   * </p>
   */
  protected final class IgnoreWord {

    /**
     * Supports the registration of ignored tests with <code>should</code> in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore should "pop values in last-in-first-out order" in { ... }
     *        ^
     * </pre>
     *
     * <p>
     * For more information and examples of the use of the <code>ignore</code> field, see <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def should(string: String) = new IgnoreVerbString("should", string)

    /**
     * Supports the registration of ignored tests with <code>must</code> in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" in { ... }
     *        ^
     * </pre>
     *
     * <p>
     * For more information and examples of the use of the <code>ignore</code> field, see <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def must(string: String) = new IgnoreVerbString("must", string)

    /**
     * Supports the registration of ignored tests with <code>can</code> in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore can "pop values in last-in-first-out order" in { ... }
     *        ^
     * </pre>
     *
     * <p>
     * For more information and examples of the use of the <code>ignore</code> field, see <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def can(string: String) = new IgnoreVerbString("can", string)
  }

  /**
   * Supports registration of ignored tests in <code>FlatSpec</code>s.
   *
   * <p>
   * This field enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>ignore</code> field, see the <a href="#IgnoredTests">Ignored tests section</a>
   * in the main documentation for this trait.
   * </p>
   */
  protected val ignore = new IgnoreWord
  
  /**
   * Class that supports the registration of tagged tests via the <code>TheyWord</code> instance
   * referenced from <code>FlatSpec</code>'s <code>they</code> field.
   *
   * <p>
   * This class enables syntax such as the following tagged test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                                        ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following registration of an ignored, tagged test:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { ... }
   *                                                                        ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of a pending, tagged test:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
   *                                                                        ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>they</code> field to register tagged tests, see
   * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
   * For examples of tagged test registration, see
   * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
   * </p>
   */
  protected final class TheyVerbStringTaggedAs(verb: String, name: String, tags: List[Tag]) {

    /**
     * Supports the registration of tagged tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
     *                                                                      ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see
     * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: => Unit) {
      registerTestToRun(verb + " " + name, "in", tags, testFun _)
    }

    /**
     * Supports the registration of pending, tagged tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
     *                                                                      ^
     * </pre>
     *
     * <p>
     * For examples of pending test registration, see the <a href="FlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def is(testFun: => PendingNothing) {
      registerTestToRun(verb + " " + name, "is", tags, testFun _)
    }

    /**
     * Supports the registration of ignored, tagged tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { ... }
     *                                                                      ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: => Unit) {
      registerTestToIgnore(verb + " " + name, tags, "ignore", testFun _)
    }
  }

  /**
   * Class that supports test registration via the <code>TheyWord</code> instance referenced from <code>FlatSpec</code>'s <code>they</code> field.
   *
   * <p>
   * This class enables syntax such as the following test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" in { ... }
   *                                                     ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" ignore { ... }
   *                                                     ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of a pending test:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" is (pending)
   *                                                     ^
   * </pre>
   *
   * <p>
   * And finally, it also enables syntax such as the following tagged test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                     ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the <a href="FlatSpec.html">main documentation</a>
   * for trait <code>FlatSpec</code>.
   * </p>
   */
  protected final class TheyVerbString(verb: String, name: String) {

    /**
     * Supports the registration of tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" in { ... }
     *                                                   ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: => Unit) {
      registerTestToRun(verb + " " + name, "in", List(), testFun _)
    }

    /**
     * Supports the registration of pending tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" is (pending)
     *                                                   ^
     * </pre>
     *
     * <p>
     * For examples of pending test registration, see the <a href="FlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def is(testFun: => PendingNothing) {
      registerTestToRun(verb + " " + name, "is", List(), testFun _)
    }

    /**
     * Supports the registration of ignored tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" ignore { ... }
     *                                                   ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: => Unit) {
      registerTestToIgnore(verb + " " + name, List(), "ignore", testFun _)
    }

    /**
     * Supports the registration of tagged tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
     *                                                   ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new ItVerbStringTaggedAs(verb, name, tagList)
    }
  }

  /**
   * Class that supports test (and shared test) registration via the instance referenced from <code>FlatSpec</code>'s <code>it</code> field.
   *
   * <p>
   * This class enables syntax such as the following test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following shared test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should behave like nonEmptyStack(lastItemPushed)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the main documentation 
   * for this trait.
   * </p>
   */
  protected final class TheyWord {

    /**
     * Supports the registration of tests with <code>should</code> in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they should "pop values in last-in-first-out order" in { ... }
     *      ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def should(string: String) = new ItVerbString("should", string)

    /**
     * Supports the registration of tests with <code>must</code> in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" in { ... }
     *      ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def must(string: String) = new ItVerbString("must", string)

    /**
     * Supports the registration of tests with <code>can</code> in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they can "pop values in last-in-first-out order" in { ... }
     *      ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def can(string: String) = new ItVerbString("can", string)

    /**
     * Supports the registration of shared tests with <code>should</code> in a <code>FlatSpec</code>.
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
     * For examples of shared tests, see the <a href="FlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def should(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests with <code>must</code> in a <code>FlatSpec</code>.
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
     * For examples of shared tests, see the <a href="FlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def must(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests with <code>can</code> in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they can behave like nonFullStack(stackWithOneItem)
     *      ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="FlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def can(behaveWord: BehaveWord) = behaveWord
  }

  /**
   * Supports test (and shared test) registration in <code>FlatSpec</code>s.
   *
   * <p>
   * This field enables syntax such as the following test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following shared test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should behave like nonEmptyStack(lastItemPushed)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the main documentation 
   * for this trait.
   * </p>
   */
  protected val they = new TheyWord

  /**
   * Class that supports test registration in shorthand form.
   *
   * <p>
   * For example, this class enables syntax such as the following test registration
   * in shorthand form:
   * </p>
   *
   * <pre class="stHighlight">
   * "A Stack (when empty)" should "be empty" in { ... }
   *                                          ^
   * </pre>
   *
   * <p>
   * This class also enables syntax such as the following ignored test registration
   * in shorthand form:
   * </p>
   *
   * <pre class="stHighlight">
   * "A Stack (when empty)" should "be empty" ignore { ... }
   *                                          ^
   * </pre>
   *
   * <p>
   * This class is used via an implicit conversion (named <code>convertToInAndIgnoreMethods</code>)
   * from <code>ResultOfStringPassedToVerb</code>. The <code>ResultOfStringPassedToVerb</code> class
   * does not declare any methods named <code>in</code>, because the
   * type passed to <code>in</code> differs in a <code>FlatSpec</code> and a <code>fixture.FlatSpec</code>.
   * A <code>fixture.FlatSpec</code> needs two <code>in</code> methods, one that takes a no-arg
   * test function and another that takes a one-arg test function (a test that takes a
   * <code>Fixture</code> as its parameter). By constrast, a <code>FlatSpec</code> needs
   * only one <code>in</code> method that takes a by-name parameter. As a result,
   * <code>FlatSpec</code> and <code>fixture.FlatSpec</code> each provide an implicit conversion
   * from <code>ResultOfStringPassedToVerb</code> to a type that provides the appropriate
   * <code>in</code> methods.
   * </p>
   *
   * @author Bill Venners
   */
  protected final class InAndIgnoreMethods(resultOfStringPassedToVerb: ResultOfStringPassedToVerb) {

    import resultOfStringPassedToVerb.verb
    import resultOfStringPassedToVerb.rest

    /**
     * Supports the registration of tests in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "A Stack" must "pop values in last-in-first-out order" in { ... }
     *                                                        ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: => Unit) {
      registerTestToRun(verb + " " + rest, "in", List(), testFun _)
    }
    
    /**
     * Supports the registration of ignored tests in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "A Stack" must "pop values in last-in-first-out order" ignore { ... }
     *                                                        ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: => Unit) {
      registerTestToIgnore(verb + " " + rest, List(), "ignore", testFun _)
    }
  }

  /**
   * Implicitly converts an object of type <code>ResultOfStringPassedToVerb</code> to an
   * <code>InAndIgnoreMethods</code>, to enable <code>in</code> and <code>ignore</code>
   * methods to be invokable on that object.
   */
  protected implicit def convertToInAndIgnoreMethods(resultOfStringPassedToVerb: ResultOfStringPassedToVerb) =
    new InAndIgnoreMethods(resultOfStringPassedToVerb)
  
  /**
   * Class that supports tagged test registration in shorthand form.
   *
   * <p>
   * For example, this class enables syntax such as the following tagged test registration
   * in shorthand form:
   * </p>
   *
   * <pre class="stHighlight">
   * "A Stack (when empty)" should "be empty" taggedAs() in { ... }
   *                                                     ^
   * </pre>
   *
   * <p>
   * This class also enables syntax such as the following tagged, ignored test registration
   * in shorthand form:
   * </p>
   *
   * <pre class="stHighlight">
   * "A Stack (when empty)" should "be empty" taggedAs(SlowTest) ignore { ... }
   *                                                             ^
   * </pre>
   *
   * <p>
   * This class is used via an implicit conversion (named <code>convertToInAndIgnoreMethodsAfterTaggedAs</code>)
   * from <code>ResultOfTaggedAsInvocation</code>. The <code>ResultOfTaggedAsInvocation</code> class
   * does not declare any methods named <code>in</code>, because the
   * type passed to <code>in</code> differs in a <code>FlatSpec</code> and a <code>fixture.FlatSpec</code>.
   * A <code>fixture.FlatSpec</code> needs two <code>in</code> methods, one that takes a no-arg
   * test function and another that takes a one-arg test function (a test that takes a
   * <code>Fixture</code> as its parameter). By constrast, a <code>FlatSpec</code> needs
   * only one <code>in</code> method that takes a by-name parameter. As a result,
   * <code>FlatSpec</code> and <code>fixture.FlatSpec</code> each provide an implicit conversion
   * from <code>ResultOfTaggedAsInvocation</code> to a type that provides the appropriate
   * <code>in</code> methods.
   * </p>
   *
   * @author Bill Venners
   */
  protected final class InAndIgnoreMethodsAfterTaggedAs(resultOfTaggedAsInvocation: ResultOfTaggedAsInvocation) {

    import resultOfTaggedAsInvocation.verb
    import resultOfTaggedAsInvocation.rest
    import resultOfTaggedAsInvocation.{tags => tagsList}

    /**
     * Supports the registration of tagged tests in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "A Stack" must "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
     *                                                                           ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: => Unit) {
      registerTestToRun(verb + " " + rest, "in", tagsList, testFun _)
    }

    /**
     * Supports the registration of tagged, ignored tests in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "A Stack" must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { ... }
     *                                                                           ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * For examples of tagged test registration, see the <a href="FlatSpec.html#TaggingTests">Tagging tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: => Unit) {
      registerTestToIgnore(verb + " " + rest, tagsList, "ignore", testFun _)
    }
  }

  /**
   * Implicitly converts an object of type <code>ResultOfTaggedAsInvocation</code> to an
   * <code>InAndIgnoreMethodsAfterTaggedAs</code>, to enable <code>in</code> and <code>ignore</code>
   * methods to be invokable on that object.
   */
  protected implicit def convertToInAndIgnoreMethodsAfterTaggedAs(resultOfTaggedAsInvocation: ResultOfTaggedAsInvocation) =
    new InAndIgnoreMethodsAfterTaggedAs(resultOfTaggedAsInvocation)

  /**
   * Supports the shorthand form of test registration.
   *
   * <p>
   * For example, this method enables syntax such as the following:
   * </p>
   *
   * <pre class="stHighlight">
   * "A Stack (when empty)" should "be empty" in { ... }
   *                        ^
   * </pre>
   *
   * <p>
   * This function is passed as an implicit parameter to a <code>should</code> method
   * provided in <code>ShouldVerb</code>, a <code>must</code> method
   * provided in <code>MustVerb</code>, and a <code>can</code> method
   * provided in <code>CanVerb</code>. When invoked, this function registers the
   * subject description (the first parameter to the function) and returns a <code>ResultOfStringPassedToVerb</code>
   * initialized with the verb and rest parameters (the second and third parameters to
   * the function, respectively).
   * </p>
   */
  protected implicit val shorthandTestRegistrationFunction: (String, String, String) => ResultOfStringPassedToVerb = {
    (subject, verb, rest) => {
      behavior.of(subject)
      new ResultOfStringPassedToVerb(verb, rest) {

        def is(testFun: => PendingNothing) {
          registerTestToRun(verb + " " + rest, "is", List(), testFun _)
        }
        // Note, won't have an is method that takes fixture => PendingNothing one, because don't want
        // to say is (fixture => pending), rather just say is (pending)
        def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
          val tagList = firstTestTag :: otherTestTags.toList
          new ResultOfTaggedAsInvocation(verb, rest, tagList) {
            // "A Stack" should "bla bla" taggedAs(SlowTest) is (pending)
            //                                               ^
            def is(testFun: => PendingNothing) {
              registerTestToRun(verb + " " + rest, "is", tags, testFun _)
            }
          }
        }
      }
    }
  }

  /**
   * Supports the shorthand form of shared test registration.
   *
   * <p>
   * For example, this method enables syntax such as the following in:
   * </p>
   *
   * <pre class="stHighlight">
   * "A Stack (with one item)" should behave like nonEmptyStack(stackWithOneItem, lastValuePushed)
   *                           ^
   * </pre>
   *
   * <p>
   * This function is passed as an implicit parameter to a <code>should</code> method
   * provided in <code>ShouldVerb</code>, a <code>must</code> method
   * provided in <code>MustVerb</code>, and a <code>can</code> method
   * provided in <code>CanVerb</code>. When invoked, this function registers the
   * subject description (the  parameter to the function) and returns a <code>BehaveWord</code>.
   * </p>
   */
  protected implicit val shorthandSharedTestRegistrationFunction: (String) => BehaveWord = {
    (left) => {
      behavior.of(left)
      new BehaveWord
    }
  }

// TODO: I got a: 
// runsuite:
// [scalatest] *** RUN ABORTED ***
// [scalatest]   An exception or error caused a run to abort: Duplicate test name: should return the new exception with the clue string appended, separated by a space char if passed a function that does that (Engine.scala:464)
// Shouldn't be Engine.scala clearly
  /**
   * Register a test to ignore, which has the given spec text, optional tags, and test function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>execute</code>
   * methods. This method exists to make it easy to ignore an existing test by changing the call to <code>it</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be executed, but a
   * report will be sent that indicates the test was ignored. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>FlatSpec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testTags the optional list of tags for this test
   * @param methodName caller's method name
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullPointerException if <code>specText</code> or any passed test tag is <code>null</code>
   */
  private def registerTestToIgnore(specText: String, testTags: List[Tag], methodName: String, testFun: () => Unit) {
    registerIgnoredTest(specText, testFun, "ignoreCannotAppearInsideAnIt", "FlatSpec.scala", methodName, 4, -3, testTags: _*)
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> tag names to which tests in this <code>FlatSpec</code> belong, and values
   * the <code>Set</code> of test names that belong to each tag. If this <code>FlatSpec</code> contains no tags, this method returns an empty <code>Map</code>.
   *
   * <p>
   * This trait's implementation returns tags that were passed as strings contained in <code>Tag</code> objects passed to 
   * methods <code>test</code> and <code>ignore</code>. 
   * </p>
   */
  override def tags: Map[String, Set[String]] = atomic.get.tagsMap

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
      val theConfigMap = args.configMap
      withFixture(
        new NoArgTest {
          def name = testName
          def apply() { theTest.testFun() }
          def configMap = theConfigMap
        }
      )
    }

    runTestImpl(thisSuite, testName, args, true, invokeWithFixture)
  }

  /**
   * Run zero to many of this <code>FlatSpec</code>'s tests.
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
   *                 I.e., <code>None</code> acts like a wildcard that means execute all relevant tests in this <code>FlatSpec</code>.
   * @param args the <code>Args</code> for this run
   *
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, <code>tagsToInclude</code>,
   *     <code>tagsToExclude</code>, or <code>configMap</code> is <code>null</code>.
   */
  protected override def runTests(testName: Option[String], args: Args) {
    runTestsImpl(thisSuite, testName, args, info, true, runTest)
  }

  /**
   * An immutable <code>Set</code> of test names. If this <code>FlatSpec</code> contains no tests, this method returns an
   * empty <code>Set</code>.
   *
   * <p>
   * This trait's implementation of this method will return a set that contains the names of all registered tests. The set's
   * iterator will return those names in the order in which the tests were registered. Each test's name is composed
   * of the concatenation of the text of each surrounding describer, in order from outside in, and the text of the
   * example itself, with all components separated by a space. For example, consider this <code>FlatSpec</code>:
   * </p>
   *
   * <pre class="stHighlight">
   * import org.scalatest.FlatSpec
   *
   * class StackSpec extends FlatSpec {
   *
   *   "A Stack (when not empty)" must "allow me to pop" in {}
   *   it must "not be empty" in {}
   *
   *   "A Stack (when not full)" must "allow me to push" in {}
   *   it must "not be full" in {}
   * }
   * </pre>
   *
   * <p>
   * Invoking <code>testNames</code> on this <code>FlatSpec</code> will yield a set that contains the following
   * two test name strings:
   * </p>
   *
   * <pre>
   * "A Stack (when not empty) must allow me to pop"
   * "A Stack (when not empty) must not be empty"
   * "A Stack (when not full) must allow me to push"
   * "A Stack (when not full) must not be full"
   * </pre>
   */
  override def testNames: Set[String] = {
    // I'm returning a ListSet here so that they tests will be run in registration order
    ListSet(atomic.get.testNamesList.toArray: _*)
  }

  override def run(testName: Option[String], args: Args) {

    runImpl(thisSuite, testName, args, super.run)
  }

  /**
   * Supports shared test registration in <code>FlatSpec</code>s.
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
   * For more information and examples of the use of <code>behave</code>, see the <a href="#SharedTests">Shared tests section</a>
   * in the main documentation for this trait.
   * </p>
   */
  protected val behave = new BehaveWord
  
  /**
   * Suite style name.
   */
  final override val styleName: String = "org.scalatest.FlatSpec"
}

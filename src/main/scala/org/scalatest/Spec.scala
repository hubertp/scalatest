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

import scala.collection.immutable.ListSet
import Suite.autoTagClassAnnotations
import Spec.isTestMethod
import Suite.takesInformer
import Suite.InformerInParens
import Suite.testMethodTakesAnInformer
import java.lang.reflect.Method
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier

trait Spec extends Suite { thisSuite =>

  private final val engine = new Engine("concurrentSpecMod", "Spec")
  import engine._
  
  private def discoveryAndRegisterTests() {
    
    def getTestTags(testName: String) =
      for {
        a <- getMethodForTestName(testName).getDeclaredAnnotations
        annotationClass = a.annotationType
        if annotationClass.isAnnotationPresent(classOf[TagAnnotation])
      } yield annotationClass.getName
      
    def testMethodTakesARep(method: Method): Boolean = {
      val paramTypes = method.getParameterTypes
      (paramTypes.size == 1) && (paramTypes(0) eq classOf[Rep])
    }
 
    class RepImpl(val info: Informer, val markup: Documenter) extends Rep

    object MethodNameEncodedOrdering extends Ordering[Method] {
      import scala.reflect.NameTransformer.decode
      def compare(x: Method, y: Method): Int = {
        decode(x.getName) compareTo decode(y.getName)
      }
    }
 
    val testTags = tags
    val testMethods = getClass.getMethods.filter(isTestMethod(_)).sorted(MethodNameEncodedOrdering)

    testMethods.foreach { m =>
      val testName = if (takesInformer(m)) m.getName + InformerInParens else m.getName
      val methodTags = getTestTags(testName)
      val testFun: () => Unit = () => { 
        val argsArray: Array[Object] =
          if (testMethodTakesAnInformer(testName)) 
            Array(info)  
          else if (testMethodTakesARep(m)) 
            Array(new RepImpl(info, markup))
          else 
            Array.empty
        try {
          m.invoke(thisSuite, argsArray: _*)
        }
        catch {
          case ite: InvocationTargetException => 
            throw ite.getTargetException
        }
      }
      //val (filterTest, ignoreTest) = filter(tn, tags, suiteId)
      val isIgnore = testTags.get(testName) match {
        case Some(tagSet) => tagSet.contains(Suite.IgnoreAnnotation) || methodTags.contains(Suite.IgnoreAnnotation)
        case None => methodTags.contains(Suite.IgnoreAnnotation)
      }
      if (isIgnore)
        registerIgnoredTest(testName, testFun, "registrationAlreadyClosed", sourceFileName, "discoveryAndRegisterTests", 3, 0, methodTags.map(new Tag(_)): _*)
      else
        registerTest(testName, testFun, "registrationAlreadyClosed", sourceFileName, "discoveryAndRegisterTests", 2, 0, None, None, methodTags.map(new Tag(_)): _*)
    }
  }

  discoveryAndRegisterTests()

  // TODO: Probably make this private final val sourceFileName in a singleton object so it gets compiled in rather than carried around in each instance
  private[scalatest] val sourceFileName = "Spec.scala"

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
   * Returns a <code>Documenter</code> that during test execution will forward strings passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked while this
   * <code>Spec</code> is being executed, such as from inside a test function, it will forward the information to
   * the current reporter immediately. If invoked at any other time, it will
   * throw an exception. This method can be called safely by any thread.
   */
  implicit protected def markup: Documenter = atomicDocumenter.get
  
  /**
   * An immutable <code>Set</code> of test names. If this <code>FunSpec</code> contains no tests, this method returns an
   * empty <code>Set</code>.
   *
   * <p>
   * This trait's implementation of this method will return a set that contains the names of all registered tests. The set's
   * iterator will return those names in the order in which the tests were registered. Each test's name is composed
   * of the concatenation of the text of each surrounding describer, in order from outside in, and the text of the
   * example itself, with all components separated by a space. For example, consider this <code>FunSpec</code>:
   * </p>
   *
   * <pre class="stHighlight">
   * import org.scalatest.FunSpec
   *
   * class StackSpec extends FunSpec {
   *   describe("A Stack") {
   *     describe("(when not empty)") {
   *       it("must allow me to pop") {}
   *     }
   *     describe("(when not full)") {
   *       it("must allow me to push") {}
   *     }
   *   }
   * }
   * </pre>
   *
   * <p>
   * Invoking <code>testNames</code> on this <code>FunSpec</code> will yield a set that contains the following
   * two test name strings:
   * </p>
   *
   * <pre class="stExamples">
   * "A Stack (when not empty) must allow me to pop"
   * "A Stack (when not full) must allow me to push"
   * </pre>
   */
  override def testNames: Set[String] = {
    // I'm returning a ListSet here so that they tests will be run in registration order
    ListSet(atomic.get.testNamesList.toArray: _*)
  }
  
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
   * Run zero to many of this <code>FunSpec</code>'s tests.
   *
   * @param testName an optional name of one test to run. If <code>None</code>, all relevant tests should be run.
   *                 I.e., <code>None</code> acts like a wildcard that means run all relevant tests in this <code>Suite</code>.
   * @param args the <code>Args</code> for this run
   *
   * @throws NullPointerException if any of the passed parameters is <code>null</code>.
   * @throws IllegalArgumentException if <code>testName</code> is defined, but no test with the specified test name
   *     exists in this <code>Suite</code>
   */
  protected override def runTests(testName: Option[String], args: Args) {
    runTestsImpl(thisSuite, testName, args, info, true, runTest)
  }

  override def run(testName: Option[String], args: Args) {
    runImpl(thisSuite, testName, args, super.run)
  }
  
  /**
   * Suite style name.
   */
  final override val styleName: String = "org.scalatest.Spec"
}

private[scalatest] object Spec {

  def isTestMethod(m: Method): Boolean = {

    val isInstanceMethod = !Modifier.isStatic(m.getModifiers())

    val hasNoParams = m.getParameterTypes.isEmpty

    // name must have at least one encoded space: "$u0220"
    val includesEncodedSpace = m.getName.indexOf("$u0020") >= 0

    // def maybe(b: Boolean) = if (b) "" else "!"
    // println("m.getName: " + m.getName + ": " + maybe(isInstanceMethod) + "isInstanceMethod, " + maybe(hasNoParams) + "hasNoParams, " + maybe(includesEncodedSpace) + "includesEncodedSpace")
    isInstanceMethod && hasNoParams && includesEncodedSpace
  }
}


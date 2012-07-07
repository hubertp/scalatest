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

import events.Event
import org.scalatest.time.Span
import tools.{DistributedTestRunnerSuite, TestSortingReporter, Runner}

/**
 * Trait that causes that the tests of any suite it is mixed into to be run in parallel if
 * a <code>Distributor</code> is passed to <code>runTests</code>.
 *
 * <p>
 * ScalaTest's normal approach for running suites of tests in parallel is to run different suites in parallel,
 * but the tests of any one suite sequentially. This approach should provide sufficient distribution of the work load
 * in most cases, but some suites may encapsulate multiple long-running tests. Such suites may dominate the execution
 * time of the run. If so, mixing in this trait into just those suites will allow their long-running tests to run in parallel with each
 * other, thereby helping to reduce the total time required to run an entire run.
 * </p>
 *
 * <p>
 * To make it easier for users to write tests that run in parallel, this trait runs each test in its own instance of the class.
 * Running each test in its own instance enables tests to use the same instance <code>vars</code> and mutable objects referenced from
 * instance variables without needing to synchronize. Although ScalaTest provides functional approaches to
 * factoring out common test code that can help avoid such issues, running each test in its own instance is an insurance policy that makes 
 * running tests in parallel easier and less error prone.
 * </p>
 *
 * <p>
 * For the details on how <code>ParallelTestExecution</code> works, see the documentation for methods <code>run</code>, <code>runTests</code>, and <code>runTest</code>,
 * which this trait overrides.
 * </p>
 * 
 * <p>
 * FIX: If no <code>Distributor</code> is passed to <code>runTests</code>, 
 * this trait's implementation simply invokes its supertrait <code>OneInstancePerTest</code>'s implementation
 * of <code>runTests</code>, which will run each test in its own instance sequentially. If a <code>Distributor</code>
 * is passed, however, this traits' implementation of <code>runTests</code> will, for each test, wrap a new instance of the
 * suite in a special <em>wrapper suite</em> that will invoke just that one test, and passes the wrapper suites to the <code>Distributor</code>.
 * The thread or entity that takes a wrapper suite from the <code>Distributor</code> will invoke <code>run</code>
 * on the wrapper suite, which will run just one test. In this way, different tests of a suite that mixes in
 * <code>ParallelTestExecution</code> will run in parallel.
 * </p>
 *
 * @author Bill Venners
 */
trait ParallelTestExecution extends OneInstancePerTest { this: Suite =>

  /**
   * Modifies the behavior of <code>super.runTests</code> to facilitate parallel test execution.
   *
   * <p>
   * This trait's implementation of this method always invokes <code>super.runTests</code> to delegate
   * to <code>OneInstancePerTest</code>'s implementation, but it may pass in a modified <code>args</code> object.
   * If <code>args.runTestInNewInstance</code> is <code>false</code> and <code>args.distributor</code> is defined,
   * this trait's implementation of this method will wrap the passed <code>args.reporter</code> in a new <code>Reporter</code>
   * that can sort events fired by parallel tests back into sequential order, with a timeout. It will pass this new reporter to
   * <code>super.runTests</code> (in <code>args.reporter</code>) as well as a defined <code>DistributedTestSorter</code>
   * (in args.distributedTestSorter) that can be used to communicate with the sorting reporter. Otherwise, if <code>args.runTestInNewInstance</code> is
   * <code>true</code> or <code>args.distributor</code> is empty, this trait's implementation of this method simply calls <code>super.runTests</code>,
   * passing along the same <code>testName</code> and <code>args</code>.
   * </p>
   *
   * @param testName an optional name of one test to run. If <code>None</code>, all relevant tests should be run.
   *                 I.e., <code>None</code> acts like a wildcard that means run all relevant tests in this <code>Suite</code>.
   * @param args the <code>Args</code> for this run
   */
  protected abstract override def runTests(testName: Option[String], args: Args) {
    val newArgs =
      if (args.runTestInNewInstance)
        args // This is the test-specific instance
      else {
        args.distributor match {  // This is the initial instance
          case Some(distributor) =>
            val testSortingReporter = new TestSortingReporter(suiteId, args.reporter, sortingTimeout, testNames.size, args.distributedSuiteSorter)
            args.copy(reporter = testSortingReporter, distributedTestSorter = Some(testSortingReporter))
          case None =>
            args
        }
      }

    // Always call super.runTests, which is OneInstancePerTest's runTests. But if RTINI is NOT
    // set, that means we are in the initial instance. In that case, we wrap the reporter in
    // a new TestSortingReporter, and wrap the distributor in a new DistributorWrapper that
    // knows is passed the TestSortingReporter. We then call super.runTests, which is OIPT's runTests.
    super.runTests(testName, newArgs)
  }

  /**
   * Modifies the behavior of <code>super.runTest</code> to facilitate parallel test execution.
   *
   * <p>
   * This trait's implementation of this method only changes the supertrait implementation if
   * <code>args.distributor</code> is defined. If <code>args.distributor</code> is empty, it
   * simply invokes <code>super.runTests</code>, passing along the same <code>testName</code>
   * and <code>args</code> object.
   * </p>
   *
   * <p>
   * If <code>args.distributor</code> is defined, 
   * </p>
   *
   * <p>
   * TODO: Discuss...  Note this is final because don't want things like before and after to
   * be executed by the wrong thread, and therefore, at the wrong time. With OIPT, if OIPT is
   * super to BAA, then still things will happen in the expected order, because all is sequential.
   * </p>
   *
   * @param testName the name of one test to execute.
   * @param args the <code>Args</code> for this run
   */
  final protected abstract override def runTest(testName: String, args: Args) {

    args.distributor match {
      case Some(distribute) =>
        if (args.runTestInNewInstance) {
          // Tell the TSR that the test is being distributed
          for (sorter <- args.distributedTestSorter)
            sorter.distributingTest(testName)

          // It will be oneInstance, testName, args.copy(reporter = ...)
          distribute(new DistributedTestRunnerSuite(newInstance, testName, args), args.copy(tracker = args.tracker.nextTracker))
        }
        else {
          // In test-specific (distributed) instance, so just run the test. (RTINI was
          // removed by OIPT's implementation of runTests.)
          try {
            super.runTest(testName, args)
          }
          finally {
            // Tell the TSR that the distributed test has completed
            for (sorter <- args.distributedTestSorter)
              sorter.completedTest(testName)
          }
        }
      case None => super.runTest(testName, args)
    }
  }

  /**
   * Construct a new instance of this <code>Suite</code>.
   *
   * <p>
   * This trait's implementation of <code>runTests</code> invokes this method to create
   * a new instance of this <code>Suite</code> for each test. This trait's implementation
   * of this method uses reflection to call <code>this.getClass.newInstance</code>. This
   * approach will succeed only if this <code>Suite</code>'s class has a public, no-arg
   * constructor. In most cases this is likely to be true, because to be instantiated
   * by ScalaTest's <code>Runner</code> a <code>Suite</code> needs a public, no-arg
   * constructor. However, this will not be true of any <code>Suite</code> defined as
   * an inner class of another class or trait, because every constructor of an inner
   * class type takes a reference to the enclosing instance. In such cases, and in
   * cases where a <code>Suite</code> class is explicitly defined without a public,
   * no-arg constructor, you will need to override this method to construct a new
   * instance of the <code>Suite</code> in some other way.
   * </p>
   *
   * <p>
   * Here's an example of how you could override <code>newInstance</code> to construct
   * a new instance of an inner class:
   * </p>
   *
   * <pre class="stHighlight">
   * import org.scalatest.Suite
   *
   * class Outer {
   *   class InnerSuite extends Suite with ParallelTestExecution {
   *     def testOne() {}
   *     def testTwo() {}
   *     override def newInstance = new InnerSuite
   *   }
   * }
   * </pre>
   */
  override def newInstance: Suite with ParallelTestExecution = {
    val instance = getClass.newInstance.asInstanceOf[Suite with ParallelTestExecution]
    instance
  }

  /**
   * A maximum amount of time to wait for out-of-order events generated by running the tests
   * of this <code>Suite</code> in parallel while sorting the events back into a more
   * user-friendly, sequential order.
   *
   * <p>
   * The default implementation of this method returns the value specified via <code>-T</code> to
   * <a href="tools/Runner$.html"></code>Runner</code></a>, or 2 seconds, if no <code>-T</code> was given.
   * </p>
   *
   * @return a maximum amount of time to wait for events while resorting them into sequential order
   */
  protected def sortingTimeout: Span = Runner.testSortingReporterTimeout
  
  abstract override def run(testName: Option[String], args: Args) {
    val newArgs = testName match {
      case Some(testName) => 
        args.distributedTestSorter match {
          case Some(testSorter) => 
            class TestSpecificReporter(testSorter: DistributedTestSorter, testName: String) extends Reporter {
              def apply(event: Event) {
                testSorter.apply(testName, event)
              }
            }
            args.copy(reporter = new TestSpecificReporter(testSorter, testName))
          case None =>
            args
        }
      case None =>
        args
    }
    super.run(testName, newArgs)
  }
}

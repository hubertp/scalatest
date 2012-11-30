/*
 * Copyright 2001-20012 Artima, Inc.
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
package org.scalautils

/*
trait Tolerance {

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final case class DoubleTolerance(right: Double, tolerance: Double)

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final class DoublePlusOrMinusWrapper(right: Double) {

    /**
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * sevenDotOh should be (17.0 +- 0.2)
     *                            ^
     * </pre>
     */
    def +-(tolerance: Double): DoubleTolerance = {
      if (tolerance <= 0.0)
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      DoubleTolerance(right, tolerance)
    }

    /**
     * <strong>The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.</strong>
     *
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * sevenDotOh should be (17.0 plusOrMinus 0.2)
     *                            ^
     * </pre>
     */
    @deprecated("The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.")
    def plusOrMinus(tolerance: Double): DoubleTolerance = {
      if (tolerance <= 0.0)
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      DoubleTolerance(right, tolerance)
    }
  }

  /**
   * Implicitly converts an object of type <code>Double</code> to a <code>DoublePlusOrMinusWrapper</code>,
   * to enable a <code>+-</code> method to be invokable on that object.
   */
  implicit def convertDoubleToPlusOrMinusWrapper(right: Double): DoublePlusOrMinusWrapper = new DoublePlusOrMinusWrapper(right)

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final case class FloatTolerance(right: Float, tolerance: Float)

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final class FloatPlusOrMinusWrapper(right: Float) {

    /**
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * sevenDotOh should be (17.0f +- 0.2f)
     *                             ^
     * </pre>
     */
    def +-(tolerance: Float): FloatTolerance = {
      if (tolerance <= 0.0f)
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      FloatTolerance(right, tolerance)
    }

    /**
     * <strong>The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.</strong>
     *
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * sevenDotOh should be (17.0f plusOrMinus 0.2f)
     *                             ^
     * </pre>
     */
    @deprecated("The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.")
    def plusOrMinus(tolerance: Float): FloatTolerance = {
      if (tolerance <= 0.0f)
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      FloatTolerance(right, tolerance)
    }
  }

  /**
   * Implicitly converts an object of type <code>Float</code> to a <code>FloatPlusOrMinusWrapper</code>,
   * to enable a <code>+-</code> method to be invokable on that object.
   */
  implicit def convertFloatToPlusOrMinusWrapper(right: Float): FloatPlusOrMinusWrapper = new FloatPlusOrMinusWrapper(right)

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final case class LongTolerance(right: Long, tolerance: Long)

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final class LongPlusOrMinusWrapper(right: Long) {

    /**
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * seven should be (17L +- 2)
     *                      ^
     * </pre>
     */
    def +-(tolerance: Long): LongTolerance = {
      if (tolerance <= 0L)
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      LongTolerance(right, tolerance)
    }

    /**
     * <strong>The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.</strong>
     *
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * seven should be (17L plusOrMinus 2)
     *                      ^
     * </pre>
     */
    @deprecated("The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.")
    def plusOrMinus(tolerance: Long): LongTolerance = {
      if (tolerance <= 0L)
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      LongTolerance(right, tolerance)
    }
  }

  /**
   * Implicitly converts an object of type <code>Long</code> to a <code>LongPlusOrMinusWrapper</code>,
   * to enable a <code>+-</code> method to be invokable on that object.
   */
  implicit def convertLongToPlusOrMinusWrapper(right: Long): LongPlusOrMinusWrapper = new LongPlusOrMinusWrapper(right)

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final case class IntTolerance(right: Int, tolerance: Int)

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final class IntPlusOrMinusWrapper(right: Int) {

    /**
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * seven should be (17 +- 2)
     *                     ^
     * </pre>
     */
    def +-(tolerance: Int): IntTolerance = {
      if (tolerance <= 0)
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      IntTolerance(right, tolerance)
    }

    /**
     * <strong>The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.</strong>
     *
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * seven should be (17 plusOrMinus 2)
     *                     ^
     * </pre>
     */
    @deprecated("The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.")
    def plusOrMinus(tolerance: Int): IntTolerance = {
      if (tolerance <= 0)
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      IntTolerance(right, tolerance)
    }
  }

  /**
   * Implicitly converts an object of type <code>Int</code> to a <code>IntPlusOrMinusWrapper</code>,
   * to enable a <code>+-</code> method to be invokable on that object.
   */
  implicit def convertIntToPlusOrMinusWrapper(right: Int): IntPlusOrMinusWrapper = new IntPlusOrMinusWrapper(right)

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final case class ShortTolerance(right: Short, tolerance: Short)

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final class ShortPlusOrMinusWrapper(right: Short) {

    /**
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * seven should be (17.toShort +- 2.toShort)
     *                             ^
     * </pre>
     */
    def +-(tolerance: Short): ShortTolerance = {
      if (tolerance <= 0)
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      ShortTolerance(right, tolerance)
    }

    /**
     * <strong>The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.</strong>
     *
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * seven should be (17.toShort plusOrMinus 2.toShort)
     *                             ^
     * </pre>
     */
    @deprecated("The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.")
    def plusOrMinus(tolerance: Short): ShortTolerance = {
      if (tolerance <= 0)
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      ShortTolerance(right, tolerance)
    }
  }

  /**
   * Implicitly converts an object of type <code>Short</code> to a <code>ShortPlusOrMinusWrapper</code>,
   * to enable a <code>+-</code> method to be invokable on that object.
   */
  implicit def convertShortToPlusOrMinusWrapper(right: Short): ShortPlusOrMinusWrapper = new ShortPlusOrMinusWrapper(right)

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final case class ByteTolerance(right: Byte, tolerance: Byte)

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final class BytePlusOrMinusWrapper(right: Byte) {

    /**
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * seven should be (17.toByte +- 2.toByte)
     *                            ^
     * </pre>
     */
    def +-(tolerance: Byte): ByteTolerance = {
      if (tolerance <= 0)
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      ByteTolerance(right, tolerance)
    }

    /**
     * <strong>The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.</strong>
     *
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * seven should be (17.toByte plusOrMinus 2.toByte)
     *                            ^
     * </pre>
     */
    @deprecated("The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.")
    def plusOrMinus(tolerance: Byte): ByteTolerance = {
      if (tolerance <= 0)
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      ByteTolerance(right, tolerance)
    }
  }

  /**
   * Implicitly converts an object of type <code>Byte</code> to a <code>BytePlusOrMinusWrapper</code>,
   * to enable a <code>+-</code> method to be invokable on that object.
   */
  implicit def convertByteToPlusOrMinusWrapper(right: Byte): BytePlusOrMinusWrapper = new BytePlusOrMinusWrapper(right)
}
*/

/*
Could I have one implicit conversion that converts any T to a PlusOrMinusWrapper[T], which has a +- method that takes T (and therefore
anything implicitly convertible to T) and returns a Tolerance[T] so long as there's an implicit ToleranceGen[T] in scope? And Tolerance[T] has a method
that takes a T and says yes or no.

So just one extra implicit. Because I'd need to have one for Double, Float, Long, Int, Byte, Short. Could, but I think it is premature generalization
at this point.

Well I should be able to do one implicit conversion on something for which a Numeric[T] is available. Then can return a Tolerance[T].
(1.0 +- 0.1) gives me a Tolerance[Double]. Then === can have one overloaded form that takes a Tolerance[L]. Or in this case, anything
implicitly convertible to L. So a Tolerance[Short] would work if L is an Int.  So it would take a right[M]: Tolerance[M <% L] (view bounds
may go away, so just) right[M]

def ===[R](Tolerance[R])(implicit ev: R => L)
def !==[R](Tolerance[R])(implicit ev: R => L)

Question is, will they overload. This gets it down to 1 darned implicit in scope I think. I can then remove the 6 from ScalaTest matchers. And mix
in the Tolerance from scalautils into ClassicMatchers.
*/

/**
 * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
 * the matchers DSL.
 *
 * @author Bill Venners
 */
final case class Interval[T : Numeric](right: T, tolerance: T) {
  private val numeric = implicitly[Numeric[T]]
  private val max = numeric.plus(right, tolerance)
  private val min = numeric.minus(right, tolerance)
  def isWithin(n: T): Boolean = {
    numeric.gteq(n, min) && numeric.lteq(n, max)
  }
}

trait Tolerance {

  /**
   * This class is part of the ScalaUtils tolerance DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  final class PlusOrMinusWrapper[T: Numeric](right: T) {

    /**
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * sevenDotOh should be (17.0 +- 0.2)
     *                            ^
     * </pre>
     */
    def +-(tolerance: T): Interval[T] = {
      val numeric = implicitly[Numeric[T]]
      if (numeric.lteq(tolerance, numeric.zero))
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      Interval(right, tolerance)
    }

    /**
     * <strong>The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.</strong>
     *
     * This method enables the following syntax:
     *
     * <pre class="stHighlight">
     * sevenDotOh should be (17.0 plusOrMinus 0.2)
     *                            ^
     * </pre>
     */
    @deprecated("The plusOrMinus method has been deprecated and will be removed in a future version of ScalaTest. Please use +- instead.")
    def plusOrMinus(tolerance: T): Interval[T] = {
      val numeric = implicitly[Numeric[T]]
      if (numeric.lteq(tolerance, numeric.zero))
        throw new IllegalArgumentException(tolerance.toString + " passed to +- was zero or negative. Must be a positive non-zero number.")
        // throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      Interval(right, tolerance)
    }
  }

  /**
   * Implicitly converts an object of type <code>Byte</code> to a <code>BytePlusOrMinusWrapper</code>,
   * to enable a <code>+-</code> method to be invokable on that object.
   */
  implicit def convertNumericToPlusOrMinusWrapper[T : Numeric](right: T): PlusOrMinusWrapper[T] = new PlusOrMinusWrapper(right)
}

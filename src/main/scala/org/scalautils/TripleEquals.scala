/*
 * Copyright 2001-2012 Artima, Inc.
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

// Gives you unconstrained equality
trait TripleEquals extends AnyEquality with EqualityConstraints {

  override def convertToEqualizer(left: Any) = new Equalizer(left)

  implicit override def unconstrainedEquality[A, B](implicit equalityOfA: Equality[A]): EqualityConstraint[A, B] = new BasicEqualityConstraint[A, B](equalityOfA)

  override def lowPriorityTypeCheckedEqualityConstraint[A, B](implicit equalityOfA: Equality[A], ev: A <:< B): EqualityConstraint[A, B] = new BasicEqualityConstraint[A, B](equalityOfA)
  override def typeCheckedEqualityConstraint[A, B](implicit equalityOfA: Equality[A], ev: B <:< A): EqualityConstraint[A, B] = new BasicEqualityConstraint[A, B](equalityOfA)

  override def lowPriorityConversionCheckedEqualityConstraint[A, B](implicit equalityOfB: Equality[B], cnv: A => B): EqualityConstraint[A, B] = new AToBEqualityConstraint[A, B](equalityOfB, cnv)
  override def conversionCheckedEqualityConstraint[A, B](implicit equalityOfA: Equality[A], cnv: B => A): EqualityConstraint[A, B] = new BToAEqualityConstraint[A, B](equalityOfA, cnv)

  class AnyEqualizer[L](left: L) {
    def ===[R](right: R)(implicit constraint: EqualityConstraint[L, R]): Boolean = constraint.areEqual(left, right)
    def !==[R](right: R)(implicit constraint: EqualityConstraint[L, R]): Boolean = !constraint.areEqual(left, right)
    def ===(interval: Interval[L]): Boolean = if (interval != null) interval.isWithin(left) else false // left == interval
    def !==(interval: Interval[L]): Boolean = if (interval != null) !interval.isWithin(left) else true // left != interval
  }

  implicit def convertToAnyEqualizer[T](o: T): AnyEqualizer[T] = new AnyEqualizer(o)
}

object TripleEquals extends TripleEquals


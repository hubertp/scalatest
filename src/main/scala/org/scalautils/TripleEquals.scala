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
package org.scalautils

trait TripleEquals extends AnyEquality {

  class AnyEqualizer[L](left: L) {
    def ===[R](right: R)(implicit equality: Equality[L], constraint: EqualityConstraint[L, R]): Boolean = equality.areEqual(left, right)
    def !==[R](right: R)(implicit equality: Equality[L], constraint: EqualityConstraint[L, R]): Boolean = !equality.areEqual(left, right)
    def ===(interval: Interval[L]): Boolean = interval.isWithin(left)
    def !==(interval: Interval[L]): Boolean = !interval.isWithin(left)
  }

  implicit def convertToAnyEqualizer[T](o: T): AnyEqualizer[T] = new AnyEqualizer(o)
}

object TripleEquals extends TripleEquals


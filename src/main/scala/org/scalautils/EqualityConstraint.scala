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

abstract class EqualityConstraint[A, B] {
  def areEqual(left: A, right: B): Boolean
}

class BasicEqualityConstraint[A, B](equalityOfA: Equality[A]) extends EqualityConstraint[A, B] {
  def areEqual(left: A, right: B): Boolean = equalityOfA.areEqual(left, right)
}

class BToAEqualityConstraint[A, B](equalityOfA: Equality[A], cnv: B => A) extends EqualityConstraint[A, B] {
  override def areEqual(left: A, right: B): Boolean = equalityOfA.areEqual(left, cnv(right))
}

class AToBEqualityConstraint[A, B](equalityOfB: Equality[B], cnv: A => B) extends EqualityConstraint[A, B] {
  override def areEqual(left: A, right: B): Boolean = equalityOfB.areEqual(cnv(left), right)
}

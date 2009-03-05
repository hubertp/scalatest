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

package org.scalatest.junit.helpers

import org.junit.runner.RunWith

// @RunWith(classOf[JUnitRunner])
class EasySuite extends FunSuite with JUnitRunnable {

// TODO put method on here that says how many tests, and how many should fail 
  test("JUnit ran this OK!") {
    assert(1 === 1)
  }

  test("JUnit ran this OK!, but it had a failure we hope") {
    assert(1 === 2)
  }

  test("bla bla bla") {
    assert(1 === 2)
  }
}
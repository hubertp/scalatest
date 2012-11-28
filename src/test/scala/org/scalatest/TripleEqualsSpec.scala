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

import scala.collection.GenSeq
import scala.collection.GenMap
import scala.collection.GenSet
import scala.collection.GenIterable
import scala.collection.GenTraversable
import scala.collection.GenTraversableOnce

class TripleEqualsSpec extends Spec with NonImplicitAssertions {

  object `the custom equality === operator` {

    object `with UncheckedEquality` {

      def `should compare anything with anything` {

        object O extends TripleEquals with UncheckedEquality
        import O._

        assert(1 === 1)
        assert(!(1 !== 1))

        assert(1 === 1L)
        assert(!(1 !== 1L))

        assert(1L === 1)
        assert(!(1L !== 1))

        assert("1" !== 1)
        assert(!("1" === 1))

        assert(1 !== "1")
        assert(!(1 === "1"))
      }
    }

    object `with TypeCheckedEquality` {

      def `should compare supertypes with subtypes on either side` {

        object O extends TripleEquals with TypeCheckedEquality
        import O._

        class Fruit { override def equals(o: Any) = o.isInstanceOf[Fruit] }
        trait Crunchy
        class Apple extends Fruit with Crunchy

        val fr: Fruit = new Apple
        val cr: Crunchy = new Apple
        val ap: Apple = new Apple

        assert(1 === 1)
        assert(!(1 !== 1))

        assert(ap === fr)
        assert(fr === ap)
        assert(ap === cr)
        assert(cr === ap)

        // The rest should not compile
        // assert(1 === 1L)
        // assert(1L === 1)
        // assert(1 !== 1L)
        // assert(1L !== 1)

        // assert("1" === 1)
        // assert(1 === "1")
        // assert("1" !== 1)
        // assert(1 !== "1")

        // assert(fr === cr)
        // assert(cr === fr)
      }
    }

    object `with ConversionCheckedEquality` {

      def `should compare supertypes with subtypes on either side as well as types with implicit conversions in either direction` {

        object O extends TripleEquals with ConversionCheckedEquality
        import O._

        class Fruit { override def equals(o: Any) = o.isInstanceOf[Fruit] }
        trait Crunchy
        class Apple extends Fruit with Crunchy

        val fr: Fruit = new Apple
        val cr: Crunchy = new Apple
        val ap: Apple = new Apple

        assert(1 === 1)
        assert(!(1 !== 1))

        assert(ap === fr)
        assert(fr === ap)
        assert(ap === cr)
        assert(cr === ap)

        // These should work with implicit conversions
        assert(1 === 1L)
        assert(1L === 1)
        assert(1 !== 1L)
        assert(1L !== 1)

        // The rest should not compile
        // assert("1" === 1)
        // assert(1 === "1")
        // assert("1" !== 1)
        // assert(1 !== "1")

        // assert(fr === cr)
        // assert(cr === fr)
      }
    }
  }
}


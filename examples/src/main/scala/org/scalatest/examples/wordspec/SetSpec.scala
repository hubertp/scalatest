package org.scalatest.examples.wordspec

import org.scalatest.WordSpec

class SetSpec extends WordSpec {

  "An empty Set" should {
    "have size 0" in {
      assert(Set.empty.size === 0)
    }
    
    "produce NoSuchElementException when head is invoked" in {
      intercept[NoSuchElementException] {
        Set.empty.head
      }
    }
  }
}
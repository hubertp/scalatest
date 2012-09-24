package org.scalatest

class StatusSpec extends Spec {

  object `ScalaTestStatefulStatus` {
    def `should by default return false for isCompleted` {
      val status = new ScalaTestStatefulStatus
      assert(!status.isCompleted)
    }
    
    def `should return true for isCompleted after completes() is called` {
      val status = new ScalaTestStatefulStatus
      status.completes()
      assert(status.isCompleted)
    }
    
    def `should return true for succeeds() after completes() is called without fails()` {
      val status = new ScalaTestStatefulStatus
      status.completes()
      assert(status.succeeds)
    }
    
    def `should return false for succeeds() after completes is called after fails()` {
      val status = new ScalaTestStatefulStatus
      status.fails()
      status.completes()
      assert(!status.succeeds)
    }
    
    def `waitUntilCompleted should not block after completes() is called` {
      val status = new ScalaTestStatefulStatus
      status.completes()
      status.waitUntilCompleted()
    }
  }
  
  object `StatefulStatus` {
    def `should by default return false for isCompleted` {
      val status = new StatefulStatus
      assert(!status.isCompleted)
    }
    
    def `should return true for isCompleted after completes() is called` {
      val status = new StatefulStatus
      status.completes()
      assert(status.isCompleted)
    }
    
    def `should return true for succeeds() after completes() is called without fails()` {
      val status = new StatefulStatus
      status.completes()
      assert(status.succeeds)
    }
    
    def `should return false for succeeds() after completes is called after fails()` {
      val status = new StatefulStatus
      status.fails()
      status.completes()
      assert(!status.succeeds)
    }
    
    def `waitUntilCompleted should not block after completes() is called` {
      val status = new StatefulStatus
      status.completes()
      status.waitUntilCompleted()
    }
  }
  
}
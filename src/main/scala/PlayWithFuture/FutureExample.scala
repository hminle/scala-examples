package PlayWithFuture

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Created by hminle on 11/10/2016.
  */
object FutureExample extends App{
  def produceSomething(): String = "Fake result"
  def continueDoingSomethingElse(): Unit = println("Continuse doing something else")
  def startDoingSomething(): Unit = println("Start Doing Something " + Thread.currentThread().getName)
  def doSomeThingwithResult(r: String) = println(r)
  def first[T](f: Future[T], g: Future[T]): Future[T] = {
    val p = Promise[T]
    f onSuccess{
      case x => p trySuccess(x)
    }
    g onSuccess{
      case x => p trySuccess(x)
    }
    p.future
  }
  val p = Promise[String]()
  val f = p.future


  val producer = Future {
    println("Go to producer " + Thread.currentThread().getName)
    val r = produceSomething()
    p success(r)
    continueDoingSomethingElse()
  }

  val consumer = Future {
    println("Go to consumer " + Thread.currentThread().getName)
    startDoingSomething()
    f onSuccess{
      case r => doSomeThingwithResult(r)
    }
  }
  //first(producer, consumer)
  producer onComplete{
    case Success(x) => println(Thread.currentThread().getName + " we finish producer " + x )
    case Failure(x) => println(Thread.currentThread().getName + " we fail producer " + x)
  }
  consumer onComplete{
    case Success(x) => println(Thread.currentThread().getName + " we finish consumer " + x )
    case Failure(x) => println(Thread.currentThread().getName + " we fail consumer " + x)
  }
}

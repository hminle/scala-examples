package actorModel
import scala.concurrent._
import statePattern._
/**
  * Created by hminle on 10/21/2016.
  */
// MessageHandler is implemented by using Actor Model

trait MessageHandler[T] {
  implicit val context =
    ExecutionContext.fromExecutorService(java.util.concurrent.Executors.newFixedThreadPool(1))
  private var state: State.State = State.Waiting
  def handleMessage: T => Unit ={
    if(state == State.Waiting) handleMessageWhenWaiting
    else if(state == State.Running) handleMessageWhenRunning
    else handleMessageWhenTerminating
  }
  def !(m: T) = Future {handleMessage(m)}
  def handleMessageWhenWaiting: T => Unit
  def handleMessageWhenRunning: T => Unit
  def handleMessageWhenTerminating: T => Unit
  def transitionTo(destinationState: State.State): Unit = {
    this.state = destinationState
  }
  def shutdown(): Unit = {
    println("Shutdown execution context")
    context.shutdown()
  }
}

class Component1 extends MessageHandler[Message]{
  def handleMessageWhenRunning = {
    case HelloMessage(hello) => {
      println(Thread.currentThread().getName + hello)
    }
    case GoodByeMessage(goodBye) => {
      println(Thread.currentThread().getName + goodBye)
      transitionTo(State.Terminating)
    }
  }

  def handleMessageWhenWaiting = {
    case m => {
      println(Thread.currentThread().getName + " I am waiting, I am not ready to run 11 " + m)
      transitionTo(State.Running)
    }
  }

  def handleMessageWhenTerminating = {
    case m => {
      println(Thread.currentThread().getName + " I am terminating, I cannot handle any message 11")
      this.shutdown()
    }
  }
  def testThread(): Unit = {
    Future {println(Thread.currentThread().getName + " TEST THREAD ")}
  }

  override def shutdown(): Unit = {
    super.shutdown()
    println("shutdown myself " + this.getClass)
  }

}
class Component2(component1: MessageHandler[Message]) extends MessageHandler[Message]{
  def handleMessageWhenRunning = {
    case HelloMessage(hello) => {
      println(Thread.currentThread().getName + hello)
      component1 ! HelloMessage(" 2 hello 1")
    }
    case GoodByeMessage(goodBye) => {
      println(Thread.currentThread().getName + goodBye)
      component1 ! GoodByeMessage(" 2 goodbye 1")
      transitionTo(State.Terminating)
    }
  }

  def handleMessageWhenWaiting = {
    case m => {
      println(Thread.currentThread().getName + " I am waiting, I am not ready to run 22 "+ m)
      transitionTo(State.Running)
    }
  }

  def handleMessageWhenTerminating = {
    case m => {
      println(Thread.currentThread().getName + " I am terminating, I cannot handle any message 22")
      component1.shutdown()
      this.shutdown()
    }
  }
}
object MessageHandlerExample extends App {
  override def main(args: Array[String]) {
    val a = new Component1
    val b = new Component2(a)
    b ! HelloMessage(" hello World 2")
    b ! HelloMessage(" hello World 2, 2nd")
    b ! GoodByeMessage(" Good bye 2 ")
    println(Thread.currentThread().getName)
    b ! HelloMessage("hello World 2, 3rd")
    a ! HelloMessage("hello World 1, from main")
    //a.shutdown()
    //b.shutdown()
  }

}
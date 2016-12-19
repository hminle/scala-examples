package statePattern

import com.sun.corba.se.spi.transport.CorbaResponseWaitingRoom

/**
  * Created by hminle on 10/20/2016.
  */

trait Message
case class HelloMessage(hello: String) extends Message
case class GoodByeMessage(goodBye: String) extends Message
trait MessageHandler {
  //val messageQueue = List[Message]
  //def handleMessage(message: Message): Unit

}
//trait Waiting extends State


abstract class Context {
  private var state: State.State = State.Waiting
  //def handle(): Unit
  //def setState(state: State.State): Unit
  def handleMessage(message: Message): Unit = {
    if(state == State.Waiting) handleMessageWhenWaiting(message)
    else if(state == State.Running) handleMessageWhenRunning(message)
    else handleMessageWhenTerminating(message)
  }
  def handleMessageWhenWaiting(message: Message): Unit
  def handleMessageWhenRunning(message: Message): Unit
  def handleMessageWhenTerminating(message: Message): Unit
  def transitionTo(destinationState: State.State): Unit = {
    this.state = destinationState
  }
}


class ComponentA extends Context with Runnable{
  //var state = State.Waiting
  var isHelloArrived: Boolean = false
  var isTerminated: Boolean = false
  override def run(): Unit = {
    while(!isTerminated) {
      println("I am alive " + Thread.currentThread.getName())
      println(Thread.currentThread().getName)
    }
  }
  override def handleMessageWhenWaiting(message: Message): Unit = {
    message match {
      case HelloMessage(hello) => {println(Thread.currentThread.getName() + hello);transitionTo(State.Running)}
    }
  }

  override def handleMessageWhenRunning(message: Message): Unit = {
    message match {
      case HelloMessage(hello) => println(Thread.currentThread.getName() + " You are already say hello to me !!!")
      case GoodByeMessage(goodBye) => {println(goodBye + Thread.currentThread.getName()); transitionTo(State.Terminating); isTerminated = true}
    }
  }

  override def handleMessageWhenTerminating(message: Message): Unit = {
    message match {
      case HelloMessage(hello) => println( Thread.currentThread().getName + " I am terminating. This may be the last time you say hello")
      case GoodByeMessage(goodBye) => println(Thread.currentThread.getName() + " Wait Wait, I am terminating from your oder")
    }
  }

}
object State extends Enumeration {
  type State = Value
  val Waiting, Running, Terminating = Value
}

object StateExample extends App {
  override def main(args: Array[String]): Unit = {
    println("Test State Machine")
    println(Thread.currentThread().getName)
    val componentA = new ComponentA
    val hello = HelloMessage("Hello bro")
    val goodByeMessage = GoodByeMessage("Goodbye Bro")
    val threadA = new Thread(componentA)
    threadA.setName("componentA")
    threadA.start()
    //Thread.sleep(1000)
    componentA.handleMessage(hello)
    componentA.handleMessage(hello)
    componentA.handleMessage(goodByeMessage)
    componentA.handleMessage(goodByeMessage)
    //Thread.sleep(10000)
    componentA handleMessage(hello)
    //threadA.join()
  }
}
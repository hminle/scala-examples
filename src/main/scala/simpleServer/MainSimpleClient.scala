package simpleServer

/**
  * Created by hminle on 10/25/2016.
  */
object MainSimpleClient extends App{
  override def main(args: Array[String]) {
    val client1: SimpleClient = new SimpleClient("Client 1 connect to server")
    val threadClient1 = new Thread(client1)
    threadClient1.start()

    Thread.sleep(5000)
    val client2: SimpleClient = new SimpleClient("Client 2 connect to server")
    val threadClient2 = new Thread(client2)
    threadClient2.start()

    Thread.sleep(5000)
    //threadClient1.interrupt()
    //threadClient2.interrupt()
  }
}

package simpleServer

/**
  * Created by hminle on 10/25/2016.
  */
object MainSimpleServer extends App {
  override def main(args: Array[String]) {
    val server: SimpleServer = new SimpleServer
    val threadServer = new Thread(server)
    threadServer.start()
  }
}

package networkChannel

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util

/**
  * Created by hminle on 10/24/2016.
  */
class Client2 {

}

object Client2 extends App {
  override def main(args: Array[String]) {
    sendMessage("HELLO 2")
    //sendMessage("Stop Connection")
  }
  def sendMessage(str: String): Unit = {
    val addr: InetSocketAddress = new InetSocketAddress("localhost", 1111)
    val clientChannel: SocketChannel = SocketChannel.open(addr)
    println("Connecting to Server on port 1111")

    /*List[String]("Hello server", "Bye Server", "Stop Connection") foreach{
      str => {
        val message: Array[Byte] = new String(str).getBytes()
        val buffer: ByteBuffer = ByteBuffer.wrap(message)
        clientChannel.write(buffer)
        buffer.clear()
        Thread.sleep(2000)
      }
    }*/

    /*val messages: util.ArrayList[String] = new util.ArrayList[String]()
    messages.add("Hello Server")
    messages.add("Goodbye Server")
    messages.add("Stop Connection")
    for (str <- messages) {
      val message: Array[Byte] = new String(str).getBytes()
      val buffer: ByteBuffer = ByteBuffer.wrap(message)
      clientChannel.write(buffer)
      buffer.clear()
      Thread.sleep(2000)
    }*/
    val message: Array[Byte] = new String(str).getBytes()
    val buffer: ByteBuffer = ByteBuffer.wrap(message)
    clientChannel.write(buffer)
    buffer.clear()
    Thread.sleep(2000)
    clientChannel.close()
  }
}

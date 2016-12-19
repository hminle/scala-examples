package networkChannel

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel, SocketChannel}

import collection.JavaConverters._

/**
  * Created by hminle on 10/24/2016.
  */
/*class Server {

}
*/
object Server extends App {
  override def main(args: Array[String]) {
    // Selector: multiplexor of SelectableChannel objects
    val selector: Selector = Selector.open()
    // ServerSocketChannel: selectable channel for stream-oriented listening sockets
    val serverSocket: ServerSocketChannel = ServerSocketChannel.open()
    val serverAddr: InetSocketAddress = new InetSocketAddress("localhost", 1111)

    // Binds the channel's socket to a local address and configures the socket to listen for connections
    serverSocket.bind(serverAddr)

    // Adjusts this channel's blocking mode
    serverSocket.configureBlocking(false)

    val ops: Int = serverSocket.validOps()
    val selectKey: SelectionKey = serverSocket.register(selector, ops, null)

    while(true) {
      println("I am a server and I'm waiting for new connectioin and buffer select")
      // Select a set of keys whose corresponding channels are ready for I/O operations
      try{
        selector.select()
      } catch{
        case ex: IOException => ex.printStackTrace()
      }

      // token representing the registration of a SelectableChannel with a Selector
      val serverKeys: scala.collection.mutable.Set[SelectionKey] = selector.selectedKeys().asScala
      val serverKeysIterator: java.util.Iterator[SelectionKey] = serverKeys.iterator.asJava
      println("--> CHECK key " + serverKeys)
      while(serverKeysIterator.hasNext) {
        val curKey = serverKeysIterator.next()

        // Tests whether this key's channel is ready to accept a new socket connection
        if(curKey.isAcceptable){
          val client: SocketChannel = serverSocket.accept()

          // Adjust this channel's blocking mode to false
          client.configureBlocking(false)

          // Operation-set bit for read operations
          client.register(selector, SelectionKey.OP_READ)
          println("Connection Accepted : " + client.getLocalAddress)

          // Tests whether this key's channel is ready for reading
        } else if(curKey.isReadable) {
          val client: SocketChannel = curKey.channel().asInstanceOf[SocketChannel]
          val buffer: ByteBuffer = ByteBuffer.allocate(256)
          client.read(buffer)
          val result: String = new String(buffer.array()).trim
          println("Message received: " + result)

          if(result.equals("Stop Connection")) {
            client.close()
            //selector.close()
            println("It's time to close connection")
            println("Server will keep running, try running client again to establish new connection")
          }
        }
        serverKeysIterator.remove()
        curKey.channel().close()
      }
    }
  }
}
package simpleServer
import scala.pickling.Defaults._
import scala.pickling._
import json._
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel, SocketChannel}
import java.util

/**
  * Created by hminle on 10/25/2016.
  */
class SimpleServer extends Runnable {
  val ADDRESS: String = "127.0.0.1"
  val PORT: Int = 8511
  val TIMEOUT: Long = 10000
  var serverChannel: ServerSocketChannel = _
  var selector: Selector = _

  val dataTracking: util.HashMap[SocketChannel, Array[Byte]] = new util.HashMap[SocketChannel, Array[Byte]]()
  def init(): Unit = {
    println("Initalizing server")
    //assert(selector != null)
    //assert(serverChannel != null)
    try {
      // Open a Selector
      selector = Selector.open()
      // Open a ServerSocketChannel
      serverChannel = ServerSocketChannel.open()
      // Configure for non-blocking
      serverChannel.configureBlocking(false)
      // bind to the address
      serverChannel.socket().bind(new InetSocketAddress(ADDRESS, PORT))

      // Told selector that this channel will be used to accept connections
      // We can change this operation later to read/write
      serverChannel.register(selector, SelectionKey.OP_ACCEPT)
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }

  override def run(): Unit = {
    init()
    println("Accepting connections....")

    try {
      while(!Thread.currentThread().isInterrupted){
        // blocking call, can use TIMEOUT here
        selector.select()

        val keys: util.Iterator[SelectionKey] = selector.selectedKeys().iterator()
        //println("keys in Server --> " +keys.toString)
        while(keys.hasNext) {
          val key: SelectionKey = keys.next()

          //remove the key so that we don't process this OPERATION again
          keys.remove()

          //key could be invalid if for example the client closed the connection
          assert(key.isValid)
          if(key.isAcceptable){
            println("Accepting connection")
            accept(key)
          }
          if(key.isWritable){
            println("Writing...")
            write(key)
          }
          if(key.isReadable){
            println("Reading connection")
            read(key)
          }
        }

      }
    } catch {
      case e: IOException => e.printStackTrace()
    } finally {
      closeConnection()
    }
  }

  private def accept(key: SelectionKey): Unit = {
    val serverSocketChannel: ServerSocketChannel = key.channel().asInstanceOf[ServerSocketChannel]
    val socketChannel = serverSocketChannel.accept()
    socketChannel.configureBlocking(false)

    socketChannel.register(selector, SelectionKey.OP_READ)
    //val hello: Array[Byte] = new String("Hello from server").getBytes()
    //dataTracking.put(socketChannel, hello)
  }

  private def write(key: SelectionKey): Unit = {
    val channel: SocketChannel = key.channel().asInstanceOf[SocketChannel]
    val data: Array[Byte] = dataTracking.get(channel)
    channel.write(ByteBuffer.wrap(data))

    key.interestOps(SelectionKey.OP_READ)

  }

  private def read(key: SelectionKey): Unit = {
    val channel: SocketChannel = key.channel().asInstanceOf[SocketChannel]
    val readBuffer: ByteBuffer = ByteBuffer.allocate(1024)
    readBuffer.clear()
    var read: Int = 0
    try {
      read = channel.read(readBuffer)
    } catch {
      case e: IOException => {
        println("Reading problem, closing connection")
        key.cancel()
        channel.close()
        return
      }
    }
    if(read == -1){
      println("Nothing was there to be read, closing connection")
      channel.close()
      key.cancel()
      return
    }
    readBuffer.flip()
    val data: Array[Byte] = new Array[Byte](1000)
    readBuffer.get(data, 0 ,read)
    /*val receivedRequest: String = new String(data)
    receivedRequest.unpickle[MessageSimpleServer] match {
      case RegisterRequest(id, numOfInputSplit, slaveSocketAddress) => println("Received Request from client " + slaveSocketAddress)
    }*/
    println("Server Received: " + new String(data))
    echo(key, data)

  }

  private def echo(key: SelectionKey, data: Array[Byte]): Unit = {
    val socketChannel = key.channel().asInstanceOf[SocketChannel]
    dataTracking.put(socketChannel, data)
    key.interestOps(SelectionKey.OP_WRITE)
  }
  private def closeConnection(): Unit = {
    println("Closing server down")
    if(selector != null){
      try{
        selector.close()
        serverChannel.socket().close()
        serverChannel.close()
      } catch {
        case e: IOException => e.printStackTrace()
      }
    }
  }
}

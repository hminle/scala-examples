package simpleServer
import scala.pickling.Defaults._
import scala.pickling._
import json._
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{SelectionKey, Selector, SocketChannel}
import java.util

/**
  * Created by hminle on 10/25/2016.
  */
class SimpleClient(message: String) extends Runnable {
  var selector: Selector = _

  override def run(): Unit = {
    try{
      selector = Selector.open()
      val channel = SocketChannel.open()
      channel.configureBlocking(false)
      channel.register(selector, SelectionKey.OP_CONNECT)
      channel.connect(new InetSocketAddress("127.0.0.1", 8511))

      while(!Thread.interrupted()) {
        selector.select(1000)
        val keys: util.Iterator[SelectionKey] = selector.selectedKeys().iterator()
        //println("keys in Client" + keys.toString)
        while(keys.hasNext){
          val key = keys.next()
          keys.remove()
          assert(key.isValid)

          if(key.isConnectable){
            println("I am connected to the server")
            connect(key)
          }
          if(key.isWritable){
            write(key)
          }
          if(key.isReadable){
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
  private def closeConnection(): Unit = {
    try {
      selector.close()
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }
  private def connect(key: SelectionKey): Unit = {
    val channel: SocketChannel = key.channel().asInstanceOf[SocketChannel]
    if(channel.isConnectionPending){
      channel.finishConnect()
    }
    channel.configureBlocking(false)
    channel.register(selector, SelectionKey.OP_WRITE)
  }
  private def write(key: SelectionKey): Unit = {
    val channel: SocketChannel = key.channel().asInstanceOf[SocketChannel]
    channel.write(ByteBuffer.wrap(message.getBytes()))
    /*val registerRequest: RegisterRequest = RegisterRequest(1, 1, "localhost")
    val registerRequestPickleValue: String = registerRequest.pickle.value
    channel.write(ByteBuffer.wrap(registerRequestPickleValue.getBytes))
    */

    // get ready to read
    key.interestOps(SelectionKey.OP_READ)
  }
  private def read(key: SelectionKey): Unit = {
    val channel: SocketChannel = key.channel().asInstanceOf[SocketChannel]
    val readBuffer: ByteBuffer = ByteBuffer.allocate(1000)
    readBuffer.clear()
    var length: Int = 0
    try{
      length = channel.read(readBuffer)
    } catch{
      case e: IOException => {
        println("Reading in Client has problem, closing connection")
        key.cancel()
        channel.close()
        return
      }
    }
    if(length == -1){
      println("Nothing was read from server")
      channel.close()
      key.cancel()
      return
    }
    readBuffer.flip()
    val buff: Array[Byte] = new Array[Byte](1024)
    readBuffer.get(buff, 0 , length)
    println("Server said: " + new String(buff))
    Thread.currentThread().interrupt()
  }
}

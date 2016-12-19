package writeOutput

import java.io.{BufferedOutputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import scala.pickling.Defaults._
import readInput._

import scala.pickling._
import scala.pickling.binary._

/**
  * Created by hminle on 11/17/2016.
  */
class FileWriter {
  def writeOutput(dir: String, keyValue: List[(Key, Value)]): Unit = {

    val byteArray: Array[Byte] = flattenKeyValueList(keyValue).toArray[Byte]
    //val byteArray : Array[Byte] = keyValue.head._1.keys.toArray
    val bos = new BufferedOutputStream(new FileOutputStream(dir))
    Stream.continually(bos.write(byteArray))
    bos.close()
  }
  def flattenKeyValueList(keyValue: List[(Key,Value)]): List[Byte] = {
    keyValue flatten {
      case (Key(keys), Value(values)) => keys:::values
    }
  }
}
case class SlaveInfo(address: String, port: Int)
case class SlavePartition(slaveInfo: SlaveInfo, partition: List[(Key, Value)])
object FileWriterExample extends App {
  val fileSplitter = new FileSplitter
  val fileChannel = fileSplitter getFileChannelFromInput("input0.data")
  val keyValue = fileSplitter.getChunkKeyAndValueBySize(10000, fileChannel)
  val fileWriter = new FileWriter
  val key = Key(List(11,22,33,44))
  val value = Value(List(55,66,77,88))
  //fileWriter.writeOutput("./tmp/outputTest", keyValue._1)
  val slavePartition: SlavePartition = SlavePartition(SlaveInfo("localhost", 8511), keyValue._1)
  val pckl = slavePartition.pickle
  val bytes: Array[Byte] = pckl.value
  println("bytes " + bytes.length)
  //val oos = new ObjectOutputStream(new FileOutputStream("./tmp/outputTestWithPickling"))
  //oos.writeObject(bytes)
  //val ois = new ObjectInputStream(new FileOutputStream("./tmp/outputTestWithPickling"))
  //ois.readObject()
}
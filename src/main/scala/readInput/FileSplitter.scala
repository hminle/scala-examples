package readInput

/**
  * Created by hminle on 11/5/2016.
  */
import java.io.{File, FileInputStream, FileNotFoundException, IOException}
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.{Path, Paths}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer
object FileSplitter {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
}
class FileSplitter {
  //val logger = 
  import FileSplitter.logger
  def readInputFile(inputFileName: String): Unit = {
    try{
      val classLoader = getClass().getClassLoader
      val file = new File(classLoader.getResource(inputFileName).getFile)
      val is = new FileInputStream(file)
      val chunk = new Array[Byte](100)
      var chunkLen: Int = 0
      while(chunkLen != -1){
        chunkLen = is.read(chunk)
        logger.info("new line "+chunk.toList.mkString(" -- ") + " new lIne")
      }
    } catch {
      case e: FileNotFoundException => e.printStackTrace()
      case e: IOException => e.printStackTrace()
    }
  }
  def readInputUsingFileChannel(inputFileName: String): Unit = {
    val classLoader = getClass().getClassLoader
    val file = new File(classLoader.getResource(inputFileName).getFile)
    val fileChannel = FileChannel.open(Paths.get(file.getPath))
    val buffer = ByteBuffer.allocate(100)
    var numOfBytesRead = fileChannel.read(buffer)

    while(numOfBytesRead != -1) {
      //logger.info("Num of bytes read: " + numOfBytesRead)
      buffer.flip()
      val chunk = buffer.array()
      //logger.info(buffer.toString)
      logger.info("new line "+chunk.toList.mkString(" -- ") + " new line")
      buffer.clear()
      numOfBytesRead = fileChannel.read(buffer)
    }
    fileChannel.close()
  }
  def getFileChannelFromInput(inputFileName: String): FileChannel = {
    logger.info("get file channel")
    val classLoader = getClass().getClassLoader
    val file = new File(classLoader.getResource(inputFileName).getFile)
    val fileChannel = FileChannel.open(Paths.get(file.getPath))
    fileChannel
  }
  def get100BytesStringAtATime(fileChannel: FileChannel): String = {
    logger.info("get 100 bytes")
    val size = 1000000
    val buffer = ByteBuffer.allocate(100)
    val numOfByteRead = fileChannel.read(buffer)
    if(numOfByteRead != -1){
      val (chunkKey, chunkValue) = buffer.array().splitAt(10)
      //val chunkKey = chunkArray(0-9)
      "Key: " + chunkKey.mkString(" -- ") + " Value: " + chunkValue.mkString(" -- ")
    } else "End Of File"
  }
  def get100BytesKeyAndValue(fileChannel: FileChannel): Option[(Key, Value)] = {
    //logger.info("get key and values in 100 bytes")
    val size = 100
    val buffer = ByteBuffer.allocate(size)
    val numOfByteRead = fileChannel.read(buffer)
    if(numOfByteRead != -1){
      val (chunkKey, chunkValue) = buffer.array().splitAt(10)
      Some(Key(chunkKey.toList), Value(chunkValue.toList))
    } else None
  }
  def getChunkKeyAndValueBySize(size: Int, fileChannel: FileChannel): (List[(Key,Value)], Boolean) ={
    val oneKeyValueSize = 100 // bytes
    val countMax = size / oneKeyValueSize
    var count = 0
    val chunks: ListBuffer[(Key, Value)] = ListBuffer.empty
    var keyValue = get100BytesKeyAndValue(fileChannel)
    while(count < countMax && keyValue.isDefined){
      count += 1
      chunks.append(keyValue.get)
      keyValue = get100BytesKeyAndValue(fileChannel)
    }
    val isEndOfFileChannel: Boolean = !keyValue.isDefined
    (chunks.toList, isEndOfFileChannel)
  }
}
case class Key(keys: List[Byte]) extends Ordered[Key] {
  def isEmpty(): Boolean = keys.isEmpty
  def compare(that: Key): Int = {
    val keys1: List[Int] = this.keys.map(_ & 0xff)
    val keys2: List[Int] = that.keys.map(_ & 0xff)
    compare_aux(keys1, keys2)
  }
  private def compare_aux(keys1: List[Int], keys2: List[Int]): Int = {
    (keys1, keys2) match {
      case (Nil, Nil) => 0
      case (list, Nil) => 1
      case (Nil, list) => -1
      case (hd1::tl1, hd2::tl2) => {
        if(hd1 > hd2) 1
        else if(hd1 < hd2) -1
        else compare_aux(tl1, tl2)
      }
    }
  }
}
case class Value(values: List[Byte])
object TestLog {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
}
class TestLog {
  import TestLog.logger
  def testLog(): Unit = {
    logger.info("TEST LOG IN ANOTHER CLASS")
  }
}
object MainFileSplitter extends App {
  import FileSplitter.logger
  override def main(args: Array[String]) {
    val fileSplitter = new FileSplitter
    //fileSplitter readInputFile("pennyinputTest")
    //fileSplitter readInputUsingFileChannel("pennyinputTest")

    val fileChannel = fileSplitter.getFileChannelFromInput("input0.data")
    println("BEGIN file size " + fileChannel.size())
    val string = fileSplitter.get100BytesStringAtATime(fileChannel)
    logger.info(string)
    val string2 = fileSplitter.get100BytesStringAtATime(fileChannel)
    logger.info(string2)
    println("END file size "+fileChannel.size())
    val testLog = new TestLog
    testLog testLog()

    val oneMBKeyValue1 = fileSplitter.getChunkKeyAndValueBySize(3000000, fileChannel)
    println("num of keys1 " + oneMBKeyValue1._1.length)
    val oneMBKeyValue2 = fileSplitter.getChunkKeyAndValueBySize(3000000, fileChannel)
    println("num of keys2 " + oneMBKeyValue2._1.length)
    val oneMBKeyValue3 = fileSplitter.getChunkKeyAndValueBySize(3000000, fileChannel)
    println("num of keys3 " + oneMBKeyValue3._1.length + oneMBKeyValue3._2)
    val oneMBKeyValue4 = fileSplitter.getChunkKeyAndValueBySize(500, fileChannel)
    println("num of keys4 " + oneMBKeyValue4._1.length + oneMBKeyValue4._2)
    println("UNSORTEd "+oneMBKeyValue4._1.mkString("\n"))
    println("SORTED " + oneMBKeyValue4._1.sortWith(_._1 < _._1).mkString("\n"))
    //val (key1, values1) = fileSplitter.get100BytesKeyAndValue(fileChannel)
    //val (key2, value2) = fileSplitter.get100BytesKeyAndValue(fileChannel)
    val key1 = Key(List[Byte](1,1,1,2))
    val key2 = Key(List[Byte](1,1,1,1))
    val key3 = Key(List[Byte](1,1,1,0))
    val listKey = List(key1, key2, key3)
    val mapKV = Map(key1 -> "abc", key2 -> "xyz", key3 -> "yzt")

    println(listKey.sorted)
    mapKV.keys.toList.sorted.foreach(key => println(key + " --> Value " + mapKV(key)))
    if(key1 > key2) logger.info("Key 1 is greater than key2\n"+ "key1  " + key1.keys.mkString(" -- ") + "\nkey2:  " + key2.keys.mkString(" -- "))
    else logger.info("Key 1 is less than or equal key2\n"+ "key1  " + key1.keys.mkString(" -- ") + "\nkey2:  " + key2.keys.mkString(" -- "))
    logger.info((Runtime.getRuntime.freeMemory()/1024/1024) + "MB free of Memory")
    val listTest = List(1 , 2, 3 ,4 , 5)
    println(listTest)
    println(listTest.drop(1))
    println(math ceil (10485800.0/1024.0/1024.0))
  }
}
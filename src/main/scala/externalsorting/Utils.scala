package externalsorting

import java.io.{BufferedOutputStream, File, FileOutputStream}
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Paths

import readInput._

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
/**
  * Created by hminle on 12/5/2016.
  */
object Utils {
  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if(d.exists() && d.isDirectory){
      d.listFiles.filter(_.isFile).toList
    } else  List[File]()
  }
  def get100BytesKeyAndValue(fileChannel: FileChannel): Option[(Key, Value)] = {
    val size = 100
    val buffer = ByteBuffer.allocate(size)
    buffer.clear()
    val numOfByteRead = fileChannel.read(buffer)
    buffer.flip()
    if(numOfByteRead != -1){
      val data: Array[Byte] = new Array[Byte](numOfByteRead)
      buffer.get(data, 0, numOfByteRead)
      //val dataUnsignedByte: Array[Byte] = data.map{x => (x.unary_~).asInstanceOf[Byte]}
      //dataUnsignedByte foreach(x => println(x))
      val (key, value) = data.splitAt(10)
      Some(Key(key.toList), Value(value.toList))
    } else {
      None
    }
  }
  def getFileChannelFromInput(file: File): FileChannel = {
    //println(Thread.currentThread().getName +"Get file channel from input " + inputFileName)
    //val file: File = new File(inputFileName)
    val fileChannel: FileChannel = FileChannel.open(Paths.get(file.getPath))
    fileChannel
  }
/*  def orderedBinaryFileBuffer(buffer: BinaryFileBuffer): Ordered[BinaryFileBuffer] = new Ordered[BinaryFileBuffer]{
    def compare(other: BinaryFileBuffer) = buffer.head()._1.compare(other.head()._1)
  }*/
  def orderedBinaryFileBuffer(buffer: BinaryFileBuffer) = buffer.head()._1
  def estimateAvailableMemory(): Long = {
    System.gc()
    val runtime: Runtime = Runtime.getRuntime
    val allocatedMemory: Long = runtime.totalMemory() - runtime.freeMemory()
    val presFreeMemory: Long = runtime.maxMemory() - allocatedMemory
    presFreeMemory
  }
  def writePartition(dir: String, keyValue: List[(Key, Value)]): Unit = {
    val byteArray: Array[Byte] = flattenKeyValueList(keyValue).toArray[Byte]
    val bos = new BufferedOutputStream(new FileOutputStream(dir))
    Stream.continually(bos.write(byteArray))
    bos.close()
  }

  def flattenKeyValueList(keyValue: List[(Key,Value)]): List[Byte] = {
    keyValue flatten {
      case (Key(keys), Value(values)) => keys:::values
    }
  }

  def flattenKeyValue(keyVal: (Key, Value)): List[Byte] = {
    keyVal._1.keys:::keyVal._2.values
  }
  def getChunkKeyAndValueBySize(size: Int, fileChannel: FileChannel): (List[(Key, Value)], Boolean) = {
    val oneKeyValueSize = 100
    val countMax = size / oneKeyValueSize
    var isEndOfFileChannel: Boolean = false
    var count = 0
    val chunks: ListBuffer[(Key, Value)] = ListBuffer.empty
    do{
      val keyValue = get100BytesKeyAndValue(fileChannel)
      if(keyValue.isDefined) chunks.append(keyValue.get)
      isEndOfFileChannel = !keyValue.isDefined
      count += 1
    }while(!isEndOfFileChannel && count < countMax)
    (chunks.toList, isEndOfFileChannel)
  }
  def getSortedChunk(oneChunk: List[(Key, Value)]): List[(Key, Value)] = {
    oneChunk.sortWith(_._1 < _._1)
  }
  def compareTwoIntegerList(keys1: List[Int], keys2: List[Int]): Int = {
    (keys1, keys2) match {
      case (Nil, Nil) => 0
      case (list, Nil) => 1
      case (Nil, list) => -1
      case (hd1::tl1, hd2::tl2) => {
        if(hd1 > hd2) 1
        else if(hd1 < hd2) -1
        else compareTwoIntegerList(tl1, tl2)
      }
    }
  }
  def merge_aux(outputName: String, listTempFile: List[File]): File = {
    val tempFileChannels: List[FileChannel] = listTempFile.map(Utils.getFileChannelFromInput(_))
    val binaryFileBuffers: List[BinaryFileBuffer] = tempFileChannels.map(BinaryFileBuffer(_))
    //binaryFileBuffers foreach(x => println(x.toString))

    val pq1: ListBuffer[BinaryFileBuffer] = ListBuffer.empty

    binaryFileBuffers.filter(!_.isEmpty()).foreach(pq1.append(_))
    //val outputName: String = outputDir + "/mergedOutput"
    val bos = new BufferedOutputStream(new FileOutputStream(outputName))

    //var count = 0
    while(pq1.length > 0){
      val pq2 = pq1.toList.sortWith(_.head()._1 < _.head()._1)
      val buffer: BinaryFileBuffer = pq2.head
      val keyVal: (Key, Value) = buffer.pop()
      //if(count >= 995 && count < 1150) println(count + " --> " + keyVal._1.toString())
      println("Merge _ aux " + outputName + " list " + pq1.length)
      val byteArray: Array[Byte] = Utils.flattenKeyValue(keyVal).toArray[Byte]
      Stream.continually(bos.write(byteArray))
      if(buffer.isEmpty()){
        buffer.close()
        pq1 -= buffer
      }
      //count+=1
    }
    bos.close()
    new File(outputName)
  }

  @tailrec def merge(counterTempFile: Int, tempDir: String, outputDir: String, listTempFile: List[File]): File = {
    val MAX_OPEN_FILES = 2500

    if(listTempFile.length <= MAX_OPEN_FILES) {
      val outputName: String = outputDir + "/mergedOutput"
      merge_aux(outputName, listTempFile)
    } else{
      val listTempFileNeedToMerge: List[File] = listTempFile.take(MAX_OPEN_FILES)
      val outputTemp: String = tempDir + "/outputTemp-" + counterTempFile
      val mergedOutputTemp: File = merge_aux(outputTemp, listTempFileNeedToMerge)
      merge(counterTempFile+1, tempDir, outputDir, mergedOutputTemp::listTempFile.drop(MAX_OPEN_FILES))
    }
  }
}

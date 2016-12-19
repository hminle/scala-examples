package externalsorting

import java.io.{BufferedOutputStream, File, FileOutputStream}
import java.nio.channels.FileChannel
import java.util.Calendar

import scala.collection.mutable
import readInput._

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by hminle on 12/5/2016.
  */
object ExternalSortingExample extends App{
  val x: Byte = 114
  val y: Byte = 116
  val z: Byte = x.&(0xff).asInstanceOf[Byte]
  println(x)
  println((x&0xff).asInstanceOf[Byte])
  println(y & 0xFF)
  println(z)
  val dir: String = "C:\\ShareUbuntu\\testMerge"
  val listFile: List[File] = Utils.getListOfFiles(dir)
  listFile foreach(x => println(x.getName))
  var fileChannelsInput: List[(FileChannel, Boolean)] = listFile.map{input => (Utils.getFileChannelFromInput(input), false)}
  val tempDir: String = dir + "/tmp/"
  val tempDirFile: File = new File(tempDir)
  val isSuccessful: Boolean = tempDirFile.mkdir()
  if(isSuccessful) println("Create temp dir successfully")
  else println("Create temp dir failed")

  val executionContext = ExecutionContext.fromExecutorService(java.util.concurrent.Executors.newFixedThreadPool(1))

  var fileNameCounter: Int = 0
  val chunkSize = 1000000
  val startSort = Calendar.getInstance().getTime
  while(!fileChannelsInput.isEmpty){
    //println(Thread.currentThread().getName +"FileSplitter is running")
    if(Utils.estimateAvailableMemory() > 400000){
      val fileChannel = fileChannelsInput(0)._1
      val (chunks, isEndOfFileChannel) = Utils.getChunkKeyAndValueBySize(chunkSize, fileChannel)
      if(isEndOfFileChannel){
       // println(Thread.currentThread().getName + " FileSplitter reaches the end of one input")
        fileChannel.close()
        fileChannelsInput = fileChannelsInput.drop(1)
      } else {
        val sortedChunk: List[(Key, Value)] = Utils.getSortedChunk(chunks)
        val fileName: String = tempDir + "partition-" + fileNameCounter
        Future{
          Utils.writePartition(fileName, sortedChunk)
        }(executionContext)
        fileNameCounter += 1
      }
    } else {
      println(Thread.currentThread().getName +"There is not enough available free memory to continue processing" + Utils.estimateAvailableMemory())
    }
  }


  val listTempFile: List[File] = Utils.getListOfFiles(tempDir)
  val start = Calendar.getInstance().getTime

  val tempFileChannels: List[FileChannel] = listTempFile.map(Utils.getFileChannelFromInput(_))
  val binaryFileBuffers: List[BinaryFileBuffer] = tempFileChannels.map(BinaryFileBuffer(_))
  binaryFileBuffers foreach(x => println(x.toString))

  val pq1: ListBuffer[BinaryFileBuffer] = ListBuffer.empty

  def diff(binaryFileBuffer: BinaryFileBuffer): Key = binaryFileBuffer.head()._1
  val queue = new mutable.PriorityQueue[BinaryFileBuffer]()(Ordering.by(diff).reverse)
  //binaryFileBuffers.filter(!_.isEmpty()).foreach(pq1.append(_))
  binaryFileBuffers.filter(!_.isEmpty()).foreach(queue.enqueue(_))
  println(s"Buffer size ${queue.size}")
  val pq2: ListBuffer[BinaryFileBuffer] = pq1.sortWith(_.head()._1 < _.head()._1)
  val outputDir: String = dir + "/mergedOutput"
  val bos = new BufferedOutputStream(new FileOutputStream(outputDir))

  var count = 0
  while(queue.size > 0){

    val buffer:BinaryFileBuffer = queue.dequeue()
    val keyVal: (Key, Value) = buffer.pop()
    if(count >= 40 && count < 80) println(count + " --> " + keyVal._1.toString())

    val byteArray: Array[Byte] = Utils.flattenKeyValue(keyVal).toArray[Byte]
    Future{
      Stream.continually(bos.write(byteArray))
    } (executionContext)
    if(buffer.isEmpty()){
      buffer.close()
    } else queue.enqueue(buffer)
    count+=1
  }
  bos.close()
  val end = Calendar.getInstance().getTime
  executionContext.shutdown()
  println(s"Startsort at --> $startSort")
  println("Start at --> " + start)
  println("End at --> " + end)

}

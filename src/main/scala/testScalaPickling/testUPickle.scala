package testScalaPickling

import readInput.FileSplitter
import writeOutput.{FileWriter, SlaveInfo, SlavePartition}
import upickle.default._
/**
  * Created by hminle on 11/23/2016.
  */

object testUPickle extends App{
  override def main(args: Array[String]) {
    val fileSplitter = new FileSplitter
    val fileChannel = fileSplitter getFileChannelFromInput("input0.data")
    val keyValue = fileSplitter.getChunkKeyAndValueBySize(30000, fileChannel)
    val fileWriter = new FileWriter
    val slavePartition: SlavePartition = SlavePartition(SlaveInfo("localhost", 8511), keyValue._1)
    val pckl: String = write(slavePartition)
    println("PCKL --> " + pckl)
    val unpikl: SlavePartition = read[SlavePartition](pckl)
    println("UNPIKL --> " + unpikl)
    val booleanPkl = slavePartition == unpikl
    println("TEST EQUAL --> " + booleanPkl)
    val data: Array[Byte] = pckl.getBytes
    val unpikl2: SlavePartition = read[SlavePartition](new String(data))
    val booleanPlk2 = slavePartition == unpikl2
    println("TEST EQUAL2 --> " + booleanPlk2)
    val slave = Slave(slavePartition)
    val slavePkl: String = write(slave)
    val slaveUnpkl: MessageTest = read[MessageTest](slavePkl)
    val booleanPkl3 = slave == slaveUnpkl
    println("Test SLAVE " + booleanPkl3)
    val piklTOString: String = slave.toString
    val boolean4 = slavePkl == piklTOString
    println("TEST TO STRING "+ piklTOString+ "\n" + boolean4)
  }
}

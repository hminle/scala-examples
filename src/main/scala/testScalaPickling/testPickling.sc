import java.io.{FileOutputStream, ObjectOutputStream}
import scala.pickling.Defaults._
import scala.pickling.json._
import readInput.{FileSplitter, Key, Value}
import writeOutput.{FileWriter, SlaveInfo, SlavePartition}

val x = 1
/*val fileSplitter = new FileSplitter
val fileChannel = fileSplitter getFileChannelFromInput("input0.data")
val keyValue = fileSplitter.getChunkKeyAndValueBySize(30000, fileChannel)
val fileWriter = new FileWriter
val slavePartition: SlavePartition = SlavePartition(SlaveInfo("localhost", 8511), keyValue._1)
val pckl = slavePartition.pickle
val unpikl = pckl.unpickle[SlavePartition]
val booleanPkl = slavePartition == unpikl*/
case class KeyRange(beginKey: Int, endKey: Int)
case class SlaveKeyRange(host: String, keyRange: KeyRange)
val allSlaveKeyRange: List[SlaveKeyRange] =
  List(SlaveKeyRange("1", KeyRange(1, 4)), SlaveKeyRange("2", KeyRange(5, 9)), SlaveKeyRange("3", KeyRange(10, 12)))
val minKeyRange: SlaveKeyRange = allSlaveKeyRange.foldLeft(allSlaveKeyRange(2)){
  (min, slaveKeyRange) =>
    if(min.keyRange.beginKey < slaveKeyRange.keyRange.beginKey) min
    else slaveKeyRange
}
val checkMinKeyRange: Boolean = SlaveKeyRange("1", KeyRange(1, 4)) == minKeyRange
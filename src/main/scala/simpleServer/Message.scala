package simpleServer

/**
  * Created by hminle on 10/25/2016.
  */
trait MessageSimpleServer

case class RegisterRequest(id: Int, numOfInputSplit: Int, slaveSocketAddress: String) extends MessageSimpleServer
case class Confirmation(id: Int) extends MessageSimpleServer
case class PartitionTableRequest(id: Int, sampleData: List[Int]) extends MessageSimpleServer
case class PartitionTable(partitionTable: String) extends MessageSimpleServer
case class FinishNotification(id: Int) extends MessageSimpleServer


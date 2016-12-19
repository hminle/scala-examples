package testScalaPickling

import writeOutput.SlavePartition

/**
  * Created by hminle on 11/24/2016.
  */
sealed trait MessageTest
case class Slave(slavePartition: SlavePartition) extends MessageTest
case class Confirmation() extends MessageTest
case class Finished() extends MessageTest
case class Terminated() extends MessageTest
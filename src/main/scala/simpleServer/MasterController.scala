package simpleServer

/**
  * Created by hminle on 10/26/2016.
  */
class MasterController {
  var numOfSlave: Int = 3
  var numOfRegisterRequest: Int = 0
  def slaveRegistering(m: MessageSimpleServer): Unit = m match {
    case m: RegisterRequest => {
      if(numOfRegisterRequest < numOfSlave) {
        println("Slave send RegisterRequest " + m)
        numOfRegisterRequest += 1
      }
      else println("All Slaves have sent RegisterRequest")
    }
  }

}

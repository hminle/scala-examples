//import simpleServer.{HelloFromClient, MessageSimpleServer}
import java.net.InetAddress

import scala.pickling.Defaults._
import scala.pickling._
import json._

val mess = ("Hello worksheet here")
val plk = mess.pickle

case class Person(job: List[String])

val personPlk = Person(List("cleaner", "teacher")).pickle
personPlk.value.getBytes()
val byte = plk.value.getBytes()
type Color = String

val blue: Color = "blue"

val host = InetAddress.getLocalHost.getHostAddress

var list = List(1,2,3)
val hd = list(0)
list = list.drop(1)
val hostMaster = "grey1:8511"
val array = hostMaster.split(":")
val(address, port1) = (array(0), array(1))
val port = array(1)
//port.toInt
val mapDataTracking: Map[String, Int] = Map.empty
val map2 = mapDataTracking.+("localhost" -> 8511)
val map3 = mapDataTracking.+("localhost2" -> 8512)
mapDataTracking.values.toList.mkString(" , ")
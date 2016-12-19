package PlayWithFuture

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by hminle on 11/10/2016.
  */
object FutureExample2 extends App{

  override def main(args: Array[String]) {
    val contextF = ExecutionContext.fromExecutorService(java.util.concurrent.Executors.newFixedThreadPool(5))
    Future {
      println("START FUTURE FOR FUN " + Thread.currentThread().getName)
    } (contextF)
    val taxCutF: Future[TaxCut] = Government.redeemCampaignPledge()
    println("Now that they're elected, let's see if they remember their promises..." + Thread.currentThread().getName)
    taxCutF.onComplete {
      case Success(TaxCut(reduction)) =>
        println(s"A miracle! They really cut our taxes by $reduction percentage points! " + Thread.currentThread().getName)
      case Failure(ex) =>
        println(s"They broke their promises! Again! Because of a ${ex.getMessage}")
    }

    Thread.sleep(5000)
    contextF.shutdown()
  }
}
case class TaxCut(reduction: Int)
object Government {
  def redeemCampaignPledge(): Future[TaxCut] = {
    val p = Promise[TaxCut]()
    Future {
      println("Starting the new legislative period. " + Thread.currentThread().getName)
      Thread.sleep(1000)
      p.success(TaxCut(20))
      println("We reduced the taxes! You must reelect us!!!!1111" + Thread.currentThread().getName)
    }
    //p.success(TaxCut(20))
    p.future
  }
}
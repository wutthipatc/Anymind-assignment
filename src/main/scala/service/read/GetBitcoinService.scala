package service.read

import actor.{GetRestResponse, WalletMessage}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.util.Timeout
import exception.GetBitcoinException
import request.GetBitcoinRequest
import response.{BaseResponse, GetBitcoinResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class GetBitcoinService(bitcoinActor: ActorSystem[WalletMessage]) {
  def getBitcoin(getBitcoinRequest: GetBitcoinRequest): Future[BaseResponse[GetBitcoinResponse]] = {
    implicit val askTimeout: Timeout = Timeout(5.seconds)
    implicit val scheduler: Scheduler = bitcoinActor.scheduler
    val askResult: Future[GetBitcoinResponse] = bitcoinActor ? GetRestResponse.curried(getBitcoinRequest.startDateTime)(getBitcoinRequest.endDateTime)
    askResult.map(BaseResponse.createSuccessResponse[GetBitcoinResponse])
      .recover{
        case ex =>
          ex.printStackTrace()
          BaseResponse.createFailureResponse(GetBitcoinException.AskActorFailedException)
      }
  }
}
object GetBitcoinService {
  def apply(bitcoinActor: ActorSystem[WalletMessage]): GetBitcoinService =
    new GetBitcoinService(bitcoinActor: ActorSystem[WalletMessage])
}

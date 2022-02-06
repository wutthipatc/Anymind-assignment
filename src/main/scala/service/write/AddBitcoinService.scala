package service.write

import command.AddAmount
import exception.AddBitcoinException
import producer.KafkaProducer
import request.AddBitCoinRequest
import response.{AddBitcoinResponse, BaseResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AddBitcoinService(kafkaProducer: KafkaProducer) {
  def addBitcoin(addBitcoinRequest: AddBitCoinRequest): Future[BaseResponse[AddBitcoinResponse]] = {
    val command = AddAmount(addBitcoinRequest.amount, addBitcoinRequest.dateTime)
    kafkaProducer.publishMessage(command, command)
      .map(_ => BaseResponse.createSuccessResponse(AddBitcoinResponse()))
      .recover {
        case ex =>
          ex.printStackTrace()
          BaseResponse.createFailureResponse[AddBitcoinResponse](AddBitcoinException.PublishMessageException)
      }
  }
}
object AddBitcoinService {
  def apply(kafkaProducer: KafkaProducer) = new AddBitcoinService(kafkaProducer)
}

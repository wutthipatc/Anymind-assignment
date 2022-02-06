package response

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonWriter}

case class AddBitcoinResponse(message: String = "Add amount successfully!!!")
object AddBitcoinResponse extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val rootJsonWriter: RootJsonWriter[AddBitcoinResponse] = jsonFormat1(AddBitcoinResponse.apply)
}

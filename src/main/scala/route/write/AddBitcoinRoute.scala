package route.write

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import request.AddBitCoinRequest
import service.write.AddBitcoinService

object AddBitcoinRoute {
  def apply(addBitcoinService: AddBitcoinService): Route = {
    path("add") {
      post {
        decodeRequest {
          entity(as[AddBitCoinRequest]) { addBitcoinRequest =>
            complete {
              addBitcoinService.addBitcoin(addBitcoinRequest)
            }
          }
        }
      }
    }
  }
}

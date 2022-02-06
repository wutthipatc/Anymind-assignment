package route.read

import akka.http.scaladsl.server.Directives.{complete, get, parameters, path}
import akka.http.scaladsl.server.Route
import request.GetBitcoinRequest
import service.read.GetBitcoinService

object GetBitcoinRoute {
  def apply(getBitcoinService: GetBitcoinService): Route =
    path("get") {
      get {
        parameters("startDateTime", "endDateTime") { (startDateTimeStr, endDateTimeStr) =>
          complete {
            val request = GetBitcoinRequest.fromZoneDateTimeString(startDateTimeStr, endDateTimeStr)
            getBitcoinService.getBitcoin(request)
          }
        }
      }
    }
}

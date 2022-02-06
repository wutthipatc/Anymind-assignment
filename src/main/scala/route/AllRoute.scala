package route

import akka.http.scaladsl.server.Directives.{concat, pathPrefix}
import akka.http.scaladsl.server.Route
import route.read.GetBitcoinRoute
import route.write.AddBitcoinRoute
import service.read.GetBitcoinService
import service.write.AddBitcoinService

object AllRoute {
  def apply(addBitcoinService: AddBitcoinService)(getBitcoinService: GetBitcoinService): Route =
    pathPrefix("bitcoin") {
      concat(AddBitcoinRoute(addBitcoinService), GetBitcoinRoute(getBitcoinService))
    }
}

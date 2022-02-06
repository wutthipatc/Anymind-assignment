package response

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{JsArray, JsNumber, JsObject, JsString, JsValue, RootJsonWriter, enrichAny}
import state.WalletState.{CurrentState, WalletSnapshot}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

case class GetBitcoinRecord(amount: Double, dateTime: Instant)
object GetBitcoinRecord {
  implicit object RootJsonWriter extends RootJsonWriter[GetBitcoinRecord] {
    override def write(obj: GetBitcoinRecord): JsValue =
      JsObject("datetime" -> JsString(dateTimeFormatter.format(obj.dateTime)), "amount" -> JsNumber(obj.amount))
  }
  implicit val ordering: Ordering[GetBitcoinRecord] = Ordering.by(_.dateTime)
  def fromWalletSnapshot(snapshot: WalletSnapshot): GetBitcoinRecord = GetBitcoinRecord(snapshot.amount, snapshot.dateTime)
  private val dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC"))
}
case class GetBitcoinResponse(records: List[GetBitcoinRecord])
object GetBitcoinResponse extends SprayJsonSupport {
  implicit object RootJsonWriter extends RootJsonWriter[GetBitcoinResponse] {
    override def write(obj: GetBitcoinResponse): JsValue =
      JsArray(obj.records.map(_.toJson).toVector)
  }
  // start end exclusive
  def fromStartEndDateTimeAndCurrentState(start: Instant, end: Instant, state: CurrentState): GetBitcoinResponse = {
    GetBitcoinResponse(
      state.map
        .filter{case (instant, _) => start.isBefore(instant) && end.isAfter(instant)}
        .transform{case (_, endHourWalletAmount) => endHourWalletAmount.value}
        .toList
        .map(tuple2 => GetBitcoinRecord(BigDecimal(tuple2._2).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble, tuple2._1))
        .sorted
    )
  }
}

package request

import spray.json.DeserializationException

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}

case class GetBitcoinRequest(startDateTime: Instant, endDateTime: Instant)
object GetBitcoinRequest {
  type Predicate[T] = T => Boolean
  private val dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
  def fromZoneDateTimeString(startDateTimeStr: String, endDateTimeStr: String): GetBitcoinRequest = {
    val zoneOffsetFromStringFn: String => ZoneOffset = s => {
      val isStringContainPlus: Predicate[String] = _.contains("+")
      val isStringContainMinus: Predicate[String] = _.contains("-")
      if (isStringContainPlus(s)) ZoneOffset.of(s.substring(s.indexOf("+")))
      else if (isStringContainMinus(s)) ZoneOffset.of(s.substring(s.indexOf("-")))
      else ZoneOffset.of("Z")
    }
    val dateTimeStrToInstantTryFn: String => Try[Instant] = s => Try(LocalDateTime.parse(s, dateTimeFormatter).toInstant(zoneOffsetFromStringFn(s)))
    val requestTry = for {
      startDateTime <- dateTimeStrToInstantTryFn(startDateTimeStr)
      endDateTime <- dateTimeStrToInstantTryFn(endDateTimeStr)
    } yield GetBitcoinRequest(startDateTime, endDateTime)
    requestTry match {
      case Success(value) => value
      case Failure(ex) =>
        ex.printStackTrace()
        throw DeserializationException("Invalid start or end date time parameter")
    }
  }
}

package request

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DeserializationException, JsNumber, JsString, JsValue, RootJsonReader}

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneOffset}
import scala.util.{Failure, Success, Try}

case class AddBitCoinRequest(amount: Double, dateTime: Instant)
object AddBitCoinRequest extends SprayJsonSupport {
  implicit object RootJsonReader extends RootJsonReader[AddBitCoinRequest] {
    private val dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    override def read(json: JsValue): AddBitCoinRequest = {
      type Predicate[T] = T => Boolean
      val zoneOffsetFromStringFn: String => ZoneOffset = s => {
        val isStringContainPlus: Predicate[String] = _.contains("+")
        val isStringContainMinus: Predicate[String] = _.contains("-")
        if (isStringContainPlus(s)) ZoneOffset.of(s.substring(s.indexOf("+")))
        else if (isStringContainMinus(s)) ZoneOffset.of(s.substring(s.indexOf("-")))
        else ZoneOffset.of("Z")
      }
      json.asJsObject.getFields("amount", "dateTime") match {
        case Seq(JsNumber(amount), JsString(dateTimeStr)) =>
          Try(LocalDateTime.parse(dateTimeStr, dateTimeFormatter).toInstant(zoneOffsetFromStringFn(dateTimeStr)))
            .map(instant => AddBitCoinRequest(amount.doubleValue, instant)) match {
            case Success(value) => value
            case Failure(ex) =>
              ex.printStackTrace()
              throw DeserializationException("Invalid dateTime value")
          }
        case _ => throw DeserializationException("Invalid add-bitcoin request body")
      }
    }
  }
}

package command

import command.BitcoinCommand.CommandToStringFn

import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter

sealed trait BitcoinCommand
object BitcoinCommand {
  type CommandToStringFn[T <: BitcoinCommand] = T => String
}
case class AddAmount(amount: Double, instant: Instant) extends BitcoinCommand
object AddAmount {
  import spray.json._
  implicit object RootJsonFormat extends RootJsonFormat[AddAmount] {
    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("GMT+7"))
    override def write(obj: AddAmount): JsValue =
      JsObject("amount" -> JsNumber(obj.amount), "instant" -> JsString(dateTimeFormatter.format(obj.instant)))

    override def read(json: JsValue): AddAmount = json.asJsObject.getFields("amount", "instant") match {
      case Seq(JsNumber(amount), JsString(dateTimeStr)) =>
        AddAmount(amount.toDouble, LocalDateTime.parse(dateTimeStr, dateTimeFormatter).toInstant(ZoneOffset.of("+7")))
      case _ => throw DeserializationException("Invalid Kafka event message")
    }
  }
  implicit val toStringFn: CommandToStringFn[AddAmount] = addAmount => addAmount.toJson.compactPrint
  def fromJsonString(jsonString: String): AddAmount = jsonString.parseJson.convertTo[AddAmount]
}

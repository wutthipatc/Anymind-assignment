package state

import java.time.{Instant, ZoneId, ZonedDateTime}

object WalletState {
  case class CurrentState(totalAmount: Double, map: Map[Instant, EndHourWalletAmount]) {
    def addSnapshot(snapshot: WalletSnapshot): CurrentState =
      CurrentState(totalAmount + snapshot.amount, addOrUpdate(snapshot.dateTime -> snapshot, totalAmount))
    private def addOrUpdate(tuple2: (Instant, WalletSnapshot), totalAmount: Double): Map[Instant, EndHourWalletAmount] = {
      val key = transformKeyInstantToEndHour(tuple2._1)
      map.get(key) match {
        case Some(value) => map.updated(key, value + tuple2._2)
        case None => map ++ Map(key -> EndHourWalletAmount.fromWalletSnapshotAndTotalAmount(tuple2._2, totalAmount))
      }
    }
    private def transformKeyInstantToEndHour(instant: Instant): Instant = {
      val zonedDateTime = instant.atZone(ZoneId.of("UTC"))
      val zonedDateTimeToEndHourInstantFn: ZonedDateTime => Instant = zd =>
        Instant.parse(s"${zd.getYear}-${"%02d".format(zd.getMonthValue)}-${"%02d".format(zd.getDayOfMonth)}T${zd.getHour}:00:00.00Z")
      val zonedDateTimeToEndHourInstantPlusAnHourFn: ZonedDateTime => Instant = zd => {
        val plusHourZD = zd.plusHours(1)
        Instant.parse(s"${plusHourZD.getYear}-${"%02d".format(plusHourZD.getMonthValue)}-${"%02d".format(plusHourZD.getDayOfMonth)}T${plusHourZD.getHour}:00:00.00Z")
      }
      if (zonedDateTime.getMinute == 0 && zonedDateTime.getSecond == 0)
        zonedDateTimeToEndHourInstantFn(zonedDateTime)
      else zonedDateTimeToEndHourInstantPlusAnHourFn(zonedDateTime)
    }
  }
  case class WalletSnapshot(amount: Double, dateTime: Instant)
  case class EndHourWalletAmount(value: Double) {
    def +(snapshot: WalletSnapshot): EndHourWalletAmount = EndHourWalletAmount(value + snapshot.amount)
  }
  object EndHourWalletAmount {
    def fromWalletSnapshotAndTotalAmount(snapshot: WalletSnapshot, totalAmount: Double): EndHourWalletAmount = EndHourWalletAmount(snapshot.amount + totalAmount)
  }
}

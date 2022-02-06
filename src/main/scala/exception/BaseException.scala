package exception

sealed trait BaseException {
  def getErrorCode: String
  def getErrorMsg: String
}
sealed trait AddBitCoinSystemException extends BaseException
sealed trait AddBitcoinBusinessException extends BaseException
object AddBitcoinException {
  val errorCodePrefix = "001"
  object PublishMessageException extends AddBitCoinSystemException {
    override def getErrorCode: String = s"${errorCodePrefix}001"
    override def getErrorMsg: String = "Publish message to broker exception"
  }
}
sealed trait GetBitCoinSystemException extends BaseException
sealed trait GetBitcoinBusinessException extends BaseException
object GetBitcoinException {
  val errorCodePrefix = "002"
  object AskActorFailedException extends GetBitCoinSystemException {
    override def getErrorCode: String = s"${errorCodePrefix}001"
    override def getErrorMsg: String = "Ask actor get bitcoin failed exception"
  }
}

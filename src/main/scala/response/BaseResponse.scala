package response

import exception.BaseException
import spray.json.{DefaultJsonProtocol, JsBoolean, JsObject, RootJsonWriter, enrichAny}

case class ErrorData(code: String, msg: String)
object ErrorData extends DefaultJsonProtocol {
  implicit val jsonWriter: RootJsonWriter[ErrorData] = jsonFormat2(ErrorData.apply)
  def fromBaseException(ex: BaseException): ErrorData =
    ErrorData(ex.getErrorCode, ex.getErrorMsg)
}
case class BaseResponse[T: RootJsonWriter](isSuccess: Boolean, resultData: Option[T], errorData: Option[ErrorData])
object BaseResponse {
  implicit def jsonWriter[T: RootJsonWriter]: RootJsonWriter[BaseResponse[T]] =
    (obj: BaseResponse[T]) =>
    if (obj.isSuccess)
      JsObject("isSuccess" -> JsBoolean(obj.isSuccess), "resultData" -> obj.resultData.get.toJson)
    else JsObject("isSuccess" -> JsBoolean(obj.isSuccess), "errorData" -> obj.errorData.get.toJson)

  def createSuccessResponse[T: RootJsonWriter](resultData: T): BaseResponse[T] =
    BaseResponse(isSuccess = true, Some(resultData), None)
  def createFailureResponse[T: RootJsonWriter](ex: BaseException): BaseResponse[T] =
    BaseResponse(isSuccess = false, None, Some(ErrorData.fromBaseException(ex)))
}

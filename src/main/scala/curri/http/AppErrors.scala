package curri.http

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

case class HttpException(code: StatusCode, msg: String) extends Exception

object AppErrors {
  val noUser = HttpException(StatusCodes.Unauthorized, "User is mandatory")
}

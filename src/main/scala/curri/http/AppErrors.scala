package curri.http

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

case class HttpException(code: StatusCode, msg: String) extends Exception

object AppErrors {

  val unauthorized = HttpException(StatusCodes.Unauthorized, "User is mandatory")

  def forbidden(msg: String) = HttpException(StatusCodes.Forbidden, msg)

  def badRequest(msg: String) = HttpException(StatusCodes.BadRequest, msg)

  def internalError(msg: String) = HttpException(StatusCodes.InternalServerError, msg)


}


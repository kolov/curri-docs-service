package curri

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Utils {

  object Responses {
    val NOT_FOUND = Future[HttpResponse] {
      HttpResponse(status = StatusCodes.NotFound)
    }
    val BAD_REQUEST = Future[HttpResponse] {
      HttpResponse(status = StatusCodes.BadRequest)
    }

    val OK = Future(HttpResponse(status = StatusCodes.OK))
    val ERROR = Future(HttpResponse(status = StatusCodes.InternalServerError))


  }

}

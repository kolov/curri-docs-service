package curri

import akka.http.model.{HttpResponse, StatusCodes}
import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future


object Utils {


  def convertToString(input: List[BSONDocument]): String = {
    input
      .map(f => convertToString(f))
      .mkString("[", ",", "]")
  }

  def convertToString(input: BSONDocument): String = {
    Json.stringify(BSONFormats.toJSON(input))
  }

  object Responses {
    val NOT_FOUND = Future[HttpResponse] {
      HttpResponse(status = StatusCodes.NotFound)
    }

    val OK = Future(HttpResponse(status = StatusCodes.OK))
  }

}

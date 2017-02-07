package curri.docs.domain

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import curri.Utils
import curri.Utils.convertToString
import curri.db.Repository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Handler for async, alternative to routes
  */
object DocsHandler {

  def returnDocs(user: String, maybeGroups: Option[Seq[String]]): Future[HttpResponse] = {
    for {
      input <- Repository.findDocs(user, maybeGroups)
    } yield {
      HttpResponse(entity = convertToString(input))
    }
  }

  def asyncHandler(request: HttpRequest): Future[HttpResponse] = {

    // we match the request, and some simple path checking
    request match {


      // Retreive documets belonging to user
      // request will contain mandatory header x-curri-user
      // and optional header x-curri-group
      case HttpRequest(GET, Uri.Path("/docs/all"), _, _, _) => {

        request.headers.filter(_.is("x-curri-user")).headOption match {
          case Some(userHeader) =>
            returnDocs(userHeader.value(),
              request.headers.filter(_.is("x-curri-group")).headOption.map(_.name).map(_.split(",")))
          case None => Utils.Responses.BAD_REQUEST
        }
      }

      // Simple case that matches everything, just return a not found
      case HttpRequest(_, _, _, _, _)
      => Utils.Responses.NOT_FOUND
    }
  }
}

import akka.actor._
import akka.http.Http
import akka.http.model._
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.FlowGraphImplicits._
import akka.stream.scaladsl.{Flow, _}
import curri.db.Repository
import curri.docs.domain.{CurriDocument, DocsHandler}
import reactivemongo.core.commands.LastError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Simple Object that starts an HTTP server using akka-http. All requests are handled
  * through an Akka flow.
  */
object DocsService extends App {

  // the actor system to use. Required for flowmaterializer and HTTP.
  // passed in implicit
  implicit val system = ActorSystem("Streams")
  implicit val materializer = FlowMaterializer()

  // start the server on the specified interface and port.
  val serverBinding = Http().bind(interface = "localhost", port = 8091)
  val serverBinding2 = Http().bind(interface = "localhost", port = 8092)

  // helper actor for some logging
  val idActor = system.actorOf(Props[IDActor], "idActor");
  idActor ! "start"

  val in = UndefinedSource[HttpRequest]
  val out = UndefinedSink[HttpResponse]

  val mapToResponse = Flow[LastError].map[HttpResponse](
    (inp: LastError) => HttpResponse(status = if (inp.ok) StatusCodes.OK else StatusCodes.InternalServerError)
  )

  val getBody = Flow[HttpRequest].mapAsync[CurriDocument](docFromRequest)
  val save = Flow[CurriDocument].mapAsync[LastError](saveDoc(_))

  val broadCastMergeFlow = Flow[HttpRequest, HttpResponse]() {
    implicit builder =>
      in ~> getBody ~> save ~> mapToResponse ~> out
      (in, out)
  }

  // Handles port 8090
  serverBinding2.connections.foreach { connection =>
    connection.handleWith(broadCastMergeFlow)
    //    idActor ! "start"
  }

  // Handles port 8091
  serverBinding.connections.foreach { connection =>
    val docsPattern = "/docs/.*".r
    connection.handleWith(Flow[HttpRequest].mapAsync(req => {
      req.uri.path.toString() match {
        case docsPattern() => DocsHandler.asyncHandler(req)
        case _ => {
          Future[HttpResponse] {
            HttpResponse(status = StatusCodes.NotFound)
          }
        }
      }
    }))

    //    idActor ! "start"
  }

  def docFromRequest(request: HttpRequest): Future[CurriDocument] =
    request.entity.toStrict(5 seconds).map(_.data.decodeString("UTF-8"))
      .map(body => {
        CurriDocument(
          "xx",
          body,
          request.headers.filter(_.is("user")).headOption.map(_.value()),
          request.headers.find(_.is("group")).headOption.map(_.value())
        )
      })

  def saveDoc(doc: CurriDocument): Future[LastError] =
    Repository.save(doc)
}


class IDActor extends Actor with ActorLogging {

  def receive = {
    case "start" =>
      log.info("Current Actors in system:")
      self ! ActorPath.fromString("akka://Streams/user/")

    case path: ActorPath =>
      context.actorSelection(path / "*") ! Identify(())

    case ActorIdentity(_, Some(ref)) =>
      log.info(ref.toString())
      self ! ref.path

  }
}

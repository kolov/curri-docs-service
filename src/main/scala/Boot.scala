import akka.actor._
import akka.http.Http
import akka.http.model._
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{Flow, _}
import curri.docs.domain.DocsHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Simple Object that starts an HTTP server using akka-http. All requests are handled
  * through an Akka flow.
  */
object Boot extends App {

  // the actor system to use. Required for flowmaterializer and HTTP.
  // passed in implicit
  implicit val system = ActorSystem("Streams")
  implicit val materializer = FlowMaterializer()

  // start the server on the specified interface and port.
  val serverBinding1 = Http().bind(interface = "localhost", port = 8090)
  val serverBinding2 = Http().bind(interface = "localhost", port = 8091)

  // helper actor for some logging
  val idActor = system.actorOf(Props[IDActor], "idActor");
  idActor ! "start"

  // but we can also construct a flow from scratch and use that. For this
  // we first define some basic building blocks

  // broadcast sends the incoming event to multiple targets
  val bCast = Broadcast[HttpRequest]


  // We'll use the source and output provided by the http endpoint
  val in = UndefinedSource[HttpRequest]
  val out = UndefinedSink[HttpResponse]

  // waits for events on the three inputs and returns a response
  val zip = ZipWith[String, String, String, HttpResponse](
    (inp1, inp2, inp3) => new HttpResponse(status = StatusCodes.OK, entity = inp1 + inp2 + inp3)
  )

  // when an element is available on one of the inputs, take
  // that one, igore the rest
  val merge = Merge[String]
  // since merge doesn't output a HttpResponse add an additional map step.
  val mapToResponse = Flow[String].map[HttpResponse](
    (inp: String) => HttpResponse(status = StatusCodes.OK, entity = inp)
  )



  // Handles port 8091
  serverBinding2.connections.foreach { connection =>
    // connection.handleWith(Flow[HttpRequest].mapAsync(StocksHandler.asyncStocksHandler))
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

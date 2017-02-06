import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorPath, ActorSystem, Identify, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.stream.ActorMaterializer
import curri.Config
import curri.db.Repository
import curri.docs.domain.{CurriDocument, CurriDocumentReader}
import reactivemongo.core.commands.LastError
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait Protocols extends DefaultJsonProtocol {
  implicit val ipDocFormat = jsonFormat4(CurriDocument.apply)
}

/**
  * Simple Object that starts an HTTP server using akka-http. All requests are handled
  * through an Akka flow.
  */
trait DocsService extends Config with Protocols {

  implicit val system = ActorSystem("Streams")
  implicit val materializer = ActorMaterializer()


  def saveDoc(doc: CurriDocument): Future[LastError] =
    Repository.save(doc)

  def fetchDocs(user: Option[String], group: Option[String], params: Map[String, String])
  : Future[List[CurriDocument]] = {

    user match {
      case None => throw new IllegalArgumentException("User is mandatory")
      case Some(u) => {
        Repository.findDocs(u, group).map(l => l.map(CurriDocumentReader.read(_)))
      }
    }
  }

  def fetchDocs(user: String, group: Option[String], params: Map[String, String])
  : Future[List[CurriDocument]]= {
    Repository.findDocs(user, group).map(l => l.map(CurriDocumentReader.read(_)))
  }

  def fetchDoc(user: Option[String], group: Option[String], id: String, params: Map[String, String])
  : Future[Either[String, List[CurriDocument]]] = {
    Future {
      Right(List(CurriDocument.apply("title", "body", Some("x"), None)))
    }
  }

  val routes = {
    logRequestResult("curri-akka") {
      pathPrefix("docs") {
        parameterMap { params =>
          optionalHeaderValueByName("x-curri-user") { user =>
            user match {
              case Some(u) =>
                optionalHeaderValueByName("x-curri-group") { group =>
                  pathEndOrSingleSlash {
                    get {
                      complete {
                        fetchDocs(u, group, params)
                      }
                    } ~
                      (get & path(Segment)) { docId => {
                        complete {
                          fetchDoc(user, group, docId, params).map[ToResponseMarshallable] {
                            case Right(doc) => doc
                            case Left(errorMessage) => BadRequest -> errorMessage
                          }
                        }
                      }
                      }
                  }
                }
              case None => throw new IllegalArgumentException("User is mandatory")
            }

          }
        }
      }
    }
  }
}


object DocsServiceApp extends App with DocsService with Config {


  // helper actor for some logging
  val idActor = system.actorOf(Props[IDActor], "idActor");
  idActor ! "start"


  val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, "0.0.0.0", 9000)
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


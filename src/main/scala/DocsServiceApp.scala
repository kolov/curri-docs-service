import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorPath, ActorSystem, Identify, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.ActorMaterializer
import curri.Config
import curri.db.Repository
import curri.docs.domain.{CurriDocument}
import curri.http.{Api, AppErrors, HttpException}
import curri.tools.DbInitializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.circe.java8.time._

trait DocsService extends Config   {


  def saveDoc(user: String, groups: Seq[String], doc: CurriDocument): Future[String] = {
    // if user or group are present in document, they must match the
    // user/groups from request
    doc.user match {
      case Some(ownerUser) => if (ownerUser != user)
        throw AppErrors.forbidden("User in doc doesn't match user in " + "request")
      case None => if (!doc.group.isDefined)
        throw AppErrors.badRequest("Either user or group must be defined in request")
    }
    doc.group match {
      case Some(group) => if (!groups.contains(group))
        throw AppErrors.forbidden("Group in doc doesn't match groups in request")
      case None =>
    }
    Repository.save(doc)
  }

  def fetchDocs(user: String, groups: Seq[String], params: Map[String, String])
  : Future[List[CurriDocument]] = {
    Repository.findDocs(user, groups)
  }

  def fetchDoc(user: String, groups: Seq[String], id: String, params: Map[String, String])
  : Future[CurriDocument] = {
    Repository.findDoc(user, groups, id).map(_ match {
      case Some(doc) => doc
      case None => throw AppErrors.notFound
    })
  }


  val exceptionHandler = ExceptionHandler {
    case e: HttpException =>
      complete(HttpResponse(status = e.code, entity = e.msg))
    case e: Exception =>
      complete(HttpResponse(status = StatusCodes.InternalServerError, entity = e.getMessage))
  }


  val routes = {
    logRequestResult("curri-akka") {
      handleExceptions(exceptionHandler) {
        pathPrefix("docs") {
          parameterMap { params =>
            optionalHeaderValueByName(Api.HEADER_USER) { user =>
              user match {
                case None => throw AppErrors.unauthorized
                case Some(user) =>
                  optionalHeaderValueByName(Api.HEADER_GROUPS) { groupsHeader => {
                    val groups: Seq[String] = groupsHeader.map(_.split(",").toList).getOrElse(Seq())
                    pathEndOrSingleSlash {
                      get {
                        complete {
                          fetchDocs(user, groups, params)
                        }
                      } ~ post {
                        entity(as[CurriDocument]) { doc => {
                          complete {
                            saveDoc(user, groups, doc) // eventual group will be part of doc request
                          }
                        }
                        }
                      }
                    } ~
                      path(Segment) { docId => {
                        complete {
                          fetchDoc(user, groups, docId, params)
                        }
                      }
                      }
                  }
                  }
              }
            }
          }
        }
      }
    }
  }
}


object DocsServiceApp extends App with DocsService with Config {

  implicit val system = ActorSystem("Streams")
  implicit val materializer = ActorMaterializer()

  // helper actor for some logging
  val idActor = system.actorOf(Props[IDActor], "idActor")
  idActor ! "start"

  val logger = Logging(system, getClass)

  for {
    (hasDocs, done) <- DbInitializer.migrate()
    _ <- Http().bindAndHandle(routes, "0.0.0.0", httpPort)
  } yield ()
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


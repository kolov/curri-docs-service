package curri

import java.io.IOException

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import curri.docs.domain.CurriDocument
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.math._

/**
  * The original example form Lightbend, keep for reference for a while
  *

  */
case class IpInfo(query: String, country: Option[String], city: Option[String], lat: Option[Double], lon: Option[Double])

case class IpPairSummaryRequest(ip1: String, ip2: String)

case class IpPairSummary(distance: Option[Double], ip1Info: IpInfo, ip2Info: IpInfo)

object IpPairSummary {
  def apply(ip1Info: IpInfo, ip2Info: IpInfo): IpPairSummary = IpPairSummary(calculateDistance(ip1Info, ip2Info), ip1Info, ip2Info)

  private def calculateDistance(ip1Info: IpInfo, ip2Info: IpInfo): Option[Double] = {
    (ip1Info.lat, ip1Info.lon, ip2Info.lat, ip2Info.lon) match {
      case (Some(lat1), Some(lon1), Some(lat2), Some(lon2)) =>
        // see http://www.movable-type.co.uk/scripts/latlong.html
        val φ1 = toRadians(lat1)
        val φ2 = toRadians(lat2)
        val Δφ = toRadians(lat2 - lat1)
        val Δλ = toRadians(lon2 - lon1)
        val a = pow(sin(Δφ / 2), 2) + cos(φ1) * cos(φ2) * pow(sin(Δλ / 2), 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        Option(EarthRadius * c)
      case _ => None
    }
  }

  private val EarthRadius = 6371.0
}

trait Protocols extends DefaultJsonProtocol {
  implicit val ipInfoFormat = jsonFormat5(IpInfo.apply)
  implicit val ipPairSummaryRequestFormat = jsonFormat2(IpPairSummaryRequest.apply)
  implicit val ipPairSummaryFormat = jsonFormat3(IpPairSummary.apply)
  implicit val ipDocFormat = jsonFormat4(CurriDocument.apply)
}

trait Service extends Protocols {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer


  val logger: LoggingAdapter

  lazy val ipApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection("ip-api.com", 80)

  def ipApiRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request).via(ipApiConnectionFlow).runWith(Sink.head)

  def fetchIpInfo(ip: String): Future[Either[String, IpInfo]] = {
    ipApiRequest(RequestBuilding.Get(s"/json/$ip")).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[IpInfo].map(Right(_))
        case BadRequest => Future.successful(Left(s"$ip: incorrect IP format"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"FreeGeoIP request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }


  def fetchDocs(user: Option[String], group: Option[String], params: Map[String, String])
  : Future[Either[String, List[CurriDocument]]] = {
    Future {
      Right(List(CurriDocument.apply("title", "body", Some("x"), None)))
    }
  }

  def fetchDoc(user: Option[String], group: Option[String], id: String, params: Map[String, String])
  : Future[Either[String, List[CurriDocument]]] = {
    Future {
      Right(List(CurriDocument.apply("title", "body", Some("x"), None)))
    }
  }

  val routes = {
    logRequestResult("curri-akka") {
      pathPrefix("ip") {
        (get & path(Segment)) { ip =>
          complete {
            fetchIpInfo(ip).map[ToResponseMarshallable] {
              case Right(ipInfo) => ipInfo
              case Left(errorMessage) => BadRequest -> errorMessage
            }
          }
        } ~
          (post & entity(as[IpPairSummaryRequest])) { ipPairSummaryRequest =>
            complete {
              val ip1InfoFuture = fetchIpInfo(ipPairSummaryRequest.ip1)
              val ip2InfoFuture = fetchIpInfo(ipPairSummaryRequest.ip2)
              ip1InfoFuture.zip(ip2InfoFuture).map[ToResponseMarshallable] {
                case (Right(info1), Right(info2)) => IpPairSummary(info1, info2)
                case (Left(errorMessage), _) => BadRequest -> errorMessage
                case (_, Left(errorMessage)) => BadRequest -> errorMessage
              }
            }
          }
      } ~
        pathPrefix("docs") {
          parameterMap { params =>
            optionalHeaderValueByName("x-curri-user") { user =>
              optionalHeaderValueByName("x-curri-group") { group =>
                pathEndOrSingleSlash {
                  get {
                    complete {
                      fetchDocs(user, group, params).map[ToResponseMarshallable] {
                        case Right(doc) => doc
                        case Left(errorMessage) => BadRequest -> errorMessage
                      }
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
            }
          }
        }
    }
  }
}

object AkkaHttpMicroservice extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, "0.0.0.0", 9000)
}
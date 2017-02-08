import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import curri.http.Api
import org.scalatest.{Matchers, WordSpec}

class DocsServiceTest extends WordSpec with Matchers with ScalatestRouteTest
  with DocsService {


  "The service" should {

    // sealed routes process rejections too
    val sealedRoutes = Route.seal(routes)
    "request without user header" in {
      Get("/docs") ~> sealedRoutes ~> check {
        status shouldEqual StatusCodes.Unauthorized
      }
    }

    "post without body" in {
      // tests:
      Post("/docs").withHeaders(List(RawHeader(Api.HEADER_USER, "2"))) ~> sealedRoutes ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }

  }
}

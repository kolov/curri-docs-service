package curri.db


import akka.http.scaladsl.model.StatusCodes
import curri.Config
import curri.docs.domain.CurriDocument
import curri.docs.domain.CurriDocument._
import curri.http.HttpException
import reactivemongo.api._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Access to the Mongodb repository
  */
object Repository extends Config {

  final val DOCS_COLLECTION = "docs"

  val driver = new MongoDriver
  val connection = driver.connection(List(mongoHost))

  val collectionDocs = connect(DOCS_COLLECTION)

  def shutdown() = {
    connection.close()
    driver.close()
  }

  def connect(name: String): BSONCollection = {
    val db = connection(mongoDB)
    db.collection(name)
  }

  def findDocs(user: String, groups: Seq[String]): Future[List[CurriDocument]] = {
    val docsByUser = collectionDocs.find(
      BSONDocument("user" -> user)).cursor[CurriDocument]().collect[List]()
    val docsByGroup = collectionDocs.find(
      BSONDocument("group" -> "all")).cursor[CurriDocument]().collect[List]()

    Future.fold(List(docsByUser, docsByGroup))(List[CurriDocument]())(_ ++ _)
  }

  def findDoc(user: String, groups: Seq[String], id: String): Future[Option[CurriDocument]] = {
    collectionDocs
      .find(BSONDocument("_id" -> BSONObjectID(id))).one[CurriDocument]
  }

  def hasDocs(): Future[Boolean] = {
    collectionDocs
      .find(BSONDocument())
      .cursor[BSONDocument]()
      .collect[List](1).map(!_.isEmpty)
  }


  // return the saved document which will have an id
  def save(curriDocument: CurriDocument): Future[String] = {
    val doc = bsonCodec.write(curriDocument)
    val id = BSONObjectID.generate
    collectionDocs.insert(doc.add(BSONDocument("_id" -> id)))
      .map(lastFailure => if (lastFailure.ok) {
        id.stringify
      } else {
        throw new HttpException(StatusCodes.InternalServerError, lastFailure.toString)
      })

  }

}





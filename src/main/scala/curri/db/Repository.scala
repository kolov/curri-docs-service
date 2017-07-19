package curri.db


import akka.http.scaladsl.model.StatusCodes
import curri.Config
import curri.docs.domain.{CurriDocument, CurriDocumentWriter}
import curri.http.HttpException
import reactivemongo.api._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONArray, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

  def findDocs(user: String, groups: Seq[String]): Future[List[BSONDocument]] = {
    val docsByUser: Future[List[BSONDocument]] = collectionDocs.find(BSONDocument("user" -> user)).cursor[BSONDocument].collect[List]()
    val docsByGroup: Future[List[BSONDocument]] = collectionDocs.find(
      // BSONDocument("user" -> None) ++
//      BSONDocument("group" -> BSONDocument("$in" -> groups))).cursor[BSONDocument].collect[List]()
      BSONDocument("group" -> "all")).cursor[BSONDocument].collect[List]()

    Future.fold(List(docsByUser, docsByUser))(List[BSONDocument]())(_ ++ _)
  }

  def findDoc(user: String, groups: Seq[String], id: String): Future[Option[BSONDocument]] = {

    collectionDocs
      .find(BSONDocument("_id" -> BSONObjectID(id)))
      // check id/group
      .cursor[BSONDocument]
      .collect[List]().map(_.headOption)
  }


  // return the saved document which will have an id
  def save(curriDocument: CurriDocument): Future[String] = {
    val doc = CurriDocumentWriter.write(curriDocument)
    val id = BSONObjectID.generate
    collectionDocs.insert(doc.add(BSONDocument("_id" -> id)))
      .map(lastFailure => if (lastFailure.ok) {
        id.stringify
      } else {
        throw new HttpException(StatusCodes.InternalServerError, lastFailure.message)
      })

  }

}





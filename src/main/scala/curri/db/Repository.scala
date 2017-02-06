package curri.db


import curri.Config
import curri.docs.domain.{CurriDocument, CurriDocumentWriter}
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.LastError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Access to the Mongodb repository
  */
object Repository extends Config {

  val collectionDocs = connect("docs")

  def connect(name: String): BSONCollection = {

    val driver = new MongoDriver
    val connection = driver.connection(List(mongoHost))

    val db = connection(mongoDB)
    db.collection(name)
  }

  def findDocs(user: String, group: Option[String]): Future[List[BSONDocument]] = {
    // which results in a Future[List[BSONDocument]]
    collectionDocs
      .find(makeDoc(user, group))
      .cursor[BSONDocument]
      .collect[List]()
  }


  private def makeDoc(user: String, group: Option[String]) = {
    group match {
      case Some(group) => BSONDocument("ownerUser" -> user, "ownerGroup" -> group)
      case None => BSONDocument("ownerUser" -> user)
    }
  }

  def save(user: Option[String], group: Option[String], title: String, body: String)
  : Future[LastError] = {

    val doc = BSONDocument("title" -> title,
      "body" -> body,
      "user" -> user,
      "group" -> group)
    // which results in a Future[List[BSONDocument]]

    collectionDocs
      .save(doc)
  }

  def save(curriDocument: CurriDocument): Future[LastError] = {
    val doc = CurriDocumentWriter.write(curriDocument)
    collectionDocs.save(doc)
  }

}





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

  def findDocs(user: String, maybeGroups: Option[Seq[String]]): Future[List[BSONDocument]] = {
    // find all documents owned by either user ot group
    val docsByUser = collectionDocs.find(makeDoc(user, maybeGroups)).cursor[BSONDocument].collect[List]()
    docsByUser
  }

  def findDoc(user: String, maybeGroups: Option[Seq[String]], id: String): Future[BSONDocument] = {
    // which results in a Future[List[BSONDocument]]

    val query = BSONDocument("age" -> BSONDocument("$gt" -> 27))

    collectionDocs
      .find(makeDoc(user, maybeGroups))
      .cursor[BSONDocument]
      .collect[List]().map(_.head)
  }


  private def makeDoc(user: String, maybeGroups: Option[Seq[String]]) = {

    val queryGroups = maybeGroups match {
      case Some(groups) => Some(BSONDocument("$in" -> groups))
      case None => None
    }
    BSONDocument(
      "ownerUser" -> user,
      "ownerGroup" -> queryGroups
    )
  }

  def save(curriDocument: CurriDocument): Future[LastError] = {
    val doc = CurriDocumentWriter.write(curriDocument)
    collectionDocs.save(doc)
  }

}





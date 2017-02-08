package curri.db


import akka.http.scaladsl.model.StatusCodes
import curri.Config
import curri.docs.domain.{CurriDocument, CurriDocumentWriter}
import curri.http.HttpException
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONString}
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

    collectionDocs
      // .find(makeDoc(user, maybeGroups).add(BSONDocument("_id" -> BSONObjectID(id))))
      .find(BSONDocument("_id" -> BSONObjectID(id)))
      .cursor[BSONDocument]
      .collect[List]().map(_.head)
  }


  private def makeDoc(user: String, maybeGroups: Option[Seq[String]]): BSONDocument = {

    val queryGroups = maybeGroups match {
      case Some(groups) => Some(BSONDocument("$in" -> groups))
      case None => None
    }
    BSONDocument(
      "ownerUser" -> user,
      "ownerGroup" -> queryGroups
    )
  }

  // return the saved document which will have an id
  def save(curriDocument: CurriDocument): Future[String] = {
    val doc = CurriDocumentWriter.write(curriDocument)
    val id = BSONObjectID.generate
    collectionDocs.save(doc.add(BSONDocument("_id" -> id)))
      .map(lastFailure => if (lastFailure.ok) {
        id.stringify
      } else {
        throw new HttpException(StatusCodes.InternalServerError, lastFailure.message)
      })

  }

}





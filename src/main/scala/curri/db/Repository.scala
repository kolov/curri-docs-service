package curri.db


import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Access to the Mongodb repository
  */
object Repository {

  val collectionDocs = connect("docs")

  def connect(name: String): BSONCollection = {

    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))

    val db = connection("curri")
    db.collection(name)
  }

  def findDocs(user: String, group: Option[String]): Future[List[BSONDocument]] = {
    var query = _
    group match {
      case Some(group) => query = BSONDocument("ownerUser" -> user, "ownerGroup" -> group)
      case None => query = BSONDocument("ownerUser" -> user)
    }

    // which results in a Future[List[BSONDocument]]
    collectionDocs
      .find(query)
      .cursor[BSONDocument]
      .collect[List]()
  }

}





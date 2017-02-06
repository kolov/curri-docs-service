package curri.docs.domain

import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}


case class CurriDocument(
                          title: String,
                          body: String,
                          ownerUser: Option[String],
                          ownerGroup: Option[String]) {
  require(ownerUser.isDefined || ownerGroup.isDefined)
}


object CurriDocumentWriter extends BSONDocumentWriter[CurriDocument] {
  def write(curriDoc: CurriDocument): BSONDocument = BSONDocument(
    "title" -> curriDoc.title,
    "body" -> curriDoc.body,
    "ownerUser" -> curriDoc.ownerUser,
    "ownerGroup" -> curriDoc.ownerGroup)
}


object CurriDocumentReader extends BSONDocumentReader[CurriDocument] {
  def read(doc: BSONDocument): CurriDocument = {
    CurriDocument(
      doc.getAs[String]("title").get,
      doc.getAs[String]("body").get,
      doc.getAs[String]("ownerUser"),
      doc.getAs[String]("ownerGroup"))
  }
}
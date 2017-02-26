package curri.docs.domain

import com.fasterxml.jackson.annotation.JsonProperty
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}
 

case class CurriDocument(
                          @JsonProperty("title") title: String,
                          @JsonProperty("kind") kind: String,
                          @JsonProperty("body") body: String,
                          @JsonProperty("ownerUser") ownerUser: Option[String],
                          @JsonProperty("ownerGroup") ownerGroup: Option[String]) {
  def withBody(newBody: String) = CurriDocument(title, kind, newBody, ownerUser, ownerGroup)

}


object CurriDocumentWriter extends BSONDocumentWriter[CurriDocument] {
  def write(curriDoc: CurriDocument): BSONDocument = BSONDocument(
    "title" -> curriDoc.title,
    "kind" -> curriDoc.kind,
    "body" -> curriDoc.body,
    "ownerUser" -> curriDoc.ownerUser,
    "ownerGroup" -> curriDoc.ownerGroup)
}


object CurriDocumentReader extends BSONDocumentReader[CurriDocument] {
  def read(doc: BSONDocument): CurriDocument = {
    CurriDocument(
      doc.getAs[String]("title").get,
      doc.getAs[String]("kind").get,
      doc.getAs[String]("body").get,
      doc.getAs[String]("ownerUser"),
      doc.getAs[String]("ownerGroup"))
  }
}
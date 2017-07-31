package curri.docs.domain

import com.fasterxml.jackson.annotation.JsonProperty
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}


case class CurriDocument(
                          @JsonProperty("title") title: String,
                          @JsonProperty("kind") kind: String,
                          @JsonProperty("body") body: String,
                          @JsonProperty("user") user: Option[String],
                          @JsonProperty("group") group: Option[String]) {
  def withBody(newBody: String) = CurriDocument(title, kind, newBody, user, group)

}

object CurriDocument {

  implicit object CurriDocumentWriter extends BSONDocumentWriter[CurriDocument] {
    def write(curriDoc: CurriDocument): BSONDocument = BSONDocument(
      "title" -> curriDoc.title,
      "kind" -> curriDoc.kind,
      "body" -> curriDoc.body,
      "user" -> curriDoc.user,
      "group" -> curriDoc.group)
  }


  implicit object CurriDocumentReader extends BSONDocumentReader[CurriDocument] {
    def read(doc: BSONDocument): CurriDocument = {
      CurriDocument(
        doc.getAs[String]("title").get,
        doc.getAs[String]("kind").get,
        doc.getAs[String]("body").get,
        doc.getAs[String]("user"),
        doc.getAs[String]("group"))
    }
  }
}
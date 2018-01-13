package curri.docs.domain

import reactivemongo.bson.{BSONDocumentHandler, Macros}

case class CurriDocument(title: String,
                         kind: String,
                         body: String,
                         user: Option[String],
                         group: Option[String]) {
  def withBody(newBody: String) = CurriDocument(title, kind, newBody, user, group)
}


object CurriDocument {
  implicit val bsonCodec: BSONDocumentHandler[CurriDocument] = Macros.handler[CurriDocument]
}
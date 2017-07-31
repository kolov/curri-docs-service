package curri.tools

import curri.db.Repository
import curri.docs.domain.CurriDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DbInitializer {

  def migrate() = {
    for {
      hasDocs <- Repository.hasDocs()
      done <- if (!hasDocs) insertInitialDocuments() else Future {}
    } yield (hasDocs, done)
  }

  private def insertInitialDocuments() = {
    val docsFolder = classOf[CurriDocument]
      .getResource("/documents/resources-root")
      .getFile
      .stripSuffix("/resources-root")

    val loader = new DocumentsLoader
    loader.saveAllDocuments(docsFolder + "/xsd")
  }
}
package curri.tools

import curri.Config
import curri.db.Repository
import curri.docs.domain.CurriDocument


object LoadInitialDocuments extends App with Config {


  override def main(args: Array[String]) = {
    val docsFolder = classOf[CurriDocument]
      .getResource("/documents/resources-root")
      .getFile
      .stripSuffix("/resources-root")

    val loader = new DocumentsLoader
    loader.saveAllDocuments(docsFolder + "/xsd")
    Repository.shutdown()
  }
}
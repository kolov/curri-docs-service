package curri.tools

import curri.Config
import curri.docs.domain.CurriDocument


object LoadInitialDocuments extends App with Config {


  override def main(args: Array[String]) = {
    val docsFolder = classOf[CurriDocument]
      .getResource("/documents/docs.txt")
      .getFile
      .stripSuffix("/docs.txt")

    val loader = new DocumentsLoader
    loader.initDocuments(docsFolder + "/xsd")

  }
}
package curri.tools

import org.scalatest.FunSuite


class DocumentsLoaderTest extends FunSuite {

  test("testLoadDocuments") {
    val loc = classOf[DocumentsLoaderTest]
      .getResource("/documents/xsd/it-professional1.json")
      .getFile
      .stripSuffix("/it-professional1.json")

    val loader = new DocumentsLoader
    val docs = loader.readDocuments(loc)

    assert(docs.size == 1)
    assert(docs(0).title == "first")

  }

}

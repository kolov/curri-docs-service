package curri.tools

import java.io.File

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.OptionModule
import curri.db.Repository
import curri.docs.domain.CurriDocument

import scala.io.Source

class DocumentsLoader {

  val jsonMapper = new ObjectMapper()
  val module = new OptionModule {}
  jsonMapper.registerModule(module)

  def readDocument(folder: String, baseName: String): CurriDocument = {
    val json = Source.fromFile(new File(folder + "/" + baseName + ".json")).mkString
    val readDoc = jsonMapper.readValue(json, classOf[CurriDocument])
    val content = Source.fromFile(new File(folder + "/" + baseName + "." + readDoc.kind)).mkString
    readDoc.withBody(content)
  }

  def storeDocument(doc: CurriDocument) = {
    Repository.save(doc)
  }

  def initDocuments(folder: String) = {
    readDocuments(folder).foreach(storeDocument)
  }

  def readDocuments(folder: String) = {
    val file = new File(folder)
    if (!file.isDirectory || !file.exists()) {
      throw new IllegalArgumentException("No such filder " + folder)
    }
    file.listFiles
      .filter(f => f.isFile & f.getName.endsWith(".json"))
      .map(_.getName)
      .map(n => n.substring(0, n.length - ".json".length))
      .map(readDocument(folder, _))
  }

}

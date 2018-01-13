package curri.tools

import java.io.File

import curri.db.Repository
import curri.docs.domain.CurriDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import io.circe.generic.auto._
import io.circe.java8.time._
import io.circe._
import io.circe.parser.{decode, _}

class DocumentsLoader {

  def readDocument(folder: String, baseName: String): Either[Error, CurriDocument] = {
    val json = Source.fromFile(new File(folder + "/" + baseName + ".json")).mkString
    for {
      readDoc <- decode[CurriDocument](json)
      content = Source.fromFile(new File(folder + "/" + baseName + "." + readDoc.kind)).mkString
      doc = readDoc.withBody(content)
    } yield doc
  }

  def storeDocument(doc: CurriDocument) : Future[String]= {
    Repository.save(doc)
  }

  def saveAllDocuments(folder: String) = {
     Future.sequence(readDocuments(folder).map(storeDocument))
  }

  def documentsPresent = {
    Repository.hasDocs()
  }

  def readDocuments(folder: String) : Seq[CurriDocument] = {
    val file = new File(folder)
    if (!file.isDirectory || !file.exists()) {
      throw new IllegalArgumentException("No such folder " + folder)
    }
    file.listFiles
      .filter(f => f.isFile & f.getName.endsWith(".json"))
      .map(_.getName)
      .map(n => n.substring(0, n.length - ".json".length))
      .map(readDocument(folder, _))
      .map( _.fold( e => throw new Exception(e.toString), d => d))
  }

}

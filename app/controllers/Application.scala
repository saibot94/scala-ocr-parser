package controllers

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

import models.document.{Document, DocumentParser}
import play.api._
import play.api.mvc._
import play.api.libs.json._

import play.api.cache.Cache
import play.api.Play.current
import play.api.db._
import models.ocr.{BoundingBoxDrawer, FeatureDetector, OCRService, TextBuilder}
import models.utils.{ImageCropper, ImageTools}
import models.primitives.{RawImage, Row}

object Application extends Controller {

  def index = Action {
    Ok(views.html.index(null))
  }

  def db = Action {
    var out = ""
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement

      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)")
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())")

      val rs = stmt.executeQuery("SELECT tick FROM ticks")

      while (rs.next) {
        out += "Read from DB: " + rs.getTimestamp("tick") + "\n"
      }
    } finally {
      conn.close()
    }

    Ok(out)
  }


  def upload = Action(parse.multipartFormData) {
    request =>
      request.body.file("picture").map {
        picture =>
          val contentType = picture.contentType
          val conversionResult = ImageTools.preprocessImage(picture.ref.file)
          val imageByteArray = conversionResult._1.toByteArray
          val rawImage: RawImage = conversionResult._2

          println(s"[log] After conversion to byte array, the dimensions are as follows: ${rawImage.data.length}")
          println(s"[log] Width: ${rawImage.width}; Height: ${rawImage.height}")
          //checkPositives(rawImage)
          val parsedDocument = getDocumentFromRawImage(rawImage)
          val boundingBoxImage = getBoundingBoxImage(imageByteArray, parsedDocument)
          val text = getTextFromImage(imageByteArray, parsedDocument)
          println("[log] Done parsing text")
          println(s"[log] The resulting text is: ${System.lineSeparator()} $text")
          val jsResult: JsValue = JsObject(Seq(
            "text" -> JsString(text),
            "image" -> JsString(ImageTools.imageToBase64(boundingBoxImage.toByteArray))
          ))
          Ok(jsResult)
      }.getOrElse {
        Redirect(routes.Application.index).flashing("error" -> "Missing file")
      }
  }

  private def getBoundingBoxImage(imageBytes: Array[Byte], document: Document): ByteArrayOutputStream = {
    //printBoundingBoxes(rowsAndBoxes)
    BoundingBoxDrawer(imageBytes).getBoundingBoxesImage(document)
  }

  private def getTextFromImage(imageByteArray: Array[Byte], parsedDocument: Document): String = {
    val textBuilder = TextBuilder(imageByteArray, parsedDocument, Play.getFile("conf/resources").getAbsolutePath)
    val text = textBuilder.getText
    val sb = new StringBuilder
    text foreach {
      row =>
        row.foreach {
          word =>
            sb ++= word.replace("\n", "").replace("\r", "")
            sb += ' '
        }
        sb ++= System.lineSeparator()
    }
    sb.mkString
  }

  private def getDocumentFromRawImage(rawImage: RawImage): Document = {
    val rows = OCRService().identifyLinesAndCharacters(rawImage)
    DocumentParser.parseImageToDocument(rows)
  }

  //  private def printBoundingBoxes(rowsAndBoxes: List[Row]): Unit = {
  //    println(s"${rowsAndBoxes.length} rows found")
  //    rowsAndBoxes.foreach(
  //      row =>
  //        row.characterBoundingBoxes.foreach(
  //          box =>
  //            println(s"box: ${box.leftUpX}, ${box.leftUpY}, ${box.lowerRightX}, ${box.lowerRightY}")
  //
  //        )
  //    )
  //  }

  //  private def checkPositives(rawImage: RawImage): Unit = {
  //    println(s"[log] Checking where characters may exist: ")
  //    var c = 0
  //    for (i <- rawImage.data.indices) {
  //      for (j <- rawImage.data(i).indices) {
  //        if (rawImage.data(i)(j) == 0) {
  //          println(s"found on pos: $i,$j, value: ${rawImage.data(i)(j)}; ${rawImage.data(i)(j)};")
  //          c += 1
  //          if (c == 100) {
  //            return
  //          }
  //        }
  //      }
  //    }
  //  }
}

package controllers

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

import models.config.AppConfig
import models.document.{Document, DocumentParser}
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Environment
import play.api.cache.Cache
import play.api.Play.current
import play.api.db._
import models.ocr.{BoundingBoxDrawer, FeatureDetector, OCRService, TextBuilder}
import models.utils.{DrawingOptions, ImageCropper, ImageTools, TimeTools}
import models.primitives.{RawImage, Row}
import play.api.libs.Files.TemporaryFile

object Application extends Controller {


  def index = Action {
    Ok(views.html.index(null, null))
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


  def getDrawingOptions(request: Request[MultipartFormData[TemporaryFile]]): DrawingOptions = {
    val urlForm = request.body.asFormUrlEncoded
    val drawChars = urlForm.get("drawchar").map(_.head).getOrElse("false").toBoolean
    val drawWords = urlForm.get("drawword").map(_.head).getOrElse("false").toBoolean
    val drawRows = urlForm.get("drawrow").map(_.head).getOrElse("false").toBoolean
    val renderContent = urlForm.get("renderContent").map(_.head).orNull
    DrawingOptions(drawChars, drawWords, drawRows, renderContent)
  }

  def upload = Action(parse.multipartFormData) {
    request =>
      request.body.file("picture").map {
        picture =>
          val drawingOptions = getDrawingOptions(request)


          val conversionResult = ImageTools.preprocessImage(picture.ref.file)
          val imageByteArray = conversionResult._1.toByteArray
          val rawImage: RawImage = conversionResult._2

          println(s"[log] After conversion to byte array, the dimensions are as follows: ${rawImage.data.length}")
          println(s"[log] Width: ${rawImage.width}; Height: ${rawImage.height}")
          println(s"[log] Drawing config: drawchar: ${drawingOptions.drawChars}; drawword: ${drawingOptions.drawWords}; drawrow: ${drawingOptions.drawRows}, renderContent: ${drawingOptions.renderContent}")


          val parsedDocument = getDocumentFromRawImage(rawImage)
          val boundingBoxImage = getBoundingBoxImage(imageByteArray, parsedDocument, drawingOptions)
          val text = TimeTools.time("Text extraction", {
            getTextFromImage(imageByteArray, parsedDocument)
          })
          println("[log] Done parsing text")
          getResult(drawingOptions, boundingBoxImage, text)
      }.getOrElse {
        Redirect(routes.Application.index).flashing("error" -> "Missing file")
      }
  }

  def getResult(drawingOptions: DrawingOptions, boundingBoxImage: ByteArrayOutputStream, text: String): Result = {
    val base64Image = ImageTools.imageToBase64(boundingBoxImage.toByteArray)
    if (drawingOptions.renderContent == null) {
      val jsResult: JsValue = JsObject(Seq(
        "text" -> JsString(text),
        "image" -> JsString(base64Image)
      ))
      Ok(jsResult)
    }
    else {
      Ok(views.html.index(text, base64Image))
    }
  }

  private def getBoundingBoxImage(imageBytes: Array[Byte], document: Document, drawingOptions: DrawingOptions): ByteArrayOutputStream = {
    //printBoundingBoxes(rowsAndBoxes)
    BoundingBoxDrawer(imageBytes, drawingOptions).getBoundingBoxesImage(document)
  }

  private def getTextFromImage(imageByteArray: Array[Byte], parsedDocument: Document): String = {
    val modelFilePath = Play.getFile(AppConfig.resourcesFolder).getCanonicalPath
    println(s"[log] Model file path is: $modelFilePath")
    val textBuilder = TextBuilder(imageByteArray, parsedDocument, modelFilePath)
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

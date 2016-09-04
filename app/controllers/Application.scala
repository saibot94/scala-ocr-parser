package controllers

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import play.api.db._

import models.ocr.{BoundingBoxDrawer, OCRService, FeatureDetector}
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
          val filename = picture.filename
          val contentType = picture.contentType
          val conversionResult = ImageTools.preprocessImage(picture.ref.file)
          val imageByteArray = conversionResult._1.toByteArray
          val rawImage: RawImage = conversionResult._2

          println(s"[log] After conversion to byte array, the dimensions are as follows: ${rawImage.data.length}")
          println(s"[log] Width: ${rawImage.width}; Height: ${rawImage.height}")
          //checkPositives(rawImage)
          val rowsAndBoxes = (new OCRService).identifyLinesAndCharacters(rawImage)
          val boundingBoxImage = getBoundingBoxImage(imageByteArray, rowsAndBoxes)
          val croppedCharacters = ImageCropper.cropImage(imageByteArray, rowsAndBoxes.head.characterBoundingBoxes)
          val detector = FeatureDetector.apply
          croppedCharacters.foreach(character => detector.detectFeatures(character))


          val outputStream = new ByteArrayOutputStream()
          ImageIO.write(croppedCharacters.head, "jpeg", outputStream)

          Ok(
            boundingBoxImage.toByteArray
          ).as(contentType.getOrElse("image/jpeg"))
      }.getOrElse {
        Redirect(routes.Application.index).flashing("error" -> "Missing file")
      }
  }

  private def getBoundingBoxImage(imageBytes: Array[Byte], rowsAndBoxes: List[Row]): ByteArrayOutputStream = {
    //printBoundingBoxes(rowsAndBoxes)
    BoundingBoxDrawer(imageBytes).getBoundingBoxesImage(rowsAndBoxes)
  }

  private def printBoundingBoxes(rowsAndBoxes: List[Row]): Unit = {
    println(s"${rowsAndBoxes.length} rows found")
    rowsAndBoxes.foreach(
      row =>
        row.characterBoundingBoxes.foreach(
          box =>
            println(s"box: ${box.leftUpX}, ${box.leftUpY}, ${box.lowerRightX}, ${box.lowerRightY}")

        )
    )
  }

  private def checkPositives(rawImage: RawImage): Unit = {
    println(s"[log] Checking where characters may exist: ")
    var c = 0
    for (i <- rawImage.data.indices) {
      for (j <- rawImage.data(i).indices) {
        if (rawImage.data(i)(j) == 0) {
          println(s"found on pos: $i,$j, value: ${rawImage.data(i)(j)}; ${rawImage.data(i)(j)};")
          c += 1
          if (c == 100) {
            return
          }
        }
      }
    }
  }
}

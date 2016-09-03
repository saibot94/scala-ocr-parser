package controllers

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import play.api.db._
import javax.measure.unit.SI.KILOGRAM

import models.config.AppConfig
import models.ocr.{BoundingBoxDrawer, OCRService}
import models.utils.ImageTools
import org.jscience.physics.model.RelativisticModel
import org.jscience.physics.amount.Amount
import models.primitives.{RawImage, Row}

object Application extends Controller {

  def index = Action {
    RelativisticModel.select()
    val energy = AppConfig.energy

    val m = Amount.valueOf(energy).to(KILOGRAM)
    val testRelativity = s"E=mc^2: $energy = $m"

    Ok(views.html.index(testRelativity))
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
          val rawImage: RawImage = conversionResult._2

          println(s"[log] After conversion to byte array, the dimensions are as follows: ${rawImage.data.length}")
          println(s"[log] Width: ${rawImage.width}; Height: ${rawImage.height}")
          //checkPositives(rawImage)
          val boundingBoxImage = getBoundingBoxImage(conversionResult._1.toByteArray, rawImage)

          Ok(
            boundingBoxImage.toByteArray
          ).as(contentType.getOrElse("image/jpeg"))
      }.getOrElse {
        Redirect(routes.Application.index).flashing("error" -> "Missing file")
      }
  }

  private def getBoundingBoxImage(imageBytes: Array[Byte], rawImage: RawImage): ByteArrayOutputStream = {
    val rowsAndBoxes = (new OCRService).identifyLinesAndCharacters(rawImage)
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

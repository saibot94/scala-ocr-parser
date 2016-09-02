package controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import play.api.db._
import javax.measure.unit.SI.KILOGRAM

import models.utils.ImageCleaner
import org.jscience.physics.model.RelativisticModel
import org.jscience.physics.amount.Amount

import models.primitives.RawImage

object Application extends Controller {

  def index = Action {
    RelativisticModel.select()
    val energy = scala.util.Properties.envOrElse("ENERGY", "12 GeV")

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
          val conversionResult = ImageCleaner.convertToBlackAndWhite(picture.ref.file)
          val rawImage: RawImage = conversionResult._2

          println(s"[log] After conversion to byte array, the dimensions are as follows: ${rawImage.data.length}")
          println(s"[log] Width: ${rawImage.width}; Height: ${rawImage.height}")
          //checkPositives(rawImage)

          Ok.sendFile(
            content = conversionResult._1,
            fileName = _ => filename
          ).as(contentType.getOrElse("image/jpeg"))
      }.getOrElse {
        Redirect(routes.Application.index).flashing("error" -> "Missing file")
      }
  }

  private def checkPositives(rawImage: RawImage): Unit = {
    println(s"[log] Checking where characters may exist: ")
    var c = 0
    for (i <- rawImage.data.indices) {
      if (rawImage.data(i).toInt != 0) {
        println(s"found on pos: $i, value: ${rawImage.data(i).toInt}; ${rawImage.data(i)}; Binary rep: ${Integer.toBinaryString(rawImage.data(i))}")
        c += 1
        if (c == 100) {
          return
        }
      }
    }
  }
}

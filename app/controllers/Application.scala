package controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import play.api.db._
import javax.measure.unit.SI.KILOGRAM

import org.jscience.physics.model.RelativisticModel
import org.jscience.physics.amount.Amount

import scala.util.Random

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

          Ok.sendFile(
            content = models.ImageCleaner.convertToBlackAndWhite(picture.ref.file),
            fileName = _ => filename
          ).as(contentType.getOrElse("image/jpeg"))
      }.getOrElse {
        Redirect(routes.Application.index).flashing("error" -> "Missing file")
      }
  }
}

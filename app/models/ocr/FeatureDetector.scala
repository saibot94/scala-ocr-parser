package models.ocr

import scala.sys.process._
import java.awt.image.BufferedImage
import java.io.File
import java.util.UUID
import javax.imageio.ImageIO

import models.config.AppConfig
import net.sourceforge.tess4j.{ITesseract, Tesseract, Tesseract1}


/**
  * Created by darkg on 03-Sep-16.
  */
class FeatureDetector(dataPath: String) {

  val instance: ITesseract = new Tesseract()
  val useCommandLine = AppConfig.useCommandLineTesseract

  instance.setDatapath(dataPath)
  instance.setLanguage("eng")


  def doCommandLineTesseract(w: BufferedImage): String = {
    val file = File.createTempFile(UUID.randomUUID().toString, ".jpeg")
    val filePath = file.getCanonicalPath
    val command = s"tesseract --tessdata-dir $dataPath $filePath stdout -l eng"
    if (ImageIO.write(w, "jpeg", file)) {
      val resString = command.!!
      file.delete()
      resString
    } else {
      ""
    }
  }

  def detectFeatures(words: List[BufferedImage]): List[String] = {
    useCommandLine match {
      case false =>
        words.map {
          w =>
            instance.doOCR(w)
        }
      case true =>
        words.map {
          w =>
            doCommandLineTesseract(w)
        }
    }
  }

}

object FeatureDetector {
  def apply(dataPath: String): FeatureDetector = new FeatureDetector(dataPath)
}

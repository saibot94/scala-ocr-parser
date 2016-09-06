package models.ocr

import scala.sys.process._
import java.awt.image.BufferedImage
import java.io.File
import java.util.UUID
import javax.imageio.ImageIO

import models.config.AppConfig
import net.sourceforge.tess4j.{ITesseract, Tesseract, Tesseract1}


import scala.concurrent._
import scala.concurrent.duration.Duration
import ExecutionContext.Implicits.global

/**
  * Created by darkg on 03-Sep-16.
  */
class FeatureDetector(dataPath: String) {

  val useCommandLine = AppConfig.useCommandLineTesseract
  if (useCommandLine) {
    println("[log] Using the command line version of Tesseract")
  }

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

  def doSimpleTesseract(w: BufferedImage): String = {
    val instance: ITesseract = new Tesseract1()

    instance.setDatapath(dataPath)
    instance.setLanguage("eng")

    instance.doOCR(w)
  }

  def detectFeatures(words: List[BufferedImage]): Future[List[String]] = {

    useCommandLine match {
      case false =>
        Future.traverse(words) {
          w =>
            Future(doSimpleTesseract(w))
        }
      case true =>
        Future.traverse(words) {
          w =>
            Future(doCommandLineTesseract(w))
        }

    }
  }

}

object FeatureDetector {
  def apply(dataPath: String): FeatureDetector = new FeatureDetector(dataPath)
}

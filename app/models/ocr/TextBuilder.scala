package models.ocr

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

import models.document.Document
import models.utils.ImageCropper

import scala.concurrent._
import scala.concurrent.duration.Duration
import ExecutionContext.Implicits.global

/**
  * Created by darkg on 04-Sep-16.
  */
class TextBuilder(imageBytes: Array[Byte], document: Document, dataPath: String) {
  val image = ImageIO.read(new ByteArrayInputStream(imageBytes))
  val featureDetector = FeatureDetector(dataPath)

  def getText: List[List[String]] = {
    println("[log] Started parsing text from word image croppings")
    val documentFuture = Future.traverse(document.rows) {
      row =>
        val imageFuture = Future.traverse(row.words) {
          parsedWord =>
            if (parsedWord.wordBoundingBox.isDefined) {
              Future(ImageCropper.cropBoundingBox(image, parsedWord.wordBoundingBox.get))
            }
            else {
              Future(None)
            }
        }
        val images = Await.result(imageFuture, Duration.Inf).filter(img => img.isDefined).map(img => img.get)
        Future(featureDetector.detectFeatures(images))
    }
    Await.result(documentFuture, Duration.Inf)
  }
}

object TextBuilder {
  def apply(imageBytes: Array[Byte], document: Document, dataPath: String): TextBuilder = new TextBuilder(imageBytes, document, dataPath)
}

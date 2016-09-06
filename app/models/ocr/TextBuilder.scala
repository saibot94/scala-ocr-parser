package models.ocr

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

import models.document.Document
import models.utils.ImageCropper


/**
  * Created by darkg on 04-Sep-16.
  */
class TextBuilder(imageBytes: Array[Byte], document: Document, dataPath: String) {
  val image = ImageIO.read(new ByteArrayInputStream(imageBytes))
  val featureDetector = FeatureDetector(dataPath)

  def getText: List[List[String]] = {
    println("[log] Started parsing text from word image croppings")
    document.rows map {
      row =>
        val rowBufferedImages = row.words.map {
          parsedWord =>
            if (parsedWord.wordBoundingBox.isDefined) {
              ImageCropper.cropBoundingBox(image, parsedWord.wordBoundingBox.get)
            } else {
              None
            }
        }.filter(img => img.isDefined).map(img => img.get)
        featureDetector.detectFeatures(rowBufferedImages)
    }
  }
}

object TextBuilder {
  def apply(imageBytes: Array[Byte], document: Document, dataPath: String): TextBuilder = new TextBuilder(imageBytes, document, dataPath)
}

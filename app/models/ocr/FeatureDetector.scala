package models.ocr

import java.awt.image.BufferedImage

import net.sourceforge.tess4j.{ITesseract, Tesseract, Tesseract1}


/**
  * Created by darkg on 03-Sep-16.
  */
class FeatureDetector(dataPath: String) {

  //val instance: ITesseract = new Tesseract()
  val instance: Tesseract1 = new Tesseract1()

  instance.setDatapath(dataPath)
  instance.setLanguage("eng")


  def detectFeatures(words: List[BufferedImage]): List[String] = {
    words.map {
      w =>
        instance.doOCR(w)
    }
  }

}

object FeatureDetector {
  def apply(dataPath: String): FeatureDetector = new FeatureDetector(dataPath)
}

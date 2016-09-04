package models.ocr

import java.awt.image.BufferedImage

import boofcv.abst.feature.detdesc.DetectDescribePoint
import boofcv.abst.feature.detect.interest.ConfigFastHessian
import boofcv.factory.feature.detdesc.FactoryDetectDescribe
import boofcv.io.image.{ConvertBufferedImage, UtilImageIO}
import boofcv.struct.feature.BrightFeature
import boofcv.struct.image.{GrayF32, GrayU8}
import models.document.Document
import net.sourceforge.tess4j.{ITesseract, Tesseract, Tesseract1}


/**
  * Created by darkg on 03-Sep-16.
  */
class FeatureDetector(dataPath: String) {
//  val surf: DetectDescribePoint[GrayU8, BrightFeature] = FactoryDetectDescribe.
//    surfStable(new ConfigFastHessian(0, 2, 200, 2, 9, 4, 4), null, null, classOf[GrayU8])
  val instance: ITesseract = new Tesseract()

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

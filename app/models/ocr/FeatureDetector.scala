package models.ocr

import java.awt.image.BufferedImage

import boofcv.abst.feature.detdesc.DetectDescribePoint
import boofcv.abst.feature.detect.interest.ConfigFastHessian
import boofcv.factory.feature.detdesc.FactoryDetectDescribe
import boofcv.io.image.{ConvertBufferedImage, UtilImageIO}
import boofcv.struct.feature.BrightFeature
import boofcv.struct.image.{GrayF32, GrayU8}
import net.sourceforge.tess4j.{ITesseract, Tesseract1, Tesseract}


/**
  * Created by darkg on 03-Sep-16.
  */
class FeatureDetector(dataPath : String) {
  val surf: DetectDescribePoint[GrayU8, BrightFeature] = FactoryDetectDescribe.
    surfStable(new ConfigFastHessian(0, 2, 200, 2, 9, 4, 4), null, null, classOf[GrayU8])
  val instance: ITesseract = new Tesseract()

  instance.setDatapath(dataPath)
  instance.setLanguage("eng")


  def detectFeatures(imageToDetect: BufferedImage): List[Array[Double]] = {
    val gray = ConvertBufferedImage.convertFromSingle(imageToDetect, null, classOf[GrayU8])

//    surf.detect(gray)
//    println(s"Found features: ${surf.getNumberOfFeatures}")
//    if (surf.getNumberOfFeatures > 0) {
//      println(s"First descriptor's first value: + ${surf.getDescription(0).value(0)}")
//    }
//    else {
//      println("No features found for this particular character...")
//    }

    val character = instance.doOCR(imageToDetect)
    println(s"Found character: $character")
    List(Array[Double]())
  }

}

object FeatureDetector {
  def apply(dataPath: String): FeatureDetector = new FeatureDetector(dataPath)
}

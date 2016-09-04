package models.ocr

import java.awt.image.BufferedImage

import scala.collection.JavaConverters._
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

import boofcv.abst.feature.detdesc.DetectDescribePoint
import boofcv.abst.feature.detect.interest.ConfigFastHessian
import boofcv.factory.feature.detdesc.FactoryDetectDescribe
import boofcv.io.image.{ConvertBufferedImage, UtilImageIO}
import boofcv.struct.feature.BrightFeature
import boofcv.struct.image.{GrayF32, GrayU8}
import de.lmu.ifi.dbs.jfeaturelib.features.SURF
import ij.process.{ByteProcessor, ColorProcessor}

/**
  * Created by darkg on 03-Sep-16.
  */
class FeatureDetector {
  private val descriptor = new SURF()

  def detectFeatures(imageToDetect: Array[Byte]): List[Array[Double]] = {
    val inputStream = new ByteArrayInputStream(imageToDetect)
    val imageProcessor = new ByteProcessor(ImageIO.read(inputStream))
    descriptor.run(imageProcessor)
    descriptor.getFeatures.asScala.toList
  }

  def detectFeatures(imageToDetect: BufferedImage): List[Array[Double]] = {
    val gray = ConvertBufferedImage.convertFromSingle(imageToDetect, null, classOf[GrayU8])
    val surf: DetectDescribePoint[GrayU8, BrightFeature] = FactoryDetectDescribe.
      surfStable(new ConfigFastHessian(0, 2, 200, 2, 9, 4, 4), null, null, classOf[GrayU8])

    surf.detect(gray)
    println(s"Found features: ${surf.getNumberOfFeatures}")
    if (surf.getNumberOfFeatures > 0) {
      println(s"First descriptor's first value: + ${surf.getDescription(0).value(0)}")
    }
    else {
      println("No features found for this particular character...")
    }


    List(Array[Double]())
  }

}

object FeatureDetector {
  def apply: FeatureDetector = new FeatureDetector()
}

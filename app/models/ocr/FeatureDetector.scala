package models.ocr

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

import de.lmu.ifi.dbs.jfeaturelib.features.SURF
import ij.process.{ByteProcessor, ColorProcessor}

/**
  * Created by darkg on 03-Sep-16.
  */
class FeatureDetector {
  private val descriptor = new SURF()

  def detectFeatures(imageToDetect: Array[Byte]): Array[Array[Double]] = {
    val inputStream = new ByteArrayInputStream(imageToDetect)
    val imageProcessor = new ByteProcessor(ImageIO.read(inputStream))
    descriptor.run(imageProcessor)
    descriptor.getFeatures.toArray.asInstanceOf[Array[Array[Double]]]
  }

  //Detector.fastHessian(new IntegralImage())
}

object FeatureDetector {
  def apply: FeatureDetector = new FeatureDetector()
}

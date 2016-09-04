package models.ocr

import java.awt.image.BufferedImage
import scala.collection.JavaConverters._

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

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
    val imageProcessor = new ColorProcessor(imageToDetect)
    descriptor.run(imageProcessor)
    val features = descriptor.getFeatures
    println(s"Feature len: ${features.size()}")
    features.asScala.toList
  }

  //Detector.fastHessian(new IntegralImage())
}

object FeatureDetector {
  def apply: FeatureDetector = new FeatureDetector()
}

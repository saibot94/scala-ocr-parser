package models.utils

import java.awt.geom.AffineTransform
import java.awt.{Graphics2D, Image}
import java.awt.image.{BufferedImage, ConvolveOp, DataBufferByte, RescaleOp}
import java.io.File
import javax.imageio.ImageIO

import models.primitives.RawImage

import scala.util.Random

/**
  * Created by darkg on 01-Sep-16.
  */

// This class takes the image from a request and returns a high-contrast, black and white image.
object ImageCleaner {

  val xScaleSize = Integer.valueOf(scala.util.Properties.envOrElse("XSCALE_RESIZE", "1920"))
  val yScaleSize = Integer.valueOf(scala.util.Properties.envOrElse("YSCALE_RESIZE", "1080"))

  def createBlackAndWhiteImage(image: BufferedImage, imageType: Int) = {
    val blackAndWhiteImage = new BufferedImage(image.getWidth,
      image.getHeight,
      imageType)
    val graphics = blackAndWhiteImage.createGraphics()
    graphics.drawImage(image, 0, 0, null)
    blackAndWhiteImage
  }

  def convertToBlackAndWhite(image: File, imageFormat: String = "jpeg", brightenFactor: Float = 1.3f): (File, RawImage) = {
    // Preprocess image
    val preprocessedImage = convolutionOp(brightenAndIncreaseContrast(ImageIO.read(image), brightenFactor))
    // Next resize the image
    val resizedPreprocessedImage = resizeImage(xScaleSize, yScaleSize, preprocessedImage)
    // Create b&w image
    val blackAndWhiteImage = createBlackAndWhiteImage(resizedPreprocessedImage, BufferedImage.TYPE_BYTE_BINARY)
    // Write the image to a file, so we serve it back to the user afterwards
    val newTempImage = File.createTempFile("test" + Random.nextString(10), s".$imageFormat")
    ImageIO.write(blackAndWhiteImage, s"$imageFormat", newTempImage)

    (newTempImage, extractPixelsFromImage(blackAndWhiteImage))
  }

  private def brightenAndIncreaseContrast(img: BufferedImage, brightenFactor: Float): BufferedImage = {
    val rescale = new RescaleOp(brightenFactor, 1.2f, null)
    rescale.filter(img, img)
  }

  private def convolutionOp(img: BufferedImage): BufferedImage = {
    val sharpen: Array[Float] = Array[Float](
      0.0f, 0.1f, 0.0f,
      0.1f, 0.6f, 0.1f,
      0.0f, 0.6f, 0.0f)
    val kernel = new java.awt.image.Kernel(3, 3, sharpen)
    val convolution = new ConvolveOp(kernel)
    convolution.filter(img, null)
  }

  private def extractPixelsFromImage(image: BufferedImage): RawImage = {
    val databuf = image.getRaster.getDataBuffer
    var byteArray: Array[Byte] = null

    databuf match {
      case databufbyte: DataBufferByte => byteArray = databufbyte.getData
      case _ => println("[ERROR] Unexpected datatype!")
    }

    RawImage(image.getWidth, image.getHeight, byteArray)
  }

  private def toBufferedImage(img: Image, imageType: Int = BufferedImage.TYPE_BYTE_BINARY): BufferedImage = {
    img match {
      case image: BufferedImage =>
        return image;
      case _ =>
    }
    // Create a buffered image with transparency
    val bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), imageType)
    // Draw the image on to the buffered image
    val bGr: Graphics2D = bimage.createGraphics()
    bGr.drawImage(img, 0, 0, null)
    bGr.dispose()
    // Return the buffered image
    bimage
  }

  private def resizeImage(width: Int, height: Int, sourceImage: BufferedImage): BufferedImage = {
    val xScale = Math.min(width, sourceImage.getWidth)
    val yScale = Math.min(height, sourceImage.getHeight)

    val scaledImage = sourceImage.getScaledInstance(xScale, yScale, Image.SCALE_SMOOTH)
    val resizedImage = new BufferedImage(xScale, yScale, sourceImage.getType)
    resizedImage.createGraphics.drawImage(scaledImage, 0, 0, null)
    resizedImage
  }
}

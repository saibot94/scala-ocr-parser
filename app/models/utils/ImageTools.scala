package models.utils

import java.awt.{Graphics2D, Image, RenderingHints}
import java.awt.image._
import java.io.{ByteArrayOutputStream, File}
import javax.imageio.ImageIO

import models.config.AppConfig
import models.primitives.RawImage

/**
  * Created by darkg on 01-Sep-16.
  */

// This class takes the image from a request and returns a high-contrast, black and white image.
object ImageTools {

  def createNewImageType(image: BufferedImage, imageType: Int) = {
    val blackAndWhiteImage = new BufferedImage(image.getWidth,
      image.getHeight,
      imageType)
    val graphics = blackAndWhiteImage.createGraphics()
    graphics.drawImage(image, 0, 0, null)
    blackAndWhiteImage
  }

  def preprocessImage(image: File,
                      imageFormat: String = "jpeg",
                      brightenFactor: Float = AppConfig.blackAndWhiteFactor,
                      brightenOffset: Float = AppConfig.brightenOffset): (ByteArrayOutputStream, RawImage) = {
    // Pre-process image
    val preprocessedImage = brightenAndIncreaseContrast(
      convolutionOp(ImageIO.read(image)),
      brightenFactor,
      brightenOffset)
    // Next resize the image
    val resizedPreprocessedImage = resizeImage(AppConfig.xScaleSize, AppConfig.yScaleSize, preprocessedImage)
    // Create b&w image
    val blackAndWhiteImage = createNewImageType(resizedPreprocessedImage, BufferedImage.TYPE_BYTE_BINARY)
    // Write the image to a file, so we serve it back to the user afterwards
    val outputStream = new ByteArrayOutputStream()
    ImageIO.write(blackAndWhiteImage, s"$imageFormat", outputStream)

    (outputStream, extractPixelsFromImage(blackAndWhiteImage))
  }


  private def brightenAndIncreaseContrast(img: BufferedImage, brightenFactor: Float, offset: Float): BufferedImage = {
    val rescale = new RescaleOp(brightenFactor, offset, null)
    rescale.filter(img, img)
  }

  private def convolutionOp(img: BufferedImage): BufferedImage = {
    val blur: Array[Float] = Array[Float](
      0.0f, 0.1f, 0.0f,
      0.1f, 0.6f, 0.1f,
      0.0f, 0.6f, 0.0f)
    val kernel = new java.awt.image.Kernel(3, 3, blur)
    val convolution = new ConvolveOp(kernel)
    convolution.filter(img, null)
  }

  private def extractPixelsFromImage(image: BufferedImage): RawImage = {
    val pixelArray: Array[Int] = image.getRaster.getPixels(0, 0, image.getWidth, image.getHeight, null)
    val byteArray: Array[Array[Byte]] = Array.ofDim[Byte](image.getHeight, image.getWidth)
    var row = 0
    var col = 0
    pixelArray.foreach(pixel => {
      if (pixel == 0) {
        byteArray(row)(col) = 1
      } else {
        byteArray(row)(col) = 0
      }
      col += 1
      if (col == image.getWidth) {
        col = 0
        row += 1
      }
    })
    RawImage(image.getWidth, image.getHeight, byteArray)
  }

  def toBufferedImage(img: Image, imageType: Int = BufferedImage.TYPE_BYTE_BINARY): BufferedImage = {
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

package models

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util.UUID
import javax.imageio.ImageIO

import scala.util.Random

/**
  * Created by darkg on 01-Sep-16.
  */

// This class takes the image from a request and returns a high-contrast, black and white image.
object ImageCleaner {
  def convertToBlackAndWhite(image: File, imageFormat: String = "jpeg"): File = {
    val img = ImageIO.read(image)
    val blackAndWhiteImage = new BufferedImage(img.getWidth, img.getHeight, BufferedImage.TYPE_BYTE_BINARY)
    val graphics = blackAndWhiteImage.createGraphics()
    graphics.drawImage(img, 0, 0, null)

    val newTempImage = File.createTempFile("test" + Random.nextString(10), s".$imageFormat")
    ImageIO.write(blackAndWhiteImage, s"$imageFormat", newTempImage)
    newTempImage
  }
}

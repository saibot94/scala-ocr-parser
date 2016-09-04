package models.utils

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import javax.swing.ImageIcon

import models.primitives.BoundingBox

import scala.collection.mutable.ListBuffer

/**
  * Created by darkg on 03-Sep-16.
  */
object ImageCropper {
  def cropImage(imageBytes: Array[Byte], boundingBox: BoundingBox): BufferedImage = {
    val inputStream = new ByteArrayInputStream(imageBytes)
    val image = ImageIO.read(inputStream)
    cropBoundingBox(image, boundingBox).orNull
  }

  def cropImage(imageBytes: Array[Byte], boundingBoxes: List[BoundingBox]): List[BufferedImage] = {
    var crops = ListBuffer[BufferedImage]()
    val inputStream = new ByteArrayInputStream(imageBytes)
    val sourceImage = ImageIO.read(inputStream)
    boundingBoxes.map {
      bb => {
        cropBoundingBox(sourceImage, bb)
      }
    }.filter(p => p.isDefined).map(bi => bi.get)
  }

  def cropBoundingBox(image: BufferedImage, boundingBox: BoundingBox): Option[BufferedImage] = {
    val width = boundingBox.lowerRightX - boundingBox.leftUpX
    val height = boundingBox.lowerRightY - boundingBox.leftUpY
    if (width != 0 && height != 0) {
      Some(
        image.getSubimage(boundingBox.leftUpX,
          boundingBox.leftUpY,
          width,
          height)
      )
    } else {
      None
    }
  }

}

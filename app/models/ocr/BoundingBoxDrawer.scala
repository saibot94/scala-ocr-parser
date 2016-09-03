package models.ocr

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.imageio.ImageIO

import models.primitives.Row
import models.utils.ImageTools

/**
  * Created by darkg on 03-Sep-16.
  */
class BoundingBoxDrawer(imageBytes: Array[Byte]) {
  def getBoundingBoxesImage(rowList: List[Row]): ByteArrayOutputStream = {
    val inputStream = new ByteArrayInputStream(this.imageBytes)
    val bufferedImageConverted = ImageIO.read(inputStream)
    val outputStream = new ByteArrayOutputStream()
    if (bufferedImageConverted != null) {
      val colorImage = ImageTools.createNewImageType(bufferedImageConverted, BufferedImage.TYPE_INT_RGB)
      this.drawingProcedure(colorImage, rowList)
      ImageIO.write(colorImage, "jpeg", outputStream)
    }

    outputStream
  }

  private def drawingProcedure(image: BufferedImage,
                               rowList: List[Row]): BufferedImage = {
    val graphics = image.createGraphics()
    graphics.setColor(java.awt.Color.RED)

    rowList.foreach(
      row =>
        row.boundingBoxes.foreach(
          box =>
            graphics.drawRect(box.leftUpX, box.leftUpY, box.lowerRightX - box.leftUpX, box.lowerRightY - box.leftUpY)
        )
    )
    image
  }

}

object BoundingBoxDrawer {
  def apply(imageBytes: Array[Byte]): BoundingBoxDrawer = new BoundingBoxDrawer(imageBytes)
}


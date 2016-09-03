package models.ocr

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.imageio.ImageIO

import models.config.AppConfig
import models.primitives.{BoundingBox, Row}
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

    val extra = AppConfig.boundingBoxExtraSpace
    var i = 0
    rowList.foreach(
      row => {
        graphics.setColor(java.awt.Color.RED)
        row.characterBoundingBoxes.foreach(
          box =>
            graphics.drawRect(box.leftUpX - extra, box.leftUpY - extra, box.lowerRightX - box.leftUpX + extra, box.lowerRightY - box.leftUpY + extra)
        )
        // Draw Row bounding box
        if (row.rowBoundingBox.isDefined) {
          drawRowBoundingBox(graphics, row.rowBoundingBox.get, i)
          i += 1
        }
      })
    image
  }

  private def drawRowBoundingBox(graphics: Graphics2D, rowBB: BoundingBox, rowNum: Int): Unit = {
    graphics.setColor(java.awt.Color.BLUE)
    graphics.drawString(s"Row $rowNum", rowBB.leftUpX - 5, rowBB.leftUpY - 5)
    graphics.drawRect(rowBB.leftUpX,
      rowBB.leftUpY,
      rowBB.lowerRightX - rowBB.leftUpX,
      rowBB.lowerRightY - rowBB.leftUpY)
  }
}

object BoundingBoxDrawer {
  def apply(imageBytes: Array[Byte]): BoundingBoxDrawer = new BoundingBoxDrawer(imageBytes)
}


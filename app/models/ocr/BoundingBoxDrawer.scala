package models.ocr

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.imageio.ImageIO

import models.document.{Document, ParsedRow, ParsedWord}
import models.primitives.BoundingBox
import models.utils.ImageTools

/**
  * Created by darkg on 03-Sep-16.
  */
class BoundingBoxDrawer(imageBytes: Array[Byte]) {
  def getBoundingBoxesImage(document: Document): ByteArrayOutputStream = {
    val inputStream = new ByteArrayInputStream(this.imageBytes)
    val bufferedImageConverted = ImageIO.read(inputStream)
    val outputStream = new ByteArrayOutputStream()
    if (bufferedImageConverted != null) {
      val colorImage = ImageTools.createNewImageType(bufferedImageConverted, BufferedImage.TYPE_INT_RGB)
      this.drawingProcedure(colorImage, document)
      ImageIO.write(colorImage, "jpeg", outputStream)
    }

    outputStream
  }

  private def drawWordBoundingBox(wordBoundingBox: BoundingBox, graphics: Graphics2D): Unit = {
    graphics.setColor(java.awt.Color.GREEN)
    graphics.drawRect(wordBoundingBox.leftUpX,
      wordBoundingBox.leftUpY,
      wordBoundingBox.lowerRightX - wordBoundingBox.leftUpX,
      wordBoundingBox.lowerRightY - wordBoundingBox.leftUpY)
  }

  private def drawCharacter(character: BoundingBox, graphics: Graphics2D): Unit = {
    graphics.drawRect(character.leftUpX,
      character.leftUpY,
      character.lowerRightX - character.leftUpX,
      character.lowerRightY - character.leftUpY)
  }

  private def drawWord(word: ParsedWord, graphics: Graphics2D): Unit = {
    if (word.wordBoundingBox.isDefined) {
      drawWordBoundingBox(word.wordBoundingBox.get, graphics)
    }
    graphics.setColor(java.awt.Color.RED)
    word.boundingBoxes.foreach(character => drawCharacter(character, graphics))

  }

  private def drawRow(row: ParsedRow, graphics: Graphics2D, rowNum: Int): Unit = {
    row.words.foreach(word => drawWord(word, graphics))
    // Draw Row bounding box
    if (row.boundingBox.isDefined) {
      drawRowBoundingBox(graphics, row.boundingBox.get, rowNum)
    }
  }

  private def drawingProcedure(image: BufferedImage,
                               document: Document): BufferedImage = {
    val graphics = image.createGraphics()
    var rowNum = 0
    document.rows.foreach(row => {
      drawRow(row, graphics, rowNum); rowNum += 1
    })

    image
  }

  private def drawRowBoundingBox(graphics: Graphics2D, rowBB: BoundingBox, rowNum: Int): Unit = {
    graphics.setColor(java.awt.Color.BLUE)
    graphics.drawString(s"Row $rowNum", rowBB.leftUpX + 5, rowBB.leftUpY - 5)
    graphics.drawRect(rowBB.leftUpX,
      rowBB.leftUpY,
      rowBB.lowerRightX - rowBB.leftUpX,
      rowBB.lowerRightY - rowBB.leftUpY)
  }
}

object BoundingBoxDrawer {
  def apply(imageBytes: Array[Byte]): BoundingBoxDrawer = new BoundingBoxDrawer(imageBytes)
}


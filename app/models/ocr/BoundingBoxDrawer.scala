package models.ocr

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.imageio.ImageIO

import models.document.{Document, ParsedRow, ParsedWord}
import models.primitives.BoundingBox
import models.utils.{DrawingOptions, ImageTools}

import scala.concurrent._
import scala.concurrent.duration.Duration
import ExecutionContext.Implicits.global

/**
  * Created by darkg on 03-Sep-16.
  */
class BoundingBoxDrawer(imageBytes: Array[Byte], drawingOptions: DrawingOptions) {
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

  private def drawRowBoundingBox(graphics: Graphics2D, rowBB: BoundingBox, rowNum: Int): Unit = {
    graphics.setColor(java.awt.Color.BLUE)
    graphics.drawString(s"Row $rowNum", rowBB.leftUpX + 5, rowBB.leftUpY - 5)
    graphics.drawRect(rowBB.leftUpX,
      rowBB.leftUpY,
      rowBB.lowerRightX - rowBB.leftUpX,
      rowBB.lowerRightY - rowBB.leftUpY)
  }

  private def drawWordBoundingBox(wordBoundingBox: BoundingBox, graphics: Graphics2D): Unit = {
    graphics.setColor(java.awt.Color.GREEN)
    graphics.drawRect(wordBoundingBox.leftUpX,
      wordBoundingBox.leftUpY,
      wordBoundingBox.lowerRightX - wordBoundingBox.leftUpX,
      wordBoundingBox.lowerRightY - wordBoundingBox.leftUpY)
  }

  private def drawCharacterBoundingBox(character: BoundingBox, graphics: Graphics2D): Future[Unit] = {
    Future.successful(graphics.drawRect(character.leftUpX,
      character.leftUpY,
      character.lowerRightX - character.leftUpX,
      character.lowerRightY - character.leftUpY))
  }

  private def drawWord(word: ParsedWord, graphics: Graphics2D): Future[Any] = {
    if (word.wordBoundingBox.isDefined && drawingOptions.drawWords) {
      drawWordBoundingBox(word.wordBoundingBox.get, graphics)
    }
    if (drawingOptions.drawChars) {
      graphics.setColor(java.awt.Color.RED)
      Future.traverse(word.boundingBoxes) { character => drawCharacterBoundingBox(character, graphics) }
    }
    else {
      Future.successful(List[Unit]())
    }
  }

  private def drawRow(row: ParsedRow, graphics: Graphics2D, rowNum: Int): Future[Any] = {
    // Draw Row bounding box
    if (row.boundingBox.isDefined && drawingOptions.drawRows) {
      drawRowBoundingBox(graphics, row.boundingBox.get, rowNum)
    }
    Future.traverse(row.words) { word => drawWord(word, graphics) }
  }

  private def drawingProcedure(image: BufferedImage,
                               document: Document): BufferedImage = {
    val graphics = image.createGraphics()
    var rowNum = -1
    val fut = Future.traverse(document.rows) { row =>
      rowNum += 1
      drawRow(row, graphics, rowNum)
    }
    Await.result(fut, Duration.Inf)
    image

  }
}

  object BoundingBoxDrawer {
    def apply(imageBytes: Array[Byte], drawingOptions: DrawingOptions): BoundingBoxDrawer = new BoundingBoxDrawer(imageBytes, drawingOptions)
  }


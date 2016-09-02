package models.ocr

import models.primitives.{BoundingBox, RawImage, Row}
import models.utils.ArrayOps

import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks._

/**
  * Created by darkg on 02-Sep-16.
  */
class OCRService {
  val rowDetectionPixelThreshold = scala.util.Properties.envOrElse("PIXEL_ROW_DETECT_THRESHOLD", "15").toInt
  val colDetectionPixelThreshold = scala.util.Properties.envOrElse("PIXEL_COL_DETECT_THRESHOLD", "5").toInt

  def identifyLinesAndCharacters(image: RawImage): List[Row] = {
    var resultRows = new ListBuffer[Row]
    var maxY: Option[Int] = None
    var minY: Option[Int] = None
    for (i <- image.data.indices) {
      if (ArrayOps.efficientRowSum(image.data(i)) >= rowDetectionPixelThreshold) {
        maxY = Some(i)
        if (minY.isEmpty) {
          minY = Some(i)
        }
      } else if (minY.isDefined) {
        resultRows += Row(parseColumns(image.data, i, minY, maxY).toList)
        minY = None
      }
    }
    resultRows.toList
  }


  private def parseColumns(imageRow: Array[Array[Byte]], i: Int,
                           minY: Option[Int], maxY: Option[Int]): ListBuffer[BoundingBox] = {
    var resultList = new ListBuffer[BoundingBox]()
    var maxX: Option[Int] = None
    var minX: Option[Int] = None
    for (j <- imageRow(i).indices) {
      if (ArrayOps.efficientColSum(imageRow, j, minY.get, maxY.get) > colDetectionPixelThreshold) {
        maxX = Some(j)
        if (minX.isEmpty) {
          minX = Some(j)
        }
      }
      else if (minX.isDefined) {
        var maxYp = minY.get
        var exitLoop = false
        var k = maxY.get - 1
        while (k >= minY.get && exitLoop) {
          if (ArrayOps.efficientRowSum(imageRow(minX.get), k, maxX.get) < colDetectionPixelThreshold) {
            maxYp = k + 1
            exitLoop = true
          }
          k -= 1
        }
        resultList += BoundingBox(minX.get, minY.get, maxX.get, maxYp)
        minX = None
      }
    }
    resultList
  }

}

object OCRService {
  def apply: OCRService = new OCRService()
}

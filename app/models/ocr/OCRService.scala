package models.ocr

import models.config.AppConfig
import models.primitives.{BoundingBox, RawImage, Row}
import models.utils.ArrayOps

import scala.collection.mutable.ListBuffer

/**
  * Created by darkg on 02-Sep-16.
  */
case class OCRService() {

  def identifyLinesAndCharacters(image: RawImage): List[Row] = {
    var resultRows = new ListBuffer[Row]
    var maxY: Option[Int] = None
    var minY: Option[Int] = None
    for (i <- image.data.indices) {
      if (ArrayOps.efficientRowSum(image.data(i)) >= AppConfig.rowDetectionPixelThreshold) {
        maxY = Some(i)
        if (minY.isEmpty) {
          minY = Some(i)
        }
      } else if (minY.isDefined) {
        val newRow = Row(parseColumns(image.data, i, minY, maxY).toList)
        if (newRow.rowBoundingBox.isDefined) {
          resultRows += newRow
        }
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
    val extra = AppConfig.boundingBoxExtraSpace
    for (j <- imageRow(i).indices) {
      if (ArrayOps.efficientColSum(imageRow, j, minY.get, maxY.get) >= AppConfig.colDetectionPixelThreshold) {
        maxX = Some(j)
        if (minX.isEmpty) {
          minX = Some(j)
        }
      }
      else if (minX.isDefined) {
        var maxYp = maxY.get - 1
        var exitLoop = false
        var k = maxY.get - 1

        while ((k >= minY.get) && !exitLoop) {
          if (ArrayOps.rectSum(imageRow, minX.get, k, maxX.get - minX.get, 1) >= AppConfig.colDetectionPixelThreshold) {
            maxYp = k + 1
            exitLoop = true
          }
          k -= 1
        }
        if ((maxX.get - minX.get > 0) && (maxYp - minY.get > 0)) {
          resultList += BoundingBox.createFitBoundingBox(
            imageRow(i).length,
            imageRow.length,
            minX.get - (extra * 2),
            minY.get - extra,
            maxX.get + (extra * 2),
            maxYp + extra
          )
        }
        minX = None
      }
    }
    resultList
  }

}

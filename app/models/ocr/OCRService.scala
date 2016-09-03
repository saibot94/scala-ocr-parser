package models.ocr

import models.config.AppConfig
import models.primitives.{BoundingBox, RawImage, Row}
import models.utils.ArrayOps

import scala.collection.mutable.ListBuffer

/**
  * Created by darkg on 02-Sep-16.
  */
class OCRService {

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
      if (ArrayOps.efficientColSum(imageRow, j, minY.get, maxY.get) > AppConfig.colDetectionPixelThreshold) {
        maxX = Some(j)
        if (minX.isEmpty) {
          minX = Some(j)
        }
      }
      else if (minX.isDefined) {
        var maxYp = minY.get
        var exitLoop = false
        var k = maxY.get - 1
        while ((k >= minY.get) && !exitLoop) {
          if (ArrayOps.efficientRowSum(imageRow(minX.get), k, maxX.get) < AppConfig.colDetectionPixelThreshold) {
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

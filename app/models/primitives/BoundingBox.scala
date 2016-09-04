package models.primitives

import models.utils.ArrayOps

/**
  * Created by darkg on 02-Sep-16.
  */
case class BoundingBox(leftUpX: Int, leftUpY: Int, lowerRightX: Int, lowerRightY: Int) {
  override def toString: String = {
    s"[BoundingBox] ($leftUpX, $leftUpY, $lowerRightX, $lowerRightY) "
  }
}

object BoundingBox {
  def createFitBoundingBox(width: Int, height: Int, leftUpX: Int, leftUpY: Int, lowerRightX: Int, lowerRightY: Int): BoundingBox = {
    BoundingBox(ArrayOps.fitInArray(width, leftUpX),
      ArrayOps.fitInArray(height, leftUpY),
      ArrayOps.fitInArray(width, lowerRightX),
      ArrayOps.fitInArray(height, lowerRightY))

  }

}
package models.primitives

/**
  * Created by darkg on 02-Sep-16.
  */
case class BoundingBox(leftUpX: Int, leftUpY: Int, lowerRightX: Int, lowerRightY: Int) {
  override def toString: String = {
    s"[BoundingBox] ($leftUpX, $leftUpY, $lowerRightX, $lowerRightY) "
  }
}
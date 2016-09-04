package models.primitives

/**
  * Created by darkg on 04-Sep-16.
  */
abstract class ObjectWithBoundingBox(protected val boundingBoxes: List[BoundingBox]) {
  protected def computeSelfBoundingBox: Option[BoundingBox] = {
    if (boundingBoxes.nonEmpty) {
      val firstBox = boundingBoxes.head
      val lastBox = boundingBoxes.last
      Some(BoundingBox(firstBox.leftUpX,
        boundingBoxes.minBy(b => b.leftUpY).leftUpY,
        lastBox.lowerRightX,
        boundingBoxes.maxBy(b => b.lowerRightY).lowerRightY))
    } else {
      None
    }
  }
}

package models.primitives

import models.config.AppConfig

/**
  * Created by darkg on 02-Sep-16.
  */
class Row(val characterBoundingBoxes: List[BoundingBox]) {

  var rowBoundingBox: Option[BoundingBox] = computeSelfBoundingBox

  private def computeSelfBoundingBox: Option[BoundingBox] = {
    if (characterBoundingBoxes.nonEmpty) {
      val offset = AppConfig.boundingBoxExtraSpace
      val firstBox = characterBoundingBoxes.head
      val lastBox = characterBoundingBoxes.last
      Some(BoundingBox(firstBox.leftUpX - offset,
        firstBox.leftUpY - offset,
        lastBox.lowerRightX + offset,
        lastBox.lowerRightY + offset))
    } else {
      None
    }
  }
}

object Row {
  def apply(boundingBoxes: List[BoundingBox]): Row = new Row(boundingBoxes)
}


package models.document

import models.primitives.{BoundingBox, ObjectWithBoundingBox}

/**
  * Created by darkg on 04-Sep-16.
  */
class ParsedWord(override val boundingBoxes: List[BoundingBox]) extends ObjectWithBoundingBox(boundingBoxes) {
  var wordBoundingBox: Option[BoundingBox] = computeSelfBoundingBox
}

object ParsedWord {
  def apply(boundingBoxes: List[BoundingBox]): ParsedWord = new ParsedWord(boundingBoxes)

}



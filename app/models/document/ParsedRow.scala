package models.document

import models.primitives.BoundingBox

/**
  * Created by darkg on 04-Sep-16.
  */
case class ParsedRow(boundingBox : Option[BoundingBox], words : List[ParsedWord]){}

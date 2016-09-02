package models.lineops

import models.primitives.LineDelimiter

/**
  * Created by darkg on 02-Sep-16.
  */
class LineExtractor {
  def extractLineCoordinates: List[LineDelimiter] = {
    List[LineDelimiter]()
  }
}

object LineExtractor {
  def apply: LineExtractor = new LineExtractor()
}

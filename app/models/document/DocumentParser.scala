package models.document

import models.config.AppConfig
import models.primitives.{BoundingBox, Row}

import scala.collection.mutable.ListBuffer

import scala.concurrent._
import scala.concurrent.duration.Duration
import ExecutionContext.Implicits.global

/**
  * Created by darkg on 04-Sep-16.
  */
object DocumentParser {

  private def parseRows(rows: List[Row]): Future[List[ParsedRow]] = {
    Future.traverse(rows) (
      r =>
        Future(ParsedRow(r.rowBoundingBox, parseWords(r)))
    )
  }

  private def parseWords(row: Row): List[ParsedWord] = {
    val result = ListBuffer[ParsedWord]()
    var currentWordBoxes = ListBuffer[BoundingBox](row.boundingBoxes.head)
    for (i <- 0 until (row.boundingBoxes.size - 1)) {
      val j = i + 1
      if (row.boundingBoxes(j).leftUpX - row.boundingBoxes(i).lowerRightX <= AppConfig.wordDetectPixelLimit) {
        currentWordBoxes += row.boundingBoxes(j)
      } else {
        result += ParsedWord(currentWordBoxes.toList)
        currentWordBoxes.clear()
        currentWordBoxes += row.boundingBoxes(j)
      }
    }
    if (currentWordBoxes.nonEmpty) {
      result += ParsedWord(currentWordBoxes.toList)
    }
    result.toList
  }

  def parseImageToDocument(rawRows: List[Row]): Document = {
    val rows = Await.result(parseRows(rawRows), Duration.Inf)
    Document(rows)
  }


}

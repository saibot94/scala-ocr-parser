package models.utils

/**
  * Created by darkg on 02-Sep-16.
  */
object ArrayOps {

  def efficientRowSum(arr: Array[Byte]): Long = {
    var sum: Long = 0
    var i = 0
    while (i < arr.length) {
      sum += arr(i)
      i += 1
    }
    sum
  }

  def efficientRowSum(arr: Array[Byte], start: Int, stop: Int): Long = {
    var sum: Long = 0
    var i = start
    while (i < stop) {
      sum += arr(i)
      i += 1
    }
    sum
  }

  def efficientColSum(arr: Array[Array[Byte]], col: Int, minY: Int, maxY: Int): Long = {
    var sum: Long = 0
    var i = minY
    while (i < maxY) {
      sum += arr(i)(col)
      i += 1
    }
    sum
  }
}

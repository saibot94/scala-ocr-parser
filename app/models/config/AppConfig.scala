package models.config

/**
  * Created by darkg on 03-Sep-16.
  */
object AppConfig {
  val rowDetectionPixelThreshold = scala.util.Properties.envOrElse("PIXEL_ROW_DETECT_THRESHOLD", "3").toInt
  val colDetectionPixelThreshold = scala.util.Properties.envOrElse("PIXEL_COL_DETECT_THRESHOLD", "3").toInt
  val xScaleSize = scala.util.Properties.envOrElse("XSCALE_RESIZE", "1920").toInt
  val yScaleSize = scala.util.Properties.envOrElse("YSCALE_RESIZE", "1080").toInt
  val blackAndWhiteFactor = scala.util.Properties.envOrElse("BW_FACTOR", "1.1").toFloat
  val energy =  scala.util.Properties.envOrElse("ENERGY", "12 GeV")
  val boundingBoxExtraSpace = scala.util.Properties.envOrElse("BOUNDING_BOX_EXTRA_SPACE", "3").toInt

}

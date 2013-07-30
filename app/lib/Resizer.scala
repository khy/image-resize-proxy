package lib

import scala.concurrent.{Future, future}
import java.awt.{Image, RenderingHints}
import java.awt.image.BufferedImage
import play.api.libs.concurrent.Execution.Implicits._

trait Resizer {

  def resize(image: Image, width: Option[Int], height: Option[Int]): Future[Image]

}

object Resizer {

  def getScaledDimensions(image: Image, width: Option[Int], height: Option[Int]) = {
    val originalWidth = image.getWidth(null)
    val originalHeight = image.getHeight(null)
    val targetWidth = width.getOrElse(originalWidth)
    val targetHeight = height.getOrElse(originalHeight)

    val widthScale = targetWidth / originalWidth.toDouble
    val heightScale = targetHeight / originalHeight.toDouble

    if (widthScale < heightScale) {
      ((originalWidth * widthScale).toInt, (originalHeight * widthScale).toInt)
    } else {
      ((originalWidth * heightScale).toInt, (originalHeight * heightScale).toInt)
    }
  }

}

class AwtResizer extends Resizer {

  def resize(image: Image, width: Option[Int], height: Option[Int]) = {
    if (width.isDefined || height.isDefined) {
      future {
        val dimensions = Resizer.getScaledDimensions(image, width, height)
        val resizedImage = new BufferedImage(dimensions._1, dimensions._2, BufferedImage.TYPE_INT_RGB)
        val graphics = resizedImage.createGraphics

        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.drawImage(image, 0, 0, dimensions._1, dimensions._2, null)
        graphics.dispose
        resizedImage
      }
    } else {
      Future.successful(image)
    }
  }

}

package lib

import scala.concurrent.Future
import java.awt.Image
import play.api.libs.concurrent.Execution.Implicits._

trait Resizer {

  def resize(image: Image, width: Option[Int], height: Option[Int]): Future[Image]

}

class ScrimageResizer extends Resizer {

  import com.sksamuel.scrimage

  def resize(image: Image, width: Option[Int], height: Option[Int]) = {
    if (width.isDefined || height.isDefined) {
      val asyncImage = scrimage.Image(image).toAsync
      val futureScaledImage = asyncImage.scaleTo(
        width.getOrElse(asyncImage.width),
        height.getOrElse(asyncImage.height)
      )
      futureScaledImage.map { _.toImage.toBufferedImage }
    } else {
      Future.successful(image)
    }
  }

}

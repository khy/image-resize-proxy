package lib

import scala.concurrent.Future
import java.awt.Image
import play.api.libs.concurrent.Execution.Implicits._

trait Resizer {

  def resize(image: Image, height: Option[Int], width: Option[Int]): Future[Image]

}

class ScrimageResizer extends Resizer {

  import com.sksamuel.scrimage

  def resize(image: Image, height: Option[Int], width: Option[Int]) = {
    val asyncImage = scrimage.Image(image).toAsync
    val futureScaledImage = asyncImage.scaleTo(
      width.getOrElse(asyncImage.width),
      height.getOrElse(asyncImage.height)
    )
    futureScaledImage.map { _.toImage.toBufferedImage }
  }

}

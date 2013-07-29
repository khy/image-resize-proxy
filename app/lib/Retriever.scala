package lib

import scala.concurrent.Future
import java.awt.Image
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits._

trait Retriever {

  def retrieve(key: String): Future[Option[Image]]

}

class GiltRetriever extends Retriever {

  def retrieve(key: String) = {
    val path = (("0" * 16) + key).takeRight(16).grouped(4).take(3).mkString("/")
    val url = "http://cdn1.giltcdn.com/images/share/uploads/%s/%s/orig.jpg".format(path, key)
    WS.url(url).get().map { response =>
      if (response.status == 200) {
        val bytes = response.getAHCResponse.getResponseBodyAsBytes
        val stream = new ByteArrayInputStream(bytes)
        Some(ImageIO.read(stream))
      } else {
        None
      }
    }
  }

}
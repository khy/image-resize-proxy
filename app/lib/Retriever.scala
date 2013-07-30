package lib

import scala.concurrent.Future
import java.awt.Image
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger

trait Retriever {

  def retrieve(key: String): Future[Option[Image]]

}

trait UrlRetriever extends Retriever {

  def urlForKey(key: String): String

  def retrieve(key: String) = {
    val url = urlForKey(key)
    WS.url(url).get().map { response =>
      if (response.status == 200) {
        Logger.info("Download from %s succeeded!".format(url))
        val bytes = response.getAHCResponse.getResponseBodyAsBytes
        val stream = new ByteArrayInputStream(bytes)
        Some(ImageIO.read(stream))
      } else {
        Logger.info("Download from %s failed: %s".format(url, response.statusText))
        None
      }
    }
  }

}

class GiltRetriever extends UrlRetriever {

  def urlForKey(key: String) = {
    val path = (("0" * 16) + key).takeRight(16).grouped(4).take(3).mkString("/")
    "http://cdn1.giltcdn.com/images/share/uploads/%s/%s/orig.jpg".format(path, key)
  }

}

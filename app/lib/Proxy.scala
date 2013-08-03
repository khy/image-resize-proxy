package lib

import scala.concurrent.Future
import java.awt.Image
import play.api.libs.concurrent.Execution.Implicits._

object Proxy {

  def instance = new ConfigurableProxy(
    versionCache = new ConcurrentVersionCache,
    originalCache = new ConcurrentOriginalCache,
    retriever = new GiltRetriever,
    resizer = new AwtResizer
  )

}

trait Proxy {

  def get(key: String, width: Option[Int], height: Option[Int]): Future[Option[Image]]

}

class ConfigurableProxy(
  versionCache: VersionCache,
  originalCache: OriginalCache,
  retriever: Retriever,
  resizer: Resizer
) {

  def get(key: String, width: Option[Int], height: Option[Int]): Future[Option[Image]] = {
    versionCache(key, width, height) { () =>
      originalCache(key) { () =>
        retriever.retrieve(key)
      }.flatMap { optImage =>
        optImage.map { image =>
          if (width.isDefined || height.isDefined) {
            resizer.resize(image, width, height).map(Some(_))
          } else {
            Future.successful(Some(image))
          }
        }.getOrElse {
          Future.successful(None)
        }
      }
    }
  }

}

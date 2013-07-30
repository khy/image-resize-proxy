package lib

import scala.concurrent.Future
import java.awt.Image
import play.api.libs.concurrent.Execution.Implicits._

object Proxy {

  def instance: Proxy = new DefaultProxy

}

trait Proxy {

  def originalCache: Cache

  def versionCache: Cache

  def retriever: Retriever

  def resizer: Resizer

  def retrieve(
    key: String,
    width: Option[Int],
    height: Option[Int]
  ): Future[Option[Image]] = {
    val versionCacheKey = Cache.versionKey(key, width, height)
    val optImageVersion = versionCache.get(versionCacheKey)

    optImageVersion.map { imageVersion =>
      Future.successful(Some(imageVersion))
    }.getOrElse {
      val futureOriginalImage = originalCache.get(key).map { originalImage =>
        Future.successful(Some(originalImage))
      }.getOrElse {
        retriever.retrieve(key)
      }

      futureOriginalImage.flatMap { optOriginalImage =>
        optOriginalImage.map { originalImage =>
          originalCache.set(key, originalImage)

          resizer.resize(originalImage, width, height).map { imageVersion =>
            versionCache.set(versionCacheKey, imageVersion)
            Some(imageVersion)
          }
        }.getOrElse {
          Future.successful(None)
        }
      }
    }
  }

}

class DefaultProxy extends Proxy {

  def originalCache = new NullCache

  def versionCache = new NullCache

  def retriever = new GiltRetriever

  def resizer = new AwtResizer

}

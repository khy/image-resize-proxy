package lib

import scala.concurrent.{Future, promise}
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._
import java.awt.Image
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger

trait Cache {

  def apply(key: String)(getImage: () => Future[Option[Image]]): Future[Option[Image]]

}

object Cache {

  def versionKey(key: String, width: Option[Int], height: Option[Int]) = {
    Seq(Some(key), width.map(_.toString), height.map(_.toString)).flatten.mkString("-")
  }

}

trait OriginalCache extends Cache

trait VersionCache extends Cache {

  def apply(key: String, width: Option[Int], height: Option[Int])(getImage: () => Future[Option[Image]]): Future[Option[Image]] = {
    if (width.isDefined || height.isDefined) {
      val versionKey = Cache.versionKey(key, width, height)
      apply(versionKey)(getImage)
    } else {
      getImage()
    }
  }

}

trait ConcurrentCache extends Cache {

  private val imageMap = new ConcurrentHashMap[String, Future[Option[Image]]].asScala

  def apply(key: String)(getImage: () => Future[Option[Image]]): Future[Option[Image]] = {
    Logger.info("Getting key: %s".format(key))
    val imagePromise = promise[Option[Image]]

    imageMap.putIfAbsent(key, imagePromise.future).getOrElse {
      getImage() onSuccess {
        case optImage => {
          Logger.info("Setting key: %s".format(key))
          imagePromise success optImage
        }
      }
      imagePromise.future
    }
  }

}

class ConcurrentOriginalCache extends OriginalCache with ConcurrentCache

class ConcurrentVersionCache extends VersionCache with ConcurrentCache

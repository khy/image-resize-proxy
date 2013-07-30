package lib

import java.awt.Image

object Cache {

  def versionKey(key: String, width: Option[Int], height: Option[Int]) = {
    Seq(Some(key), width.map(_.toString), height.map(_.toString)).flatten.mkString("-")
  }

}

trait Cache {

  def get(key: String): Option[Image]

  def set(key: String, image: Image): Unit

}

class NullCache extends Cache {

  def get(key: String) = None

  def set(key: String, image: Image) = Unit

}

class PlayCache(
  val expiration: Int
) extends Cache {

  import play.api.cache.{Cache => UnderlyingCache}
  import play.api.Play.current

  def get(key: String) = UnderlyingCache.getAs[Image](key)

  def set(key: String, image: Image) = UnderlyingCache.set(key, image, expiration)

}

package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

import java.io.ByteArrayOutputStream
import java.awt.image.RenderedImage
import javax.imageio.ImageIO

import lib.Proxy

object Application extends Controller {

  def image(key: String) = Action { request =>
    val height = request.queryString.get("h").flatMap(_.headOption).map(_.toInt)
    val width = request.queryString.get("w").flatMap(_.headOption).map(_.toInt)

    Async {
      Proxy.instance.retrieve(key, height, width).map { optImage =>
        optImage.map { image =>
          image match {
            case renderedImage: RenderedImage => {
              val stream = new ByteArrayOutputStream
              ImageIO.write(renderedImage, "jpg", stream)
              Ok(stream.toByteArray).as("image/jpeg")
            }
            case _ => InternalServerError
          }
        }.getOrElse {
          NotFound
        }
      }
    }
  }

}

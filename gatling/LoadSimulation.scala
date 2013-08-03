package imageresizeproxy

import scala.util.Random

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import bootstrap._

class LoadSimulation extends Simulation {

  def randomImageId() = {
    val ids = Seq(
      "201063942",
      "300590875",
      "201211102",
      "165898720",
      "300575017",
      "300085977",
      "201525511",
      "300582776",
      "300222359",
      "300596557"
    )
    ids(Random.nextInt(ids.length))
  }

  def randomDimension() = {
    val dimensions = Seq(
      Some("100"),
      Some("200"),
      Some("300"),
      Some("400"),
      Some("500"),
      Some("600"),
      Some("700"),
      None
    )
    dimensions(Random.nextInt(dimensions.length))
  }

  def randomRequest() = {
    val imageId = randomImageId()
    val width = randomDimension()
    val height = randomDimension()
    var request = http("Get image: %s".format(imageId)).get("/" + imageId)

    if (width.isDefined) {
      request = request.queryParam("w", width.get)
    }

    if (height.isDefined) {
      request = request.queryParam("h", height.get)
    }

    request.check(status.is(200))
  }

  def randomRequestSequence() = {
    var requestSequence = exec(
      randomRequest()
    ).pause(0 milliseconds, 5 milliseconds)

    1 to 100 foreach { _ => 
      requestSequence = requestSequence.exec(
        randomRequest()
      ).pause(0 milliseconds, 5 milliseconds)
    }

    requestSequence
  }

  val httpConf = http.baseURL("http://localhost:9000")

  val getImageScenario = scenario("Image Endpoint").during(10) { randomRequestSequence() }

  setUp {
    getImageScenario.inject(ramp(100 users) over (10 seconds))
  }.protocols(httpConf)

}

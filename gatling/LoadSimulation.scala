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

  val httpConf = http.baseURL("http://localhost:9000")

  val getImageScenario = scenario("Image Endpoint")
   .during(10) {
     exec(
       http("Get Random Image")
         .get("/" + randomImageId())
         .check(status.is(200)))
     .pause(0 milliseconds, 5 milliseconds)
   }

  setUp {
    getImageScenario.inject(ramp(100 users) over (10 seconds))
  }.protocols(httpConf)

}

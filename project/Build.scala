import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "image-resize-proxy"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.sksamuel.scrimage" % "scrimage-core" % "1.3.3"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings()

}

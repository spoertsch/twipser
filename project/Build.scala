import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "Twipser"
    val appVersion      = "1.0"

    val appDependencies = Seq(
	   // Adds filters like GZIP filter
       filters 
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}
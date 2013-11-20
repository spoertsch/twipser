import scala.concurrent.duration.DurationInt

import actors.GenerateDummyTwiip
import actors.MessageActor
import akka.actor.Props
import play.api.GlobalSettings
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter()) with GlobalSettings {

  override def onStart(app: play.api.Application) {
    Logger.info("onStart: Init actors")
    val messageActor = Akka.system.actorOf(Props[MessageActor], name = "messageActor")
    Akka.system.scheduler.schedule(30 seconds, 30 seconds, messageActor, GenerateDummyTwiip)
  }

}
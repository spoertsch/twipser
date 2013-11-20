import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api._
import play.api.Play._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import actors.MessageActor
import scala.concurrent.duration.DurationInt
import actors.GenerateDummyTwiip
import play.api.mvc._
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter()) with GlobalSettings {

  override def onStart(app: play.api.Application) {
    Logger.info("onStart: Init actors")
    val messageActor = Akka.system.actorOf(Props[MessageActor], name = "messageActor")
    Akka.system.scheduler.schedule(30 seconds, 30 seconds, messageActor, GenerateDummyTwiip)
  }

}
import play.api.GlobalSettings
import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import actors.MessageActor
import scala.concurrent.duration.DurationInt
import actors.GenerateDummyMessage

object Global extends GlobalSettings {
  override def onStart(app: play.api.Application) {
    Logger.info("onStart: Init actors")
    val messageActor = Akka.system.actorOf(Props[MessageActor], name = "messageActor")
    Akka.system.scheduler.schedule(30 seconds, 30 seconds, messageActor, GenerateDummyMessage)
  }
}
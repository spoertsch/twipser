package actors

import akka.actor.Actor
import model.Twiip
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import java.util.Date
import controllers.TwiipController

case class GenerateDummyMessage()

class MessageActor extends Actor {

  // macr
  def receive = {
    case GenerateDummyMessage => {
      val date = new Date()
      val message = Twiip("Dummy", "This is a dummy message (" + date + ")")
      Twiip.save(message).map { lastError =>
        if (lastError.ok) {
          TwiipController.channel.push(Json.toJson(message))
        } else {
          Logger.error("Error generating dummy message: " + lastError)
        }
      }
    }
    case _ => Logger.error("Invalid message")
  }
}
package actors

import akka.actor.Actor
import model.Message
import controllers.MessageController
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import java.util.Date

case class GenerateDummyMessage()

class MessageActor extends Actor {

  // macr
  def receive = {
    case GenerateDummyMessage => {
      val date = new Date()
      val message = Message("Dummy", "This is a dummy message (" + date + ")")
      Message.save(message).map { lastError =>
        if (lastError.ok) {
          MessageController.channel.push(Json.toJson(message))
        } else {
          Logger.error("Error generating dummy message: " + lastError)
        }
      }
    }
    case _ => Logger.error("Invalid message")
  }
}
package actors

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.Actor
import controllers.TwiipController
import model.Twiip
import play.api.Logger
import play.api.libs.json.Json

case class GenerateDummyTwiip()

class MessageActor extends Actor {

  // macr
  def receive = {
    case GenerateDummyTwiip => {
      val twiip = Twiip("Dummy", "This is a dummy twiip!")
      Twiip.save(twiip).map { lastError =>
        if (lastError.ok) {
          TwiipController.pushToFeed(twiip)
        } else {
          Logger.error("Error generating dummy twiip: " + lastError)
        }
      }
    }
    case _ => Logger.error("Invalid twiip")
  }
}
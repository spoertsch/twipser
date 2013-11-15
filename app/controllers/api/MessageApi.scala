package controllers.api

import play.api.mvc.Action
import play.api.mvc.Controller
import model.Message
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._
import controllers.MessageController
import scala.concurrent.ExecutionContext

object MessageApi extends Controller {
  
  implicit val ec: ExecutionContext =  ExecutionContext.Implicits.global
  
  def findAll() = Action.async {
    implicit req =>
      Message.findAll.map { messages =>
        render {
          case Accepts.Json() => Ok(Json.toJson(messages)).as(JSON)
          //case Accepts.Html() => Ok(views.html.messages(messages))
        }
      }
  }

  implicit val rds = (
    (__ \ 'author).read[String] and
    (__ \ 'message).read[String]) tupled

  def createJson() = Action(parse.json) { request =>
    request.body.validate[(String, String)].map {
      case (author, message) => {
        val newMessage = Message(author, message)
        Message.save(newMessage)
        MessageController.channel.push(Json.toJson(newMessage))
        Ok(Json.toJson(newMessage)).as(JSON)
      }
    }.recoverTotal {
      e => BadRequest("Detected error:" + JsError.toFlatJson(e))
    }
  }
}
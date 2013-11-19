package controllers.api

import play.api.mvc.Action
import play.api.mvc.Controller
import model.Message
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._
import controllers.MessageController
import scala.concurrent.ExecutionContext
import views.html.defaultpages.notFound
import play.api.mvc.Accepting
import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID
import play.Logger


case class MessageV2(author: String, message: String, createdOn: DateTime, id: String = BSONObjectID.generate.stringify)

object MessageApi extends Controller {

  // ec
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val AcceptMessageV2Json = Accepting("application/vnd.com.twipser.message.v2+json")

  implicit val messageWrites = Json.writes[MessageV2]
  implicit val messageReads = Json.reads[MessageV2]

  // mafa
  def findAll() = Action.async {
    implicit req =>
      Message.findAll.map { messages =>
        Ok(Json.toJson(messages)).as(JSON)
      }
  }

  // mafaba
  def findAllByAuthor(author: String) = Action.async {
    implicit req =>
      Message.findAll(Some(author)).map { messages =>
        render {
          case AcceptMessageV2Json() => {
            val messagesV2 = messages.map(m => MessageV2(m.author, m.message, new DateTime(), m.id))
            Ok(Json.toJson(messagesV2)).as(JSON)
          }
          case Accepts.Json() => {
            Ok(Json.toJson(messages)).as(JSON)
          }
          case Accepts.Xml() => {
            Ok(<messages>
                { messages.map(m => <message><author>{ m.author }</author><text>{ m.message }</text></message>) }
              </messages>).as(XML)
          }
          case _ => NotAcceptable

        }
      }
  }

  // mar
  implicit val rds = (
    (__ \ 'author).read[String] and
    (__ \ 'message).read[String]) tupled

  // mac
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
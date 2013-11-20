package controllers.api

import play.api.mvc.Action
import play.api.mvc.Controller
import model.Twiip
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent.ExecutionContext
import views.html.defaultpages.notFound
import play.api.mvc.Accepting
import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID
import play.Logger
import controllers.TwiipController


case class MessageV2(author: String, message: String, createdOn: DateTime, id: String = BSONObjectID.generate.stringify)

object TwiipApi extends Controller {

  // ec
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val AcceptMessageV2Json = Accepting("application/vnd.com.twipser.message.v2+json")

  implicit val messageWrites = Json.writes[MessageV2]
  implicit val messageReads = Json.reads[MessageV2]

  // mafa
  def findAll() = Action.async {
    implicit req =>
      Twiip.findAll.map { messages =>
        Ok(Json.toJson(messages)).as(JSON)
      }
  }

  // mafaba
  def findAllByAuthor(author: String) = Action.async {
    implicit req =>
      Twiip.findAll(Some(author)).map { twiips =>
        render {
          case AcceptMessageV2Json() => {
            val messagesV2 = twiips.map(m => MessageV2(m.author, m.message, new DateTime(), m.id))
            Ok(Json.toJson(messagesV2)).as(JSON)
          }
          case Accepts.Json() => {
            Ok(Json.toJson(twiips)).as(JSON)
          }
          case Accepts.Xml() => {
            Ok(<twiips>
                { twiips.map(t => <twiip>
                						<author>{ t.author }</author>
                						<text>{ t.message }</text>
                						<createdAt>{ t.createdAtISO }</createdAt>
                					</twiip>) }
              </twiips>).as(XML)
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
        val newTwiip = Twiip(author, message)
        Twiip.save(newTwiip)
        TwiipController.channel.push(Json.toJson(newTwiip))
        Ok(Json.toJson(newTwiip)).as(JSON)
      }
    }.recoverTotal {
      e => BadRequest("Detected error:" + JsError.toFlatJson(e))
    }
  }
}
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

object TwiipApi extends Controller {

  // ec
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  // mafa
  def findAll() = Action.async {
    implicit req =>
      Twiip.findAll.map { messages =>
        Ok(Json.toJson(messages)).as(JSON)
      }
  }

  def findById(id: String) = Action.async {
    implicit req =>
      Twiip.findById(id).map(twiip =>
        twiip match {
          case Some(twiip) => {
            render {
              case Accepts.Json() => {
                Ok(Json.toJson(twiip)).as(JSON)
              }
              case Accepts.Xml() => {
                Ok(<twiip>
                     <author>{ twiip.author }</author>
                     <text>{ twiip.message }</text>
                     <createdAt>{ twiip.createdAtISO }</createdAt>
                   </twiip>).as(XML)
              }
              case _ => NotAcceptable
            }
          }
          case _ => NotFound
        })
  }

  // mafaba
  def findAllByAuthor(author: String) = Action.async {
    implicit req =>
      Twiip.findAll(Some(author)).map { twiips =>
        render {
          case Accepts.Json() => {
            Ok(Json.toJson(twiips)).as(JSON)
          }
          case Accepts.Xml() => {
            Ok(<twiips>
                 {
                   twiips.map(t => <twiip>
                                     <author>{ t.author }</author>
                                     <text>{ t.message }</text>
                                     <createdAt>{ t.createdAtISO }</createdAt>
                                   </twiip>)
                 }
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
  def createJson() = Action(parse.json) { implicit request =>
    request.body.validate[(String, String)].map {
      case (author, message) => {
        val newTwiip = Twiip(author, message)
        Twiip.save(newTwiip)
        TwiipController.channel.push(Json.toJson(newTwiip))
        Created.withHeaders(
          "Location" -> routes.TwiipApi.findById(newTwiip.id).absoluteURL())
      }
    }.recoverTotal {
      e => BadRequest("Detected error:" + JsError.toFlatJson(e))
    }
  }
}
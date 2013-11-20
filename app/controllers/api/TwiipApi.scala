package controllers.api

import scala.concurrent.ExecutionContext.Implicits.global

import controllers.TwiipController
import model.Twiip
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsError
import play.api.libs.json.Json
import play.api.libs.json.__
import play.api.mvc.Action
import play.api.mvc.Controller

object TwiipApi extends Controller {

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
        TwiipController.pushToFeed(newTwiip)
        Created.withHeaders(
          "Location" -> routes.TwiipApi.findById(newTwiip.id).absoluteURL())
      }
    }.recoverTotal {
      e => BadRequest("Detected error:" + JsError.toFlatJson(e))
    }
  }
}
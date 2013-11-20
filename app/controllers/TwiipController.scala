package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Results._
import scala.concurrent.Future
import model._
import play.modules.reactivemongo.MongoController
import scala.concurrent.ExecutionContext
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Concurrent
import play.api.mvc.WebSocket
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.Json
import play.api.Logger
import play.api.libs.json.Writes
import org.joda.time.LocalDateTime
import play.api.libs.json.JsString
import org.joda.time.format.ISODateTimeFormat

object TwiipController extends Controller with MongoController {

  //ec
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  // mcmf
  val twiipForm = Form(
    tuple(
      "author" -> nonEmptyText(minLength = 3),
      "message" -> nonEmptyText(minLength = 1, maxLength = 120)))

  //mccf    
  def createForm = Action.async {
    implicit request =>
      Twiip.findAll.map {
        twiips => Ok(views.html.twiips(twiipForm, twiips))
      }
  }

  // mcc
  def create = Action.async {
    implicit request =>
      twiipForm.bindFromRequest.fold(
        errors => {
          Twiip.findAll.map {
            twiips => BadRequest(views.html.twiips(errors, twiips))
          }
        },
        twiip => {
          val newTwiip = Twiip(twiip._1, twiip._2)
          Twiip.save(newTwiip).map(lastError => {
            if (lastError.ok) {
              pushToFeed(newTwiip)
              Redirect(routes.TwiipController.createForm())
            } else {
              InternalServerError(lastError.message)
            }
          })

        })
  }

  //mcws1
  val (broadcast, channel) = Concurrent.broadcast[JsValue]

  def pushToFeed(twiip: Twiip) = {
    channel.push(Json.toJson(twiip))
  }

  def feed = WebSocket.using[JsValue] { req =>
    (Iteratee.foreach { json =>
      Unit
    }, broadcast)
  }

  //mcws2
  //  def feed = WebSocket.using[JsValue] { req =>
  //    (Iteratee.foreach { json =>
  //      val form = twiipForm.bind(json)
  //      if (!form.hasErrors) {
  //        val newTwiip = Twiip(form.get._1, form.get._2)
  //        Twiip.save(newTwiip)
  //        channel.push(Json.toJson(newTwiip))
  //      }
  //    }, broadcast)
  //  }

}
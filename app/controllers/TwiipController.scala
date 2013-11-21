package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import model.Twiip
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.tuple
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import play.modules.reactivemongo.MongoController
import model.TwiipImplicits._

object TwiipController extends Controller with MongoController {

  val twiipForm = Form(
    tuple(
      "author" -> nonEmptyText(minLength = 3),
      "message" -> nonEmptyText(minLength = 1, maxLength = 120)))

  def createForm = Action.async {
    implicit request =>
      Twiip.findAll.map {
        twiips => Ok(views.html.twiips(twiipForm, twiips))
      }
  }

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

  val (broadcast, channel) = Concurrent.broadcast[JsValue]

  def pushToFeed(twiip: Twiip) = {
    channel.push(Json.toJson(twiip))
  }

  def feed = WebSocket.using[JsValue] { req =>
    (Iteratee.foreach { json =>
      Unit
    }, broadcast)
  }

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
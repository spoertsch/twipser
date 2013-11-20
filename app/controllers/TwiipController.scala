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
        twiips => Ok(views.html.messages(twiipForm, twiips))
      }
  }

  // mcc
  def create = Action.async {
    implicit request =>
      twiipForm.bindFromRequest.fold(
        errors => {
          Twiip.findAll.map {
            twiips => BadRequest(views.html.messages(errors, twiips))
          }
        },
        twiip => {
          val newTwiip = Twiip(twiip._1, twiip._2)
          Twiip.save(newTwiip).map(lastError => {
            if (lastError.ok) {
              channel.push(Json.toJson(newTwiip))
              Redirect(routes.TwiipController.createForm())
            } else {
              InternalServerError(lastError.message)
            }
          })

        })
  }

  //mcws1
  val (broadcast, channel) = Concurrent.broadcast[JsValue]

  def feed = WebSocket.using[JsValue] { req =>
    (Iteratee.foreach { json =>
      Unit
    }, broadcast)
  }

  //mcws2
  //  def feed = WebSocket.using[JsValue] { req =>
  //    (Iteratee.foreach { json =>
  //      val form = messageForm.bind(json)
  //      if (!form.hasErrors) {
  //        val newMessage = Message(form.get._1, form.get._2)
  //        Message.save(newMessage)
  //        channel.push(Json.toJson(newMessage))
  //      }
  //    }, broadcast)
  //  }

}
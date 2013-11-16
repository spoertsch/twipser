package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller
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


object MessageController extends Controller with MongoController {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val messageForm = Form(
    tuple(
      "author" -> nonEmptyText(minLength = 3),
      "message" -> nonEmptyText(minLength = 1, maxLength = 120)))

  def createForm = Action.async {
    implicit request =>
      Message.findAll.map {
        messages => Ok(views.html.message(messageForm, messages))
      }
  }

  def create = Action.async {
    implicit request =>
      messageForm.bindFromRequest.fold(
        errors => {
          Message.findAll.map {
            messages => BadRequest(views.html.message(errors, messages))
          }
        },
        message => {
          val newMessage = Message(message._1, message._2)
          Message.save(newMessage)
          channel.push(Json.toJson(newMessage))
          Future.successful(Redirect(routes.MessageController.create()))
        })
  }
  
  val (broadcast, channel) = Concurrent.broadcast[JsValue]

  def feed = WebSocket.using[JsValue] { req =>
    (Iteratee.foreach { json =>
      Unit
    }, broadcast)
  }
  
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
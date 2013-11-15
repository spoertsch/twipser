package viewhelper

import views.html.helper.FieldConstructor
import views.html.inputTemplate

object ViewHelper {
  implicit val myFields = FieldConstructor(inputTemplate.f) 
}
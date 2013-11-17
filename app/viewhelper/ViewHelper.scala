package viewhelper

import views.html.helper.FieldConstructor
import views.html.inputTemplate

object ViewHelper {
  // vh
  implicit val myFields = FieldConstructor(inputTemplate.f) 
}
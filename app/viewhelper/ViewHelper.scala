package viewhelper

import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

import views.html.helper.FieldConstructor
import views.html.inputTemplate

object ViewHelper {
  implicit val myFields = FieldConstructor(inputTemplate.f)

  def formatJodaDateTime(date: LocalDateTime, pattern: String = "HH:mm yy-MM-dd") = DateTimeFormat.forPattern(pattern).print(date)

}
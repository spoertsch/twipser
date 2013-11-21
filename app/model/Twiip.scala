package model

import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import reactivemongo.bson.BSONObjectID

case class Twiip(author: String,
  message: String,
  createdAt: LocalDateTime = new LocalDateTime(),
  id: String = BSONObjectID.generate.stringify) {

  /**
   * Converts createdAt to printable date time.
   */
  def createdAtISO(): String = ISODateTimeFormat.dateTimeNoMillis().print(createdAt)

}
package model

import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.Play._
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

object Twiip {

  import play.modules.reactivemongo.ReactiveMongoPlugin
  import reactivemongo.api.collections.default.BSONCollection
  import reactivemongo.api._
  import reactivemongo.bson._
  import model.TwiipImplicits._
  
  import scala.concurrent.ExecutionContext.Implicits.global

  def db = ReactiveMongoPlugin.db
  def collection = db.collection[BSONCollection]("Twiips")
  
}

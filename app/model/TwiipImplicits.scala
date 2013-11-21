package model

import play.api.libs.json.Reads
import play.api.libs.json.Writes
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import play.api.libs.json.Json
import org.joda.time.LocalDateTime
import reactivemongo.bson.BSONObjectID
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.JsString

object TwiipImplicits {
  implicit val readsJodaLocalDateTime = Reads[LocalDateTime](js =>
    js.validate[String].map[LocalDateTime](dtString =>
      LocalDateTime.parse(dtString, ISODateTimeFormat.dateTimeNoMillis())))

  implicit val writesJodaLocalDateTime = Writes[LocalDateTime](dt =>
    JsString(ISODateTimeFormat.dateTimeNoMillis().print(dt)))

  implicit val twiipWrites = Json.writes[Twiip]
  implicit val twiipReads = Json.reads[Twiip]

  // Does the mapping BSON <-> Scala object
  implicit object TwiipBSONReader extends BSONDocumentReader[Twiip] {
    def read(doc: BSONDocument): Twiip = Twiip(
      doc.getAs[String]("author").get,
      doc.getAs[String]("message").get,
      doc.getAs[BSONDateTime]("created_at").map(bdt => new LocalDateTime(bdt.value)).get,
      doc.getAs[BSONObjectID]("_id").get.stringify)
  }

  implicit object TwiipBSONWriter extends BSONDocumentWriter[Twiip] {
    def write(twiip: Twiip) = {
      BSONDocument(
        "_id" -> BSONObjectID(twiip.id),
        "author" -> twiip.author,
        "created_at" -> BSONDateTime(twiip.createdAt.toDateTime().getMillis()),
        "message" -> twiip.message)
    }
  }
}
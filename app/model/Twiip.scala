package model

//mmi
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import reactivemongo.bson._
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.Future
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Twiip(author: String,
  message: String,
  createdAt: LocalDateTime = new LocalDateTime(),
  id: String = BSONObjectID.generate.stringify) {
  
  /**
   * Converts createdAt to printable date time.
   */
  def createdAtISO() : String = ISODateTimeFormat.dateTimeNoMillis().print(createdAt)
  
}

object Twiip {

  // mmd
  def db = ReactiveMongoPlugin.db
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  def collection = db.collection[BSONCollection]("Twiips")

  implicit val readsJodaLocalDateTime = Reads[LocalDateTime](js =>
    js.validate[String].map[LocalDateTime](dtString =>
      LocalDateTime.parse(dtString, ISODateTimeFormat.dateTimeNoMillis())))

  implicit val writesJodaLocalDateTime = Writes[LocalDateTime](dt =>
    JsString(ISODateTimeFormat.dateTimeNoMillis().print(dt)))

  implicit val twiipWrites = Json.writes[Twiip]
  implicit val twiipReads = Json.reads[Twiip]

  // Does the mapping BSON <-> Scala object
  implicit object MessageBSONReader extends BSONDocumentReader[Twiip] {
    def read(doc: BSONDocument): Twiip = Twiip(
      doc.getAs[String]("author").get,
      doc.getAs[String]("message").get,
      doc.getAs[BSONDateTime]("created_at").map(bdt => new LocalDateTime(bdt.value)).get,
      doc.getAs[BSONObjectID]("_id").get.stringify)
  }

  implicit object MatchBSONWriter extends BSONDocumentWriter[Twiip] {
    def write(writeMessage: Twiip) = {
      BSONDocument(
        "_id" -> BSONObjectID(writeMessage.id),
        "author" -> writeMessage.author,
        "created_at" -> BSONDateTime(writeMessage.createdAt.toDateTime().getMillis()),
        "message" -> writeMessage.message)
    }
  }
  
  // mfa3
  def findAll: Future[List[Twiip]] = findAll(None)

  // mfa1
  //  def findAll = {
  //    collection.find(BSONDocument()).sort(BSONDocument("_id" -> -1)).cursor[Message].collect[List]() 
  //  }

  // mfa2
  def findAll(author: Option[String]) = {
    val query = author match {
      case Some(author) => BSONDocument("author" -> author)
      case _ => BSONDocument()
    }
    collection.find(query).sort(BSONDocument("_id" -> -1)).cursor[Twiip].collect[List]()
  }

  // ms
  def save(twiit: Twiip) = {
    collection.insert(twiit)
  }
}
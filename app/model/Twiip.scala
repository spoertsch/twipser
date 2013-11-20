package model

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat

import play.api.Play.current
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsString
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.QueryOpts
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDateTimeIdentity
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentIdentity
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONIntegerHandler
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONObjectIDIdentity
import reactivemongo.bson.BSONStringHandler
import reactivemongo.bson.Producer.nameValue2Producer

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

  // mmd
  def db = ReactiveMongoPlugin.db
  def collection = db.collection[BSONCollection]("Twiips")

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

  // mfa3
  def findAll: Future[List[Twiip]] = findAll(None)

  // mfa1
  //  def findAll = {
  //    collection.find(BSONDocument()).sort(BSONDocument("created_at" -> -1)).options(QueryOpts().batchSize(10)).cursor[Message].collect[List]() 
  //  }

  // mfa2
  def findAll(author: Option[String]) = {
    val query = author match {
      case Some(author) => BSONDocument("author" -> author)
      case _ => BSONDocument()
    }
    collection.find(query).sort(BSONDocument("created_at" -> -1)).options(QueryOpts().batchSize(10)).cursor[Twiip].collect[List]()
  }

  def findById(id: String): Future[Option[Twiip]] = {
    collection.find(BSONDocument("_id" -> BSONObjectID(id))).one[Twiip]
  }
  
  def findNLatest(n: Int): Future[List[Twiip]] = {
    collection.find(BSONDocument()).sort(BSONDocument("created_at" -> -1)).options(QueryOpts().batchSize(n)).cursor[Twiip].collect[List](n)
  }

  // ms
  def save(twiit: Twiip) = {
    collection.insert(twiit)
  }
}
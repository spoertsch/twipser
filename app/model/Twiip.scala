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
import play.api.libs.json.Json
import model.TwiipImplicits._

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

  def db = ReactiveMongoPlugin.db
  def collection = db.collection[BSONCollection]("Twiips")


  def findAll: Future[List[Twiip]] = findAll(None)

  //  def findAll = {
  //    collection.find(BSONDocument()).sort(BSONDocument("created_at" -> -1)).options(QueryOpts().batchSize(10)).cursor[Message].collect[List]() 
  //  }

  def findAll(author: Option[String]) = {
    val query = author match {
      case Some(author) => BSONDocument("author" -> author)
      case _ => BSONDocument()
    }
    collection.find(query).sort(BSONDocument("created_at" -> -1)).cursor[Twiip].collect[List]()
  }

  def findById(id: String): Future[Option[Twiip]] = {
    collection.find(BSONDocument("_id" -> BSONObjectID(id))).one[Twiip]
  }
  
  def findNLatest(n: Int): Future[List[Twiip]] = {
    collection.find(BSONDocument()).sort(BSONDocument("created_at" -> -1)).options(QueryOpts().batchSize(n)).cursor[Twiip].collect[List](n)
  }

  def save(twiit: Twiip) = {
    collection.insert(twiit)
  }
}
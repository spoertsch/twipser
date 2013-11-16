package model

import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import reactivemongo.bson._
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.Future

case class Message(author: String, message: String, id: Option[String] = Some(BSONObjectID.generate.stringify)) 

object Message {
  
  def db = ReactiveMongoPlugin.db
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  def collection = db.collection[BSONCollection]("Messages")

  implicit val messageWrites = Json.writes[Message]
  implicit val messageReads = Json.reads[Message]
  
  // Does the mapping BSON <-> Scala object
  implicit object MessageBSONReader extends BSONDocumentReader[Message] {
    def read(doc: BSONDocument): Message = Message(
      doc.getAs[String]("author").get,      
      doc.getAs[String]("message").get,      
      doc.getAs[BSONObjectID]("_id").map(_.stringify))

  }

  implicit object MatchBSONWriter extends BSONDocumentWriter[Message] {
    def write(writeMessage: Message) = {
      val bson = BSONDocument(
        "_id" -> BSONObjectID(writeMessage.id.get),
        "author" -> writeMessage.author,
        "message" -> writeMessage.message)

      bson
    }
  }
  
  def findAll: Future[List[Message]] = findAll(None)
  
  def findAll(author: Option[String]) = {
    val query = author match {
      case Some(author) => BSONDocument("author" -> author)
      case _ => BSONDocument()
    }
    collection.find(query).sort(BSONDocument("_id" -> -1)).cursor[Message].collect[List]()
  }
  
  def save(message: Message) = {
    collection.insert(message)
  }
}
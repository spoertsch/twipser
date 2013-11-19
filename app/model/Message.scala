package model

//mmi
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import reactivemongo.bson._
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.Future

case class Message(author: String, message: String, id: String = BSONObjectID.generate.stringify)

object Message {

  // mmd
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
      doc.getAs[BSONObjectID]("_id").get.stringify)
  }

  implicit object MatchBSONWriter extends BSONDocumentWriter[Message] {
    def write(writeMessage: Message) = {
      BSONDocument(
        "_id" -> BSONObjectID(writeMessage.id),
        "author" -> writeMessage.author,
        "message" -> writeMessage.message)
    }
  }

  // mfa3
  def findAll: Future[List[Message]] = findAll(None)

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
    collection.find(query).sort(BSONDocument("_id" -> -1)).cursor[Message].collect[List]()
  }

  // ms
  def save(message: Message) = {
    collection.insert(message)
  }
}
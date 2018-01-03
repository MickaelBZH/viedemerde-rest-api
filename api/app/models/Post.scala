package models

import org.joda.time.DateTime

import play.api.data._
import play.api.data.Forms.{ text, longNumber, mapping, nonEmptyText, optional }
import play.api.data.validation.Constraints.pattern

import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson.{
  BSONDateTime, BSONDocument, BSONObjectID
}

case class Post(
  id: BSONObjectID,
  content: String,
  author: String,
  date: DateTime)

object Post {
  import play.api.libs.json._

  /*
  Post Json deserializer
  */

  implicit object PostWrites extends OWrites[Post] {
    def writes(post: Post): JsObject = Json.obj(
      "id" -> post.id.stringify,
      "content" -> post.content,
      "author" -> post.author,
      "date" -> post.date.toString("yyyy-MM-dd'T'HH:mm:ss'Z'"))
  }

  implicit object PostReads extends Reads[Post] {
    def reads(json: JsValue): JsResult[Post] = json match {
      case obj: JsObject => try {
        val id = (obj \ "_id").as[BSONObjectID]
        val content = (obj \ "content").as[String]
        val author = (obj \ "author").as[String]
        val date = (obj \ "date").as[BSONDateTime]
        JsSuccess(Post(id, content, author, new DateTime(date.value)))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }
}

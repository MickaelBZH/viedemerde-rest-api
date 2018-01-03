package controllers

import javax.inject.Inject

import java.text.SimpleDateFormat
import org.joda.time.DateTime

import scala.concurrent.Future

import play.api.Logger
import play.api.Play.current
import play.api.i18n.{ MessagesApi }
import play.api.mvc.{ Action, Controller, Request }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{ Json, JsObject, JsString }
import scala.util.Try

import play.modules.reactivemongo.{
  MongoController, ReactiveMongoApi, ReactiveMongoComponents
}

import reactivemongo.bson.{
  BSONDateTime, BSONDocument, BSONObjectID
}

import play.modules.reactivemongo.json._, ImplicitBSONHandlers._
import play.modules.reactivemongo.json.collection._

import models.Post, Post._

class PostsController @Inject() (
  val messagesApi: MessagesApi,
  val reactiveMongoApi: ReactiveMongoApi)
    extends Controller with MongoController with ReactiveMongoComponents {

  private val logger = Logger(getClass)

  // get the collection 'posts'
  val collection = db[JSONCollection](play.Play.application.configuration.getString("mongodb.collection"))

  def index(from: Option[String], to: Option[String], author: Option[String]) = Action.async { implicit request =>

    /*
    Returns all the posts
    Optional query parameters from and to for date range filtering: from=2017-01-01T00:00:00Z&to=2017-02-18T00:00:00Z
    Optional query parameter author for author filtering: author=Jean-Kevin
    */

    var query = Json.obj()
    val validationDateFrom: Try[DateTime] = Try(DateTime.parse(from.get))
    val validationDateTo: Try[DateTime] = Try(DateTime.parse(to.get))
    // check valid dates
    if((!from.isEmpty && validationDateFrom.isFailure) || (!to.isEmpty && validationDateTo.isFailure)){
      Future(BadRequest(Json.obj("message" -> "Date range not valid")))
    }

    else{
      // build filter query
      if(!from.isEmpty && !to.isEmpty){
        query = query.as[JsObject] + ("date" -> Json.obj("$gte" -> BSONDateTime(DateTime.parse(from.get).getMillis), "$lt" -> BSONDateTime(DateTime.parse(to.get).getMillis)))
      }
      if(!author.isEmpty){
        query = query.as[JsObject] + ("author" -> Json.toJson(author))
      }

      // the cursor of documents
      val found = collection.find(query).cursor[Post]
      // build (asynchronously) a list containing all the posts
      found.collect[List]().map { posts =>
        Ok(Json.toJson(Json.obj("posts" -> posts, "count" -> posts.size)))
      }.recover {
        case e =>
          e.printStackTrace()
          BadRequest(e.getMessage())
      }
    }
  }

  def show(id: String) = Action.async { implicit request =>

    /*
    Returns post by id
    */

    // id validation
    val validationId: Try[BSONObjectID] = BSONObjectID.parse(id)
    if(!validationId.isSuccess){
      Future(BadRequest(Json.obj("message" -> "Id not valid")))
    }
    // Returns the post or NotFound
    else{
      val post = collection.find(Json.obj("_id" -> BSONObjectID(id))).one[Post]
      post.map {
        posts => if (!posts.isEmpty) Ok(Json.toJson(posts)) else NotFound(Json.obj("message" -> "Post not found"))
      }.recover {
        case e =>
          e.printStackTrace()
          BadRequest(e.getMessage())
      }
    }
  }
}

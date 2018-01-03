import play.api.Play
import org.scalatestplus.play._
import org.scalatest._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import scala.concurrent.Await
import scala.concurrent.duration._
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

import controllers.PostsController
import models.Post, Post._
import play.api.libs.json.{ Json, JsObject, JsString }

import scala.util.Try

import play.modules.reactivemongo.json._, ImplicitBSONHandlers._
import play.modules.reactivemongo.json.collection._
import reactivemongo.bson.{
   BSONDocument, BSONObjectID, BSONDateTime
}

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import play.modules.reactivemongo.ReactiveMongoApi

class PostsControllerSpec extends PlaySpec with Results with BeforeAndAfterAll {

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]

  var app: FakeApplication = _

  override def beforeAll() {
    app = FakeApplication(additionalConfiguration = Map(("mongodb.collection" -> "posts_test")))
    Play.start(app)
    val collection = reactiveMongoApi.db[JSONCollection]("posts_test")

    // test posts
    val post1 = BSONDocument(
      "_id" -> BSONObjectID("142580dfa5f4cc604c85c588"),
      "content" -> "My post content 1",
      "author" -> "Alice",
      "date" -> BSONDateTime(DateTime.parse("2017-01-01T00:00:00").getMillis))
    val post2 = BSONDocument(
      "_id" -> BSONObjectID("132580dfa5f4cc604c85c588"),
      "content" -> "My post content 2",
      "author" -> "Bob",
      "date" -> BSONDateTime(DateTime.parse("2017-02-01T00:00:00").getMillis))

    Await.result(collection.insert(post1), atMost = Duration(1000, MILLISECONDS))
    Await.result(collection.insert(post2), atMost = Duration(1000, MILLISECONDS))
  }

  override def afterAll() {
    val collection = reactiveMongoApi.db[JSONCollection]("posts_test")
    Await.result(collection.remove(BSONDocument("_id" -> BSONObjectID("142580dfa5f4cc604c85c588"))), atMost = Duration(1000, MILLISECONDS))
    Await.result(collection.remove(BSONDocument("_id" -> BSONObjectID("132580dfa5f4cc604c85c588"))), atMost = Duration(1000, MILLISECONDS))
    Play.stop(app)
  }

  "Test PostsController" should {
    "get all posts" in {
        val request = FakeRequest("GET", "/api/posts")
        val apiResult = route(app, request).get
        status(apiResult) mustEqual 200
        val jsonResult = contentAsJson(apiResult)
        (jsonResult \ "count").as[Int] mustEqual 2
        val posts = (jsonResult \ "posts").as[List[JsObject]]
        posts mustBe a [List[_]]
        posts.foreach {
          post => (post \ "author").as[String].isEmpty mustBe false
                  (post \ "content").as[String].isEmpty mustBe false
                  (post \ "id").as[String].isEmpty mustBe false
                  (post \ "date").as[String].isEmpty mustBe false
                  Try(DateTime.parse((post \ "date").as[String])).isSuccess mustBe true
                  Try(BSONObjectID.parse((post \ "id").as[String])).isSuccess mustBe true
        }
    }
    "get all posts by author name" in {
        val request = FakeRequest("GET", "/api/posts?author=Bob")
        val apiResult = route(app, request).get
        status(apiResult) mustEqual 200
        val jsonResult = contentAsJson(apiResult)
        (jsonResult \ "count").as[Int] mustEqual 1
    }
    "get all posts by date range" in {
        val request = FakeRequest("GET", "/api/posts?from=2017-01-01T00:00:00Z&to=2017-01-18T00:00:00Z")
        val apiResult = route(app, request).get
        status(apiResult) mustEqual 200
        val jsonResult = contentAsJson(apiResult)
        (jsonResult \ "count").as[Int] mustEqual 1
    }
    "enter wrong date parameter" in {
      val request = FakeRequest("GET", "/api/posts?from=2017-010-01T00:00:00Z&to=2017-12-31T00:00:00Z")
      val apiResult = route(app, request).get
      status(apiResult) mustEqual 400
    }
    "get a single post" in {
        val request = FakeRequest("GET", "/api/posts/142580dfa5f4cc604c85c588")
        val apiResult = route(app, request).get
        status(apiResult) mustEqual 200
        val jsonResult = contentAsJson(apiResult)
        (jsonResult \ "author").as[String] mustEqual "Alice"
        (jsonResult \ "content").as[String] mustEqual "My post content 1"
        (jsonResult \ "id").as[String] mustEqual "142580dfa5f4cc604c85c588"
        (jsonResult \ "date").as[String] mustEqual "2017-01-01T00:00:00Z"
    }
    "get a non existing post" in {
        val request = FakeRequest("GET", "/api/posts/5a2580dfa5f4cc604c85c588")
        val apiResult = route(app, request).get
        status(apiResult) mustEqual 404
    }
  }
}

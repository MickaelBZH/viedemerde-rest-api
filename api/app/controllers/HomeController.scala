package controllers

import javax.inject.Inject

import play.api.mvc._

/**
  * A very small controller that renders a home page.
  */
class HomeController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }
}

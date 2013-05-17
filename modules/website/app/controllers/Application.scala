package controllers.website

import play.api._
import play.api.libs._
import play.api.libs.concurrent._
import play.api.libs.iteratee._
import play.api.libs.ws._
import play.api.libs.oauth._
import play.api.i18n.Messages
import play.api.mvc._

import com.bindscha.playmate.auth._
import controllers.common._

import models._

/**
 * <code>Application</code> contains the base controller for this application.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
object Application extends Controller
    with Auth
    with AuthConfigImpl
    with Authority 
    with DB {

  /**
   * GET index
   */
  def index = 
    UserAwareAction { implicit user => implicit request => 
      Ok(views.html.index())
    } { implicit request => 
      Redirect(controllers.website.routes.Users.login)
    }

  // -- Javascript routing

  def javascriptRoutes = Action { implicit request =>
    import controllers.website.routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRouter")()
    ).as("text/javascript")
  }

}
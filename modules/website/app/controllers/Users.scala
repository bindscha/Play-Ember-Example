package controllers.website

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.mvc.Results._
import play.api.Play.current
import play.api.templates._

import com.typesafe.plugin._

import com.bindscha.playmate.auth._
import controllers.common._

import models._

/**
 * User management controller for this application.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
object Users extends Controller
    with Auth
    with AuthConfigImpl
    with Authority 
    with LoginLogout
    with DB {

  import scala.slick.session.Session

  val LOGIN_FORM = Form {
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 8)
    ) {
        (email, password) =>
          withSession { implicit session => 
            DAL.user(email) filter { _.credentials.checkPassword(password) }
          }
      } {
        _ map (u => (u.email, ""))
      }.verifying("Invalid email or password", _.isDefined)
      .transform[User](_.get, Some(_))
  }

  def login = NoUserAction {
    implicit request => Ok(views.html.login())
  }

  def logout = DeauthenticateAction(
    "success" -> "You've been logged out"
  )

  def authenticate = Action { implicit request =>
    LOGIN_FORM.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => user.id match {
        case Some(id) => AuthenticateAction(id)(request)
        case None => Redirect(controllers.website.routes.Application.index)
      }
    )
  }

}

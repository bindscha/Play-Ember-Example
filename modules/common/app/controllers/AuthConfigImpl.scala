package controllers.common

import play.api._
import play.api.mvc._
import play.api.mvc.Results._

import scala.reflect.classTag
import scala.util.Try

import com.bindscha.playmate.auth._

import _root_.config._
import _root_.models._

/**
 * Implementation of required authentication configuration.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
trait AuthConfigImpl extends AuthConfig {
  self: Controller with DB =>

  // User auth

  type Id = Long

  val idClassTag = classTag[Id]

  type User = models.User

  type Authority = User => Boolean

  def user(id: Id): Option[User] =
    withSession { implicit session => DAL.user(id) }

  def loginSuccess(implicit requestHeader: RequestHeader): Result = {
    val uri = requestHeader.session.get("access_uri").getOrElse(Config.INDEX_URL) // TODO
    requestHeader.session - "access_uri"
    Redirect(uri)
  }

  def logoutSuccess(implicit requestHeader: RequestHeader): Result =
    Redirect(Call("GET", Config.INDEX_URL)) // TODO

  def authenticationFailure(implicit requestHeader: RequestHeader): Result =
    Redirect(Call("GET", Config.LOGIN_URL)).withSession("access_uri" -> requestHeader.uri) // TODO

  def authorizationFailure(implicit requestHeader: RequestHeader): Result =
    Forbidden("You do not have permission to perform this action!")

  def authorize(user: User, authority: Authority): Boolean =
    authority(user)

  override val sessionPersister: SessionPersister[Id] =
    new DBSessionPersister

  // API auth

  type ApiKey = models.ApiKey

  type KeyAuthority = ApiKey => Boolean

  def apiKey(str: String): Option[ApiKey] =
    Try(ApiKey(new LowerCaseString(str))).toOption

  def keyAuthenticationFailure(implicit requestHeader: RequestHeader): Result =
    Unauthorized("You do not have permission to access this resource!")

  def keyAuthorizationFailure(implicit requestHeader: RequestHeader): Result =
    Forbidden("You do not have permission to access this resource!")

  def authorizeKey(apiKey: ApiKey, authority: KeyAuthority): Boolean =
    authority(apiKey)

}

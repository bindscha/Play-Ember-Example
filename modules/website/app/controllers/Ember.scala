package controllers.website

import play.api._
import play.api.libs._
import play.api.libs.concurrent._
import play.api.libs.iteratee._
import play.api.libs.ws._
import play.api.libs.oauth._
import play.api.i18n._
import play.api.mvc._
import play.api.Play.current

import com.bindscha.playmate.auth._
import controllers.common._

import models._

/**
 * <code>Ember</code> contains the Ember-related controller for this application
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
object Ember extends Controller
    with Auth
    with AuthConfigImpl
    with Authority 
    with DB {
  
  /**
   * GET messages
   */
  def messages(locale: Option[String] = None, prefix: Option[String] = None) = 
    AuthorizedAction(isStandardUser) { implicit user => implicit request => 
      val chosenLocale = locale getOrElse Lang.preferred(Lang.availables).language
      val messagesMap = 
        Messages.messages getOrElse (chosenLocale, Messages.messages(current)(Lang.defaultLang.language))

      Ok(
        "CLDR.defaultLocale = '" + chosenLocale + "';\n\n" + 
        "Em.I18n.translations = {\n" +
        (for {
          (k, m) <- messagesMap
        } yield {
          "  " + "'" + k + "'" + ": " + "\"" + m + "\", "
        }).mkString("\n") +
        "\n}"
      ).as("text/javascript")
    }
  
}
package controllers.admin

import play.api._
import play.api.mvc._

/**
 * Administration area controller.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
object Admin extends Controller {

  // TODO: temporary
  def index = Action { implicit request =>
    Ok("admin")
  }
  
}

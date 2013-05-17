import play.api.{ Application, GlobalSettings }
import play.api.mvc._
import play.api.Play.current

/**
 * <code>Evolutions</code> contains database evolution settings.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
object Evolutions {

  val DEFAULT = models.DB.DAL

}
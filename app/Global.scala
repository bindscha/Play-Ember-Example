import play.api.{ Application, GlobalSettings }
import play.api.mvc._
import play.api.Play.current

// Use H2Driver to connect to an H2 database
import scala.slick.driver.{ H2Driver, PostgresDriver }
import scala.slick.session.Session

import models._
import models.DB._

/**
 * <code>Global</code> contains application-global settings.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
object Global extends GlobalSettings {

  // Called on application startup
  override def onStart(application: Application) {

    withTransaction { implicit session =>
      val ret = (for {
        user1 <- DAL.userNew(User(None, "L", "B", "lb@lakemind.com", UserCredentials("secret12"), Administrator)).toOption
        user2 <- DAL.userNew(User(None, "N", "S", "nsingh@gmail.com", UserCredentials("secret12"), Administrator)).toOption
        alarm <- DAL.alarmNew(Alarm(None, org.joda.time.DateTime.now, Low, Unresolved, "UNHAPPY", "Whatever")).toOption
      } yield (user1, user2, alarm))

      ret match {
        case Some(r) =>
          play.api.Logger.info("Inserted default data")
        case None =>
          play.api.Logger.info("Default data not inserted (already present or exception thrown)")
      }
    }

  }

}
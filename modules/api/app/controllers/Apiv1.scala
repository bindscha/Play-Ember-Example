package controllers.api

import play.api._
import play.api.mvc._
import play.api.Play.current

import com.bindscha.playmate.auth._
import com.bindscha.playmate.rest._
import controllers.common._

import models._

trait ACLImpl extends ACL {
  self: Controller with AuthConfigImpl with DB =>
  override def userAuthorized: Authority =
    (user: User) => user.role == StandardUser || user.role == Administrator

  override def keyAuthorized: KeyAuthority =
    (key: ApiKey) => true // TODO
}

trait ACL2Impl extends ACL2[Long] {
  self: Controller with AuthConfigImpl with DB =>
  override def userAuthorized: Long => Authority =
    (accountId: Long) => (user: User) => user.role == StandardUser || user.role == Administrator

  override def keyAuthorized: Long => KeyAuthority =
    (accountId: Long) => (key: ApiKey) => true // TODO
}

/**
 * Controller for alarm resource.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
object AlarmsController
    extends Controller
    with JsonRestResourceController2[Long, Long, Alarm]
    with ACL2Impl
    with AuthConfigImpl
    with DB
    with JsonFormat {

  override val resource = withSession { implicit session =>
    Resource2[Long, Long, Alarm]("alarm", (id: Long) => DAL.alarms, (id: Long, aid: Long) => DAL.alarm(aid), (id: Long, a: Alarm) => DAL.alarmNew(a), (id: Long, aid: Long, a: Alarm) => DAL.alarmIs(aid, a), (id: Long, aid: Long) => DAL.alarmDel(aid))
  }

  override val format = alarmFormat

}

package controllers.common

import models._

/**
 * Authorization functions.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
trait Authority {
  self: DB =>

  /**
   * Authority functions
   */

  def isStandardUser(user: User): Boolean =
    user.role == StandardUser || user.role == Administrator

  def isAdministrator(user: User): Boolean =
    user.role == Administrator

  def isValidApiKey(key: ApiKey): Boolean =
    true // TODO

}

object Authority
  extends Authority
  with DB

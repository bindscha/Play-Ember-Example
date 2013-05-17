package models

import scala.util.matching.Regex

import org.apache.commons.codec.digest.DigestUtils._
import org.mindrot.jbcrypt.BCrypt

import org.joda.time.{ DateTime, Interval }
import java.sql.Timestamp

import com.bindscha.scatter.Random
import com.bindscha.scatter.Regex.{ R_EMAIL, R_URL }

import config.Config

//
// User
//

case class User(
    id: Option[Long],
    firstName: String,
    lastName: String,
    email: String,
    credentials: UserCredentials,
    role: UserRole = StandardUser,
    dateCreated: DateTime = DateTime.now) {
  assert(id != null && (!id.isDefined || id.get > 0), "Invalid user identifier")
  assert(firstName != null && firstName.trim.length >= 0, "Invalid user first name")
  assert(lastName != null && lastName.trim.length >= 0, "Invalid user last name")
  assert(email != null && email.trim.length >= 0 && email.trim.matches(R_EMAIL), "Invalid user email")
  assert(credentials != null, "Invalid user credentials")
  assert(role != null, "Invalid user role")
  assert(dateCreated != null && dateCreated.isBefore(DateTime.now.plusSeconds(10)), "Invalid user date created")

  val fullName = firstName + " " + lastName
}

case class PasswordHash(value: String) {
  assert(value != null && value.trim.length >= 0, "Invalid password hash")
}

case class UserCredentials(passwordHash: PasswordHash) {
  assert(passwordHash != null, "Invalid user credentials")

  def checkPassword(candidate: String): Boolean = BCrypt.checkpw(candidate, passwordHash.value)

}

object UserCredentials {

  def apply(password: String): UserCredentials = UserCredentials(PasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(Config.PASSWORD_HASHING_LOG_ROUNDS))))

  implicit def passwordHash2string(passwordHash: PasswordHash) = passwordHash.value

}

sealed trait UserRole
case object Administrator extends UserRole
case object StandardUser extends UserRole

object UserRole {
  def toOrdinal(role: UserRole) =
    role match {
      case Administrator => 0
      case StandardUser => 1
    }
  def fromOrdinal(i: Int) =
    i match {
      case 0 => Administrator
      case _ => StandardUser
    }
}

case class ActivationCode(
    userId: Option[Long] = None, 
    code: LowerCaseString = new LowerCaseString(Random.randomAlphanumericString(Config.ACTIVATION_CODE_LENGTH)),
    dateCreated: DateTime = DateTime.now) {
  assert(userId != null && (!userId.isDefined || userId.get > 0), "Invalid user identifier")
  assert(code != null && code.value.length == Config.ACTIVATION_CODE_LENGTH, "Invalid activation code")
  assert(dateCreated != null && dateCreated.isBeforeNow, "Invalid activation code date")
}

case class PasswordResetCode(
    userId: Option[Long] = None, 
    code: LowerCaseString = new LowerCaseString(Random.randomAlphanumericString(Config.PASSWORD_RESET_CODE_LENGTH)),
    timeout: DateTime = DateTime.now.plusMillis(Config.PASSWORD_RESET_CODE_TIMEOUT.toMillis.toInt)) {
  assert(userId != null && (!userId.isDefined || userId.get > 0), "Invalid user identifier")
  assert(code != null && code.value.length == Config.PASSWORD_RESET_CODE_LENGTH, "Invalid password reset code")
  assert(timeout != null && timeout.isAfterNow, "Invalid timeout")
}

//
// Account
//

sealed trait AccountPlan
case object Free extends AccountPlan
case object Pro extends AccountPlan

object AccountPlan {
  def toOrdinal(plan: AccountPlan) =
    plan match {
      case Free => 0
      case Pro => 1
    }
  def fromOrdinal(i: Int) =
    i match {
      case 0 => Free
      case _ => Pro
    }
}

case class Account(
    id: Option[Long],
    plan: AccountPlan = Free,
    dateCreated: DateTime = DateTime.now) {
  assert(id != null && (!id.isDefined || id.get > 0), "Invalid account identifier")
  assert(plan != null, "Invalid account plan")
  assert(dateCreated != null && dateCreated.isBeforeNow, "Invalid account date created")
}

case class ApiKey(
    key: LowerCaseString = new LowerCaseString(Random.randomAlphanumericString(Config.API_KEY_LENGTH))) {
  assert(key != null && key.value.length == Config.API_KEY_LENGTH, "Invalid API key")
}

class LowerCaseString(s: String) extends Proxy {
  assert(s != null, "Invalid lower case string value")
  val self: String = s.toLowerCase
  def value = self
}

// TEMP STUFF

case class Alarm(
  id: Option[Long],
  date: DateTime,
  priority: AlarmPriority,
  status: AlarmStatus,
  code: String,
  description: String) {
  assert(id != null && (!id.isDefined || id.get > 0), "Invalid alarm identifier")
  assert(date != null, "Invalid alarm date")
  assert(priority != null, "Invalid alarm priority")
  assert(status != null, "Invalid alarm status")
  assert(code != null && code.trim.length > 0, "Invalid alarm code")
  assert(description != null && description.trim.length > 0, "Invalid alarm description")
}

sealed trait AlarmPriority
case object Low extends AlarmPriority
case object Medium extends AlarmPriority
case object High extends AlarmPriority
case object Urgent extends AlarmPriority

object AlarmPriority {
  def toOrdinal(priority: AlarmPriority) =
    priority match {
      case Urgent => 0
      case High => 1
      case Medium => 4
      case Low => 7
    }
  def fromOrdinal(i: Int) =
    i match {
      case 0 => Urgent
      case 1 => High
      case 4 => Medium
      case _ => Low
    }
}

sealed trait AlarmStatus
case object Unresolved extends AlarmStatus
case object Ongoing extends AlarmStatus
case object Resolved extends AlarmStatus

object AlarmStatus {
  def toOrdinal(status: AlarmStatus) =
    status match {
      case Unresolved => 0
      case Ongoing => 1
      case Resolved => 2
    }
  def fromOrdinal(i: Int) =
    i match {
      case 1 => Ongoing
      case 2 => Resolved
      case _ => Unresolved
    }
}


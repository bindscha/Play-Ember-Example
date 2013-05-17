package models

import scala.util.matching.Regex
import scala.util.{Try, Success, Failure}
import scala.util.control.Exception

import org.joda.time.{ DateTime, Interval }

import scala.slick.driver.ExtendedProfile
import scala.slick.lifted._

import TypeMapper._

/**
 * Database `Profile` for Data Access Layer
 */
trait Profile {
  val profile: ExtendedProfile
}

/**
 * Data Access Layer component for users
 */
trait UserComponent {
  self: Profile =>
  import profile.simple._

  /**
   * Users table
   *
   * Stores [[models.User]] items, which correspond to users in the application
   */
  object Users extends Table[User]("_USER_") {
    // Columns
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def firstName = column[String]("FIRST_NAME")
    def lastName = column[String]("LAST_NAME")
    def email = column[String]("EMAIL")
    def passwordHash = column[UserCredentials]("PASSWORD_HASH")
    def role = column[UserRole]("ROLE")
    def dateCreated = column[DateTime]("DATE_CREATED")

    // Default projection
    def * = id.? ~ firstName ~ lastName ~ email ~ passwordHash ~ role ~ dateCreated <> (User, User.unapply _)

    // Insert projection
    def forInsert = firstName ~ lastName ~ email ~ passwordHash ~ role ~ dateCreated <>
      ((f, l, e, p, r, d) => User(None, f, l, e, p, r, d),
        (u: User) => Some((u.firstName, u.lastName, u.email, u.credentials, u.role, u.dateCreated))) returning id

    // Query templates
    lazy val user = createFinderBy(_.id)
    lazy val userByEmail = createFinderBy(_.email)
  }

  /**
   * @return user for the given identifier
   */
  def user(id: Long)(implicit session: Session): Option[User] =
    Users.user.firstOption(id)

  /**
   * @return user for the given email address
   */
  def user(email: String)(implicit session: Session): Option[User] =
    Users.userByEmail.firstOption(email)

  /**
   * @return all users
   */
  def users(implicit session: Session): Set[User] =
    Query(Users).to[Set]

  /**
   * Add a new user
   *
   * User identifier will be ignored if present
   *
   * @return inserted user or exception
   */
  def userNew(user: User)(implicit session: Session): Try[User] =
    Try {
      val id = Users.forInsert.insert(user)
      user.copy(id = Some(id))
    }

  /**
   * Modify a user
   *
   * @return modified user or exception
   */
  def userIs(id: Long, user: User)(implicit session: Session): Try[User] =
    Try {
      Users.where(_.id === id).update(user)
      user
    }

  /**
   * Delete a user
   *
   * @return deleted user
   */
  def userDel(id: Long)(implicit session: Session): Option[User] =
    for {
      ret <- user(id)
    } yield {
      Exception.allCatch { Users.where(_.id === id).delete }
      ret
    }
}

/**
 * Data Access Layer component for accounts
 */
trait AccountComponent {
  self: Profile =>
  import profile.simple._

  /**
   * Accounts table
   *
   * Stores [[models.Account]] items, which correspond to accounts in the application
   */
  object Accounts extends Table[Account]("ACCOUNT") {
    // Columns
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def plan = column[AccountPlan]("PLAN")
    def dateCreated = column[DateTime]("DATE_CREATED")

    // Default projection
    def * = id.? ~ plan ~ dateCreated <> (Account, Account.unapply _)

    // Auto increment projection
    def forInsert = plan ~ dateCreated <>
      ((p, d) => Account(None, p, d),
        (a: Account) => Some((a.plan, a.dateCreated))) returning id

    // Query templates
    lazy val account = createFinderBy(_.id)
  }

  /**
   * @return account for the given identifier
   */
  def account(id: Long)(implicit session: Session): Option[Account] =
    Accounts.account.firstOption(id)

  /**
   * @return all accounts
   */
  def accounts(implicit session: Session): Set[Account] =
    Query(Accounts).to[Set]

  /**
   * Add a new account
   *
   * Account identifier will be ignored if present
   *
   * @return inserted account or exception
   */
  def accountNew(account: Account)(implicit session: Session): Try[Account] = 
    Try {
      val id = Accounts.forInsert.insert(account)
      account.copy(id = Some(id))
    }

  /**
   * Modify an account
   *
   * @return modified account or exception
   */
  def accountIs(id: Long, account: Account)(implicit session: Session): Try[Account] = 
    Try {
      Accounts.where(_.id === id).update(account)
      account
    }

  /**
   * Delete an account
   *
   * @return deleted account
   */
  def accountDel(id: Long)(implicit session: Session): Option[Account] = 
    for {
      ret <- account(id)
    } yield {
      Exception.allCatch { Accounts.where(_.id === id).delete }
      ret
    }
    
}

/**
 * Data Access Layer component for accounts <-> users mapping
 */
trait AccountUserXRefComponent {
  self: Profile with UserComponent with AccountComponent =>
  import profile.simple._

  /**
   * Junction table for accounts and users
   */
  object AccountsUsersXRef extends Table[(Long, Long)]("ACCOUNT_USER_XREF") {
    // Columns
    def accountId = column[Long]("ACCOUNT_ID")
    def userId = column[Long]("USER_ID")

    // Default projection
    def * = accountId ~ userId

    // Constraints
    def users = foreignKey("USERS_FK", userId, Users)(_.id)
    def accounts = foreignKey("ACCOUNT_FK", accountId, Accounts)(_.id)
    def pk = primaryKey("PK", (userId, accountId))

    // Query templates
    // Note: lazy val instead of val to prevent looping on object initially
    // QueryTemplates are due for an update, so this will not remain long
    lazy val userAccounts = for {
      userId <- Parameters[Long]
      xref <- AccountsUsersXRef if xref.userId === userId
      account <- Accounts if account.id === xref.accountId
    } yield account
    lazy val accountUsers = for {
      accountId <- Parameters[Long]
      xref <- AccountsUsersXRef if xref.accountId === accountId
      user <- Users if user.id === xref.userId
    } yield user
  }

  /**
   * @return all accounts associated to the given user identifier
   */
  def userAccounts(userId: Long)(implicit session: Session): Set[Account] =
    AccountsUsersXRef.userAccounts.to[Set](userId)

  /**
   * @return all users associated to the given account identifier
   */
  def accountUsers(accountId: Long)(implicit session: Session): Set[User] =
    AccountsUsersXRef.accountUsers.to[Set](accountId)

  /**
   * Associate user to account using given identifiers
   *
   * @return newly linked pair or exception
   */
  def accountUserLink(accountId: Long, userId: Long)(implicit session: Session): Try[Pair[Account, User]] =
    Try {
      AccountsUsersXRef.insert((accountId, userId))
      (account(accountId), user(userId)) match {
        case (Some(account), Some(user)) => (account, user)
        case _ => throw new RuntimeException("Unexpected problem returning newly linked account and user")
      }
    }

  /**
   * Dissociate user from account using given identifiers
   *
   * @return newly unlinked pair or exception
   */
  def accountUserUnlink(accountId: Long, userId: Long)(implicit session: Session): Option[Pair[Account, User]] =
    for {
      a <- account(accountId)
      u <- user(userId)
    } yield {
      Exception.allCatch { AccountsUsersXRef.where(xref => xref.accountId === accountId && xref.userId === userId).delete }
      (a, u)
    }

  /**
   * Associate account to user using given identifiers
   *
   * @return newly linked pair or exception
   */
  def userAccountLink(userId: Long, accountId: Long)(implicit session: Session): Try[Pair[User, Account]] =
    accountUserLink(accountId, userId) map { p => p.swap }

  /**
   * Dissociate account from user using given identifiers
   *
   * @return newly unlinked pair or exception
   */
  def userAccountUnlink(userId: Long, accountId: Long)(implicit session: Session): Option[Pair[User, Account]] =
    accountUserUnlink(accountId, userId) map { p => p.swap }
}

/**
 * Data Access Layer component for activation codes
 */
trait ActivationCodeComponent {
  self: Profile with UserComponent =>
  import profile.simple._

  /**
   * ActivationCodes table
   *
   * Stores [[models.ActivationCode]] items, which correspond to activation codes
   */
  object ActivationCodes extends Table[ActivationCode]("ACTIVATION_CODE") {
    // Columns
    def userId = column[Long]("USER_ID", O.PrimaryKey)
    def code = column[LowerCaseString]("CODE")
    def dateCreated = column[DateTime]("DATE_CREATED")

    // Default projection
    def * = userId.? ~ code ~ dateCreated <> (ActivationCode, ActivationCode.unapply _)

    // Insert projection
    def forInsert = code ~ dateCreated <>
      ((code, dateCreated) => ActivationCode(None, code, dateCreated), 
        (a: ActivationCode) => a.userId map { id => (a.code, a.dateCreated) }) returning userId

    // Constraints
    def users = foreignKey("ACTIVATION_CODE_USER_FK", userId, Users)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
    
    // Query templates
    lazy val activationCode = createFinderBy(_.userId)
  }

  /**
   * @return activation code for the given identifier
   */
  def activationCode(userId: Long)(implicit session: Session): Option[ActivationCode] =
    ActivationCodes.activationCode.firstOption(userId)

  /**
   * @return all activation codes
   */
  def activationCodes(implicit session: Session): Set[ActivationCode] =
    Query(ActivationCodes).to[Set]

  /**
   * Add a new activation code
   *
   * ActivationCode identifier will be ignored if present
   *
   * @return inserted activation code or exception
   */
  def activationCodeNew(userId: Long, activationCode: ActivationCode)(implicit session: Session): Try[ActivationCode] =
    Try {
      ActivationCodes.forInsert.insert(activationCode.copy(userId=Some(userId)))
      activationCode.copy(userId=Some(userId))
    }

  /**
   * Modify an activation code
   *
   * @return modified activation code or exception
   */
  def activationCodeIs(userId: Long, activationCode: ActivationCode)(implicit session: Session): Try[ActivationCode] =
    Try {
      ActivationCodes.where(_.userId === userId).update(activationCode.copy(userId=Some(userId)))
      activationCode
    }

  /**
   * Delete an activation code
   *
   * @return deleted activation code
   */
  def activationCodeDel(userId: Long)(implicit session: Session): Option[ActivationCode] =
    for {
      ret <- activationCode(userId)
    } yield {
      Exception.allCatch { ActivationCodes.where(_.userId === userId).delete }
      ret
    }
    
}

/**
 * Data Access Layer component for password reset codes
 */
trait PasswordResetCodeComponent {
  self: Profile with UserComponent =>
  import profile.simple._

  /**
   * PasswordResetCodes table
   *
   * Stores [[models.PasswordResetCode]] items, which correspond to password reset codes
   */
  object PasswordResetCodes extends Table[PasswordResetCode]("PASSWORD_RESET_CODE") {
    // Columns
    def userId = column[Long]("USER_ID", O.PrimaryKey, O.AutoInc)
    def code = column[LowerCaseString]("CODE")
    def timeout = column[DateTime]("TIMEOUT")

    // Default projection
    def * = userId.? ~ code ~ timeout <> (PasswordResetCode, PasswordResetCode.unapply _)

    // Insert projection
    def forInsert = userId ~ code ~ timeout <>
      ((userId, code, timeout) => PasswordResetCode(Some(userId), code, timeout), 
        (p: PasswordResetCode) => p.userId map { id => (id, p.code, p.timeout) }) returning userId

    // Constraints
    def users = foreignKey("PASSWORD_RESET_CODE_USER_FK", userId, Users)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
    
    // Query templates
    lazy val passwordResetCode = createFinderBy(_.userId)
  }

  /**
   * @return password reset code for the given identifier
   */
  def passwordResetCode(userId: Long)(implicit session: Session): Option[PasswordResetCode] =
    // TODO: there might be a more idiomatic way to do this
    Try { PasswordResetCodes.passwordResetCode.firstOption(userId) } match {
      case Success(Some(c)) => Some(c)
      case Success(None) => None
      case Failure(_) => 
        Exception.allCatch { PasswordResetCodes.where(_.userId === userId).delete }
        None
    }

  /**
   * @return all password reset codes
   */
  def passwordResetCodes(implicit session: Session): Set[PasswordResetCode] =
    Query(PasswordResetCodes).to[Set]

  /**
   * Add a new password reset code
   *
   * PasswordResetCode identifier will be ignored if present
   *
   * @return inserted password reset code or exception
   */
  def passwordResetCodeNew(userId: Long, passwordResetCode: PasswordResetCode)(implicit session: Session): Try[PasswordResetCode] =
    Try {
      PasswordResetCodes.forInsert.insert(passwordResetCode.copy(userId=Some(userId)))
      passwordResetCode.copy(userId=Some(userId))
    }

  /**
   * Modify a password reset code
   *
   * @return modified password reset code or exception
   */
  def passwordResetCodeIs(userId: Long, passwordResetCode: PasswordResetCode)(implicit session: Session): Try[PasswordResetCode] =
    Try {
      PasswordResetCodes.where(_.userId === userId).update(passwordResetCode.copy(userId=Some(userId)))
      passwordResetCode.copy(userId=Some(userId))
    }

  /**
   * Delete a password reset code
   *
   * @return deleted password reset code
   */
  def passwordResetCodeDel(userId: Long)(implicit session: Session): Option[PasswordResetCode] =
    for {
      ret <- passwordResetCode(userId)
    } yield {
      Exception.allCatch { PasswordResetCodes.where(_.userId === userId).delete }
      ret
    }
    
}

/**
 * Data Access Layer component for user session
 */
trait SessionComponent {
  self: Profile with UserComponent =>
  import profile.simple._
  
  import com.bindscha.playmate.auth.{Session => S}

  protected case class UserSession(
    userId: Long,
    session: S)

  /**
   * Sessions table
   *
   * Stores [[models.Session]] items, which correspond to sessions for users
   */
  object Sessions extends Table[UserSession]("_SESSION_") {
    // Columns
    def userId = column[Long]("USER_ID", O.PrimaryKey)
    def sessionId = column[String]("SESSION_ID")
    def timeout = column[DateTime]("TIMEOUT")

    // Default projection
    def * = userId ~ sessionId ~ timeout <> (
      (userId: Long, sessionId: String, timeout: DateTime) => UserSession(userId, S(Some(sessionId), timeout)),
      (userSession: UserSession) => userSession.session.id map { id => (userSession.userId, id, userSession.session.timeout) })

    // Constraints
    def users = foreignKey("SESSION_USER_FK", userId, Users)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
    def uniqueIds = index("SESSION_ID_UNIQUE", (sessionId), unique = true)

    // Query templates
    lazy val userSession = createFinderBy(_.userId)
    lazy val session = createFinderBy(_.sessionId)
    lazy val sessionUser = for {
      sessionId <- Parameters[String]
      session <- Sessions if session.sessionId === sessionId
      user <- Users if user.id === session.userId
    } yield user
  }

  /**
   * @return session for the given user identifier
   */
  def userSession(userId: Long)(implicit dbSession: Session): Option[S] = 
    Try { Sessions.userSession.firstOption(userId) } match {
      case Success(Some(c)) => Some(c.session)
      case Success(None) => None
      case Failure(_) => 
        Exception.allCatch { Sessions.where(_.userId === userId).delete }
        None
    }

  /**
   * @return all sessions
   */
  def userSessions(implicit session: Session): Set[S] =
    Query(Sessions).to[Set] map (_.session)

  /**
   * Assign a new user session to a user
   *
   * @return inserted session or exception
   */
  def userSessionNew(userId: Long, session: S)(implicit dbSession: Session): Try[S] =
    Try {
      Sessions.insert(UserSession(userId, session))
      session
    }

  /**
   * Modify a session
   *
   * @return modified session or exception
   */
  def userSessionIs(userId: Long, session: S)(implicit dbSession: Session): Try[S] =
    Try {
      Sessions.where(_.userId === userId).update(UserSession(userId, session))
      session
    }

  /**
   * Delete a session
   *
   * @return deleted session
   */
  def userSessionDel(userId: Long)(implicit dbSession: Session): Option[S] =
    for {
      ret <- userSession(userId)
    } yield {
      Exception.allCatch { Sessions.where(_.userId === userId).delete }
      ret
    }
    
  /**
   * @return session for the given session identifier
   */
  def session(sessionId: String)(implicit dbSession: Session): Option[S] =
    Try { Sessions.session.firstOption(sessionId) } match {
      case Success(Some(c)) => Some(c.session)
      case Success(None) => None
      case Failure(_) => 
        Exception.allCatch { Sessions.where(_.sessionId === sessionId).delete }
        None
    }
    
  /**
   * Delete a session
   *
   * @return deleted session
   */
  def sessionDel(sessionId: String)(implicit dbSession: Session): Option[S] =
    for {
      ret <- session(sessionId)
    } yield {
      Exception.allCatch { Sessions.where(_.sessionId === sessionId).delete }
      ret
    }
    
  /**
   * @return user for the given session identifier
   */
  def sessionUser(sessionId: String)(implicit dbSession: Session): Option[User] = 
    Try { Sessions.session.firstOption(sessionId) } match {
      case Success(Some(c)) => Sessions.sessionUser.firstOption(sessionId)
      case Success(None) => None
      case Failure(_) => 
        Exception.allCatch { Sessions.where(_.sessionId === sessionId).delete }
        None
    }
    
}

/**
 * Data Access Layer component for alarms
 */
trait AlarmComponent {
  self: Profile with UserComponent =>
  import profile.simple._

  /**
   * Alarms table
   *
   * Stores [[models.Alarm]] items, which correspond to alarms
   */
  object Alarms extends Table[Alarm]("ALARM") {
    // Columns
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def date = column[DateTime]("DATE")
    def priority = column[AlarmPriority]("PRIORITY")
    def status = column[AlarmStatus]("STATUS")
    def code = column[String]("CODE")
    def description = column[String]("DESCRIPTION")

    // Default projection
    def * = id.? ~ date ~ priority ~ status ~ code ~ description <> (Alarm, Alarm.unapply _)

    // Insert projection
    def forInsert = date ~ priority ~ status ~ code ~ description <>
      ((date, priority, status, code, description) => Alarm(None, date, priority, status, code, description), 
        (a: Alarm) => Some((a.date, a.priority, a.status, a.code, a.description))) returning id

    // Query templates
    lazy val alarm = createFinderBy(_.id)
  }

  /**
   * @return alarm for the given identifier
   */
  def alarm(id: Long)(implicit session: Session): Option[Alarm] =
    Alarms.alarm.firstOption(id)

  /**
   * @return all alarms
   */
  def alarms(implicit session: Session): Set[Alarm] =
    Query(Alarms).to[Set]

  /**
   * Add a new alarm
   *
   * Alarm identifier will be ignored if present
   *
   * @return inserted alarm or exception
   */
  def alarmNew(alarm: Alarm)(implicit session: Session): Try[Alarm] =
    Try {
      val id = Alarms.forInsert.insert(alarm)
      alarm.copy(id = Some(id))
    }

  /**
   * Modify an alarm
   *
   * @return modified alarm or exception
   */
  def alarmIs(id: Long, alarm: Alarm)(implicit session: Session): Try[Alarm] =
    Try {
      Alarms.where(_.id === id).update(alarm.copy(id=Some(id)))
      alarm
    }

  /**
   * Delete an alarm
   *
   * @return deleted alarm
   */
  def alarmDel(id: Long)(implicit session: Session): Option[Alarm] =
    for {
      ret <- alarm(id)
    } yield {
      Exception.allCatch { Alarms.where(_.id === id).delete }
      ret
    }
    
}

/**
 * Data Access Layer.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
class DAL(override val profile: ExtendedProfile)
  extends Profile
  with UserComponent
  with AccountComponent
  with AccountUserXRefComponent 
  with ActivationCodeComponent
  with PasswordResetCodeComponent
  with SessionComponent
  with AlarmComponent
  

package controllers.common

import play.api.cache.Cache
import play.api.Play._
import play.api.mvc.{Session => _, _}

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.util.Try

import org.joda.time.DateTime

import com.bindscha.playmate.auth.config.Config
import com.bindscha.playmate.auth._
import com.bindscha.scatter.Random

import models._

/**
 * `DBSessionPersister` persists sessions to the database system
 */
class DBSessionPersister extends SessionPersister[Long] {
  
  override def userSession(userId: Long): Option[Session] = 
    DB.withSession { implicit session => 
      DB.DAL.userSession(userId)
    }
  
  override def sessionUserId(sessionId: String): Option[Long] = 
    DB.withSession { implicit session => 
      DB.DAL.sessionUser(sessionId) flatMap (_.id)
    }
  
  override def userSessionNew(userId: Long, session: Session): Try[Session] = 
    DB.withSession { implicit dbSession => 
      DB.DAL.userSessionNew(userId, session.copy(id = Some(generateSessionId)))
    }

  override def userSessionIs(userId: Long, session: Session): Try[Session] = 
    DB.withSession { implicit dbSession => 
      DB.DAL.userSessionIs(userId, session)
    }
    
  override def userSessionDel(userId: Long): Option[Session] = 
    DB.withSession { implicit session => 
      DB.DAL.userSessionDel(userId)
    }
  
  override def withTransaction[A](f: => A) : A = DB.withTransaction(session => f)
    
  private def generateSessionId: String = 
    DB.withTransaction { implicit session => 
      @tailrec
      def generateSessionIdInner: String = {
        val table = "abcdefghijklmnopqrstuvwxyz1234567890-_.!~*'()"
        val token = Random.randomString(table)(Config.SESSION_ID_LENGTH)
        if(DB.DAL.sessionUser(token).isDefined) generateSessionIdInner else token
      }
      generateSessionIdInner
    }
  
}

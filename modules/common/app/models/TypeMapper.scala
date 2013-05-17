package models

import scala.util.matching.Regex

import scala.slick.lifted.{ MappedTypeMapper => Mapper }

/**
 * Database type mapper for various model components.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
object TypeMapper {

  implicit val datetime2timestamp =
    Mapper.base[org.joda.time.DateTime, java.sql.Timestamp](
      dt => new java.sql.Timestamp(dt.getMillis),
      ts => new org.joda.time.DateTime(ts.getTime))

  implicit val usercredentials2string =
    Mapper.base[UserCredentials, String](
      credentials => credentials.passwordHash.value,
      str => UserCredentials(PasswordHash(str)))

  implicit val userrole2int =
    Mapper.base[UserRole, Int](UserRole.toOrdinal, UserRole.fromOrdinal)

  implicit val lowercasestring2string =
    Mapper.base[LowerCaseString, String](_.value, new LowerCaseString(_))

  implicit val alarmpriority2int =
    Mapper.base[AlarmPriority, Int](AlarmPriority.toOrdinal, AlarmPriority.fromOrdinal)

  implicit val alarmstatus2int =
    Mapper.base[AlarmStatus, Int](AlarmStatus.toOrdinal, AlarmStatus.fromOrdinal)

  implicit val apikey2string =
    Mapper.base[ApiKey, String](_.key.value, s => ApiKey(new LowerCaseString(s)))

  implicit val accountplan2int =
    Mapper.base[AccountPlan, Int](AccountPlan.toOrdinal, AccountPlan.fromOrdinal)

}
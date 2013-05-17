package config

/**
 * <code>Config</code> contains application configuration.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
object Config {

  import play.api.Play.current
  import play.api.Play.configuration

  import scala.concurrent.duration._

  val DB_DEFAULT_DRIVER =
    configuration.getString("db.default.driver") getOrElse "org.h2.Driver"

  val API_KEY_LENGTH =
    configuration.getInt("account.apikey.length") getOrElse 64

  val ACTIVATION_CODE_LENGTH =
    configuration.getInt("user.activation_code.length") getOrElse 32

  val ACTIVATION_CODE_TIMEOUT =
    configuration.getMilliseconds("user.activation_code.timeout") map { _ millis } getOrElse (24 hours)

  val PASSWORD_HASHING_LOG_ROUNDS =
    configuration.getInt("user.password_hashing.log_rounds") getOrElse 12

  val PASSWORD_RESET_CODE_LENGTH =
    configuration.getInt("user.password_reset_code.length") getOrElse 32

  val PASSWORD_RESET_CODE_TIMEOUT =
    configuration.getMilliseconds("user.password_reset_code.timeout") map { _ millis } getOrElse (2 hours)

  val INDEX_URL = 
    configuration.getString("app.index.url") getOrElse "/"
    
  val LOGIN_URL = 
    configuration.getString("app.login.url") getOrElse "/login"
    
}
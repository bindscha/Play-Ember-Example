package models

/**
 * JSON formatting for various models.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
trait JsonFormat {

  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  
  import scala.util.matching.Regex
  
  implicit object AlarmPriorityFormat extends Format[AlarmPriority] {
    def reads(json: JsValue) : JsResult[AlarmPriority] = json.validate[Short] map (_.toInt) map AlarmPriority.fromOrdinal
    def writes(priority: AlarmPriority) : JsValue = JsNumber(AlarmPriority.toOrdinal(priority))
  }
  
  implicit object AlarmStatusFormat extends Format[AlarmStatus] {
    def reads(json: JsValue) : JsResult[AlarmStatus] = json.validate[Short] map (_.toInt) map AlarmStatus.fromOrdinal
    def writes(status: AlarmStatus) : JsValue = JsNumber(AlarmStatus.toOrdinal(status))
  }
  
  implicit val alarmFormat : Format[Alarm] = Json.format[Alarm]
  
}

object JsonFormat extends JsonFormat

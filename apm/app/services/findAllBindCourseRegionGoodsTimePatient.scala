package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region_goods_time_patient, patient}
import com.pharbers.models.request._
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.parseToken
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findAllBindCourseRegionGoodsTimePatient()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    var request_data: request = null
    var patientIdLst: List[bind_course_region_goods_time_patient] = Nil

    override val brick_name: String = "find all bind course region med time patient list"

     implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        patientIdLst = queryMultipleObject[bind_course_region_goods_time_patient](request_data, "patient_id")

        val rq = new request()
        rq.res = "patient"
        rq.incond = Some(in2c("_id", patientIdLst.map(_.patient_id)) :: Nil)
        rq.fmcond = Some(fm2c(0, 1000))

        val patientList: List[patient] = queryMultipleObject[patient](rq, "_id")

        val these = patientIdLst.iterator
        val those = patientList.iterator
        while (these.hasNext && those.hasNext){
            these.next().patient = Some(those.next())
        }
    }

    override def goback: model.RootObject = toJsonapi(patientIdLst)
}

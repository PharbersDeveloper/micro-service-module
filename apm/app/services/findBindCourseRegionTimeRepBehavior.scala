package services

import com.pharbers.pattern.common.PhToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region_time_rep_behavior, repbehaviorreport}
import com.pharbers.models.request.{fm2c, in2c, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindCourseRegionTimeRepBehavior()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind course region time rep_behavior_report list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var bindLst: List[bind_course_region_time_rep_behavior] = Nil

    override def prepare: Unit = request_data = {
        existToken(rq)
        formJsonapi[request](rq.body)
    }

    override def exec: Unit = bindLst = queryMultipleObject[bind_course_region_time_rep_behavior](request_data)

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "rep_behavior_report"
        request.fmcond = Some(fm2c(0, 1000))
        request.incond = Some(in2c("id", bindLst.map(_.rep_behavior_id)) :: Nil)
//        val resultStr = forward("123.56.179.133", "19101")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
        val resultStr = forward("apm_findrepbehavior", "9000")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
        val repBehaviorLst = formJsonapiLst[repbehaviorreport](decodeJson[model.RootObject](parseJson(resultStr)))

        bindLst = bindLst.map{ bind =>
            bind.repbehaviorreport = repBehaviorLst.find(_.id == bind.rep_behavior_id)
            bind
        }
    }

    override def goback: model.RootObject = toJsonapi(bindLst)
}
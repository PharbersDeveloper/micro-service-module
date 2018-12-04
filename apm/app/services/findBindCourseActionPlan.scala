package services

import com.pharbers.pattern.common.PhToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{actionplan, bind_course_action_plan}
import com.pharbers.models.request._
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindCourseActionPlan()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
    import io.circe.syntax._

    override val brick_name: String = "find bind course actionplan list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var planIdLst: List[bind_course_action_plan] = Nil
    var planLstStr: String = ""

    override def prepare: Unit = {
        existToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = planIdLst = queryMultipleObject[bind_course_action_plan](request_data)

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "action_plan"
        request.fmcond = Some(fm2c(0, 1000))
        request.incond = Some(in2c("_id", planIdLst.map(x => x.plan_id)) :: Nil)
//        planLstStr = forward("123.56.179.133", "19006")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
        planLstStr = forward("apm_findactionplan", "9000")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
    }

    override def goback: model.RootObject = decodeJson[model.RootObject](parseJson(planLstStr))
}
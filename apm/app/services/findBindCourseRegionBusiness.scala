package services

import com.pharbers.pattern.common.PhToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region_business, businessreport}
import com.pharbers.models.request.{in2c, fm2c, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindCourseRegionBusiness()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind course region business_report list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var bindLst: List[bind_course_region_business] = Nil

    override def prepare: Unit = request_data = {
        existToken(rq)
        formJsonapi[request](rq.body)
    }

    override def exec: Unit = bindLst = queryMultipleObject[bind_course_region_business](request_data)

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "business_report"
        request.fmcond = Some(fm2c(0, 1000))
        request.incond = Some(in2c("id", bindLst.map(_.business_id)) :: Nil)
//        val resultStr = forward("123.56.179.133", "19009")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
        val resultStr = forward("apm_findbusiness", "9000")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
        val businessLst = formJsonapiLst[businessreport](decodeJson[model.RootObject](parseJson(resultStr)))

        if(businessLst.length != bindLst.length) throw new Exception("Could not find specified business_report")

        val these = bindLst.iterator
        val those = businessLst.iterator
        while (these.hasNext && those.hasNext){
            these.next().businessreport = Some(those.next())
        }
    }

    override def goback: model.RootObject = toJsonapi(bindLst)
}
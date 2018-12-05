package services

import com.pharbers.pattern.common.PhToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.bind_course_region_rep
import com.pharbers.models.request.{fm2c, in2c, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindCourseRegionRep()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind course region representative list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var bindLst: List[bind_course_region_rep] = Nil
    var repLstStr: String = ""

    override def prepare: Unit = {
        existToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = bindLst = queryMultipleObject[bind_course_region_rep](request_data)

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "representative"
        request.fmcond = Some(fm2c(0, 1000))
        request.incond = Some(in2c("id", bindLst.map(_.rep_id)) :: Nil)
//        repLstStr = forward("123.56.179.133", "19011")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
        repLstStr = forward("findrepresentative", "9000")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
    }

    override def goback: model.RootObject = decodeJson[model.RootObject](parseJson(repLstStr))
}
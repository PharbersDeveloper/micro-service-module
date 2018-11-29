package services

import com.pharbers.pattern.common.PhToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region, region}
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindCourseRegion()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind course region list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var regionIdLst: List[bind_course_region] = Nil
    var regionLst: List[region] = Nil

    override def prepare: Unit = request_data = {
        parseToken(rq)
        formJsonapi[request](rq.body)
    }

    override def exec: Unit = regionIdLst = queryMultipleObject[bind_course_region](request_data)

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "region"

        regionLst = regionIdLst.map { x =>
            request.eqcond = None
            val ec = eqcond()
            ec.key = "id"
            ec.`val` = x.region_id
            request.eqcond = Some(List(ec))
//            val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            val str = forward("123.56.179.133", "18006")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            formJsonapi[region](decodeJson[model.RootObject](parseJson(str)))
        }
    }

    override def goback: model.RootObject = toJsonapi(regionLst)
}
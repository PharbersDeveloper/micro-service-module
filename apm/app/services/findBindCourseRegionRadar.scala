package services

import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region_radar, radarfigure}
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindCourseRegionRadar()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind course region radar_figure list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var radarIdLst: List[bind_course_region_radar] = Nil

    override def prepare: Unit = request_data = {
        parseToken(rq)
        formJsonapi[request](rq.body)
    }

    override def exec: Unit = radarIdLst = queryMultipleObject[bind_course_region_radar](request_data)

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "radar_figure"

        radarIdLst = radarIdLst.map { x =>
            request.eqcond = None
            val ec = eqcond()
            ec.key = "id"
            ec.`val` = x.radar_id
            request.eqcond = Some(List(ec))
//            val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            val str = forward("127.0.0.1", "18008")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            x.radarfigure = Some(formJsonapi[radarfigure](decodeJson[model.RootObject](parseJson(str))))
            x
        }
    }

    override def goback: model.RootObject = toJsonapi(radarIdLst)
}
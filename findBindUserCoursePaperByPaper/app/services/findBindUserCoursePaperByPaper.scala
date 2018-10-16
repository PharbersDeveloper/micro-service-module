package services

import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_user_course_paper, course}
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindUserCoursePaperByPaper()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
    import io.circe.syntax._

    override val brick_name: String = "find bind user course paper by paper"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var bind_data: bind_user_course_paper = null
    var course_data: course = null

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = bind_data = queryObject[bind_user_course_paper](request_data).get

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "course"
        val ec = eqcond()
        ec.key = "id"
        ec.`val` = bind_data.course_id
        request.eqcond = Some(List(ec))
        val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
        course_data = formJsonapi[course](decodeJson[model.RootObject](parseJson(str)))
    }

    override def goback: model.RootObject = toJsonapi(course_data)
}
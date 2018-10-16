package services

import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{course, paper}
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findPaperById()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find paper by id"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var paper_data: paper = null

    override def prepare: Unit = request_data = {
        parseToken(rq)
        formJsonapi[request](rq.body)
    }

    override def exec: Unit = paper_data =
            queryObject[paper](request_data).getOrElse(throw new Exception("Could not find specified paper"))

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        val ec = eqcond()
        ec.key = "paper_id"
        ec.`val` = paper_data.id
        request.res = "bind_user_course_paper"
        request.eqcond = Some(List(ec))
        val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
        val course = formJsonapi[course](decodeJson[model.RootObject](parseJson(str)))
        paper_data.course = Some(course)
    }

    override def goback: model.RootObject = toJsonapi(paper_data)
}
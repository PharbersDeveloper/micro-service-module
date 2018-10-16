package services

import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.jsonapi.model.{Attribute, Attributes}
import com.pharbers.jsonapi.model.JsonApiObject.StringValue
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region, bind_user_course_paper, paperinput}
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class pushPaperInputByCourse()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "push paper_input by course"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var bind_data: bind_user_course_paper = null
    var regionIdLst: List[bind_course_region] = Nil

    override def prepare: Unit = {
        parseToken(rq)
        bind_data = formJsonapi[bind_user_course_paper](rq.body)
    }

    override def exec: Unit = {
        val request = new request()
        request.res = "bind_course_region"
        val ec = eqcond()
        ec.key = "course_id"
        ec.`val` = bind_data.course_id
        request.eqcond = Some(List(ec))
        regionIdLst = queryMultipleObject[bind_course_region](request)
    }

    override def forwardTo(next_brick: String): Unit = {
        regionIdLst.foreach{ x =>
            val tmp = new paperinput
            tmp.paper_id = bind_data.paper_id
            tmp.region_id = x.region_id
            forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(tmp).asJson.noSpaces).check()
        }
    }

    override def goback: model.RootObject = model.RootObject(Some(
        model.RootObject.ResourceObject(
            `type` = "result",
            attributes = Some(
                Seq(Attribute("insert", StringValue("success"))).asInstanceOf[Attributes]
            )
        )
    ))
}
package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.bind_user_course_paper
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class pushBindUserCoursePaper()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "push bind user course paper"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var bind_data: bind_user_course_paper = null

    override def prepare: Unit = {
        bind_data = formJsonapi[bind_user_course_paper](rq.body)
    }

    override def exec: Unit = bind_data.id = insertObject(bind_data).get("_id").toString

    override def forwardTo(next_brick: String): Unit =
        forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(bind_data).asJson.noSpaces).check()

    override def goback: model.RootObject = toJsonapi(bind_data)
}

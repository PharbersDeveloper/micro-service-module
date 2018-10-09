package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_user_course_paper, paper}
import com.pharbers.models.service.auth
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.parseToken
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class pushPaper()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "push paper"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var insert_data: paper = null
    var auth: auth = null

    override def prepare: Unit = {
        auth = parseToken(rq)
        insert_data = formJsonapi[paper](rq.body)
    }

    override def exec: Unit = {
        insert_data.state = true
        insert_data.start_time = System.currentTimeMillis()
        insert_data.id = insertObject(insert_data).get("_id").toString
    }

    override def forwardTo(next_brick: String): Unit ={
        val bind_data: bind_user_course_paper = new bind_user_course_paper
        bind_data.user_id = auth.user.get.id
        bind_data.course_id = insert_data.course.get.id
        bind_data.paper_id = insert_data.id
        forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(bind_data).asJson.noSpaces).check()
    }

    override def goback: model.RootObject = toJsonapi(insert_data)
}
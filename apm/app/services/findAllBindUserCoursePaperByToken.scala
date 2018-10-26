package services

import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_user_course_paper, paper}
import com.pharbers.models.request.{eqcond, fmcond, incond, request}
import com.pharbers.models.service.auth
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import org.bson.types.ObjectId
import play.api.mvc.Request

case class findAllBindUserCoursePaperByToken()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind user course paper by token"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var auth_data: auth = null
    var paperIdLst: List[bind_user_course_paper] = Nil
    var paperLst: List[paper] = Nil

    override def prepare: Unit = auth_data = parseToken(rq)

    override def exec: Unit = {

    }

    override def forwardTo(next_brick: String): Unit = {
        val request = new request
        val str = forward("127.0.0.1", "9000")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
        paperLst = formJsonapiLst[paper](decodeJson[model.RootObject](parseJson(str)))

    }

    override def goback: model.RootObject = toJsonapi(paperLst)
}
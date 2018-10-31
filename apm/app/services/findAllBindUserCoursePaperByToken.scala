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
        val request = new request()
        val ec = eqcond()
        val fm = fmcond()
        fm.take = 1000
        ec.key = "user_id"
        ec.`val` = auth_data.user.get.id
        request.res = "bind_user_course_paper"
        request.eqcond = Some(List(ec))
        request.fmcond = Some(fm)
        paperIdLst = queryMultipleObject[bind_user_course_paper](request)
    }

    override def forwardTo(next_brick: String): Unit = {
        val request = new request
        request.res = "paper"

        var valList: List[Any] = Nil
        paperIdLst.foreach(x => valList = valList :+ x.paper_id)
        val fm = fmcond()
        fm.take = 1000
        request.fmcond = Some(fm)
        val in = incond()
        in.key = "_id"
        in.`val` = valList
        request.incond = Some(List(in))

        val str = forward("123.56.179.133", "18015")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
//        paperLst = formJsonapiLst[paper](decodeJson[model.RootObject](parseJson(str)))

    }

    override def goback: model.RootObject = toJsonapi(paperLst)
}
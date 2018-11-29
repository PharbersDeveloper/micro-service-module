package services

import com.pharbers.pattern.common.PhToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_user_course_paper, paper}
import com.pharbers.models.request._
import com.pharbers.models.service.auth
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findAllBindUserCoursePaperByToken()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

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
        val rq = new request()
        rq.res = "bind_user_course_paper"
        rq.eqcond = Some(eq2c("user_id", auth_data.user.get.id) :: Nil)
        rq.fmcond = Some(fm2c(0, 1000))
        paperIdLst = queryMultipleObject[bind_user_course_paper](rq).reverse
    }

    override def done: Option[String] = {
        if(paperIdLst.isEmpty) None
        else super.done
    }

    override def forwardTo(next_brick: String): Unit = {
        val request = new request
        request.res = "paper"
        request.fmcond = Some(fm2c(0, 1000))
        val valList = paperIdLst.map(_.paper_id)
        request.incond = Some(in2c("_id", valList) :: Nil)

        val str = forward("123.56.179.133", "18023")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
        paperLst = formJsonapiLst[paper](decodeJson[model.RootObject](parseJson(str)))
    }

    override def goback: model.RootObject = {
        if(paperLst.isEmpty) model.RootObject(Some(
            model.RootObject.ResourceObjects(Nil)
        ))
        else toJsonapi(paperLst)
    }
}
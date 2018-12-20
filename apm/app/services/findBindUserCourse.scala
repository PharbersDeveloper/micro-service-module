package services

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.models.request._
import com.pharbers.models.service.auth
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.PhToken
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.models.entity.{bind_user_course, course}
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}

case class findBindUserCourse()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind user course list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var auth_data: auth = null
    var courseIdLst: List[bind_user_course] = Nil
    var courseLstStr: String = ""

    override def prepare: Unit = auth_data = parseToken(rq)

    override def exec: Unit = {
        val request = new request()
        request.res = "bind_user_course"
        request.eqcond = Some(eq2c("user_id", auth_data.user.get.id) :: Nil)
        request.fmcond = Some(fm2c(0, 1000))

        courseIdLst = queryMultipleObject[bind_user_course](request)
    }

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "course"
        request.fmcond = Some(fm2c(0, 1000))
        request.incond = Some(in2c("id", courseIdLst.map(_.course_id)) :: Nil)
        courseLstStr = forward("123.56.179.133", "19102")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
//        courseLstStr = forward("apm_findcourse", "9000")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
    }

    override def goback: model.RootObject = decodeJson[model.RootObject](parseJson(courseLstStr))
}
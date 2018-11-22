package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.models.request._
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class pushBindTeacherStudentTimePaper()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import io.circe.syntax._

    override val brick_name: String = "push BindTeacherStudentTimePaper"
    var login_data: request = null
    var auth: String = ""

    override def prepare: Unit = {}

    override def exec: Unit = {
        auth = forward("123.56.179.133", "18025")(api + (cur_step + 1)).post(rq.body.asJson.noSpaces).check()
    }

    override def goback: model.RootObject = decodeJson[model.RootObject](parseJson(auth))
}
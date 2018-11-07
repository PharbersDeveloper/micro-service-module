package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.models.request._
import com.pharbers.pattern.frame._
import com.pharbers.pattern.frame.{Brick, forward}
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class login()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "login"
    var login_data: request = null
    var auth: String = ""

    override def prepare: Unit = {}

    override def exec: Unit = {
        auth = forward("123.56.179.133", "19001")(api + (cur_step + 1)).post(rq.body.asJson.noSpaces).check()
    }

    override def goback: model.RootObject = decodeJson[model.RootObject](parseJson(auth))
}
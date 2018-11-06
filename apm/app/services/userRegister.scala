package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.models.entity.auth.user
import com.pharbers.pattern.frame.{Brick, forward, _}
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class userRegister()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
    import io.circe.syntax._

    override val brick_name: String = "user register"
    var user_data: user = null

    override def prepare: Unit = {}

    override def exec: Unit = {
        val str = forward("127.0.0.1", "19002")(api + (cur_step + 1)).post(rq.body.asJson.noSpaces).check()
        user_data = formJsonapi[user](decodeJson[model.RootObject](parseJson(str)))
    }

    override def goback: model.RootObject = toJsonapi(user_data)
}
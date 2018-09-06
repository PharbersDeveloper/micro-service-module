package services

import io.circe.syntax._
import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.models.{auth, user}
import com.pharbers.pattern.request.request
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport

import com.pharbers.macros._
import com.pharbers.pattern.mongo.client_db_inst._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

case class login()(implicit val request: Request[model.RootObject]) extends Brick with CirceJsonapiSupport {
    override val brick_name: String = "verify email"

    var request_data: request = new request()
    var auth_data: auth = auth()

    override def prepare: Unit = request_data = formJsonapi[request](request.body)

    override def exec: Unit = queryObject[user](request_data) match {
        case Some(user) => auth_data.user = Some(user)
        case None => throw new Exception("email or password error")
    }

    override def done: Option[String] = super.done

    override def forwardTo(next_brick: String): Unit = {
        val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(auth_data).asJson.noSpaces)
        val result = decodeJson[model.RootObject](parseJson(str))
        auth_data.token = formJsonapi[auth](result).token
    }

    override def goback: model.RootObject = toJsonapi(auth_data)
}
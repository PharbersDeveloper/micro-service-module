package services

import java.util.Date

import com.pharbers.driver.PhRedisDriver
import play.api.mvc.Request
import com.pharbers.models.auth
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.macros._
import com.pharbers.sercuity.Sercurity
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

case class encryptToken()(implicit val rq: Request[model.RootObject]) extends Brick {
    override val brick_name: String = "encrypt token"

    var auth_data: auth = auth()

    override def prepare: Unit = auth_data = formJsonapi[auth](rq.body)

    override def exec: Unit = auth_data.token = {
        val rd = new PhRedisDriver()
        val user = auth_data.user.get
        val token = Sercurity.md5Hash(user.id + new Date().getTime)
        rd.addMap(token, "user_id", user.id)
        rd.addMap(token, "email", user.email)
        rd.addMap(token, "user_name", user.user_name)
        rd.expire(token, auth_data.token_expire)
        token
    }

    override def forwardTo(next_brick: String): Unit = {}

    override def goback: model.RootObject = toJsonapi(auth_data)

}
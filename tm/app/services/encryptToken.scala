package services

import com.pharbers.driver.PhRedisDriver
import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.service.auth
import org.bson.types.ObjectId

case class encryptToken()(implicit val rq: Request[model.RootObject]) extends Brick {
    override val brick_name: String = "encrypt token"

    var auth_data: auth = new auth()

    override def prepare: Unit = {
        auth_data = formJsonapi[auth](rq.body)
    }

    override def exec: Unit = auth_data.token = {
        val rd = new PhRedisDriver()
        val user = auth_data.user.get
        val token = ObjectId.get().toString
        rd.addMap(token, "user_id", user.id)
        rd.addMap(token, "email", user.email)
        rd.addMap(token, "user_name", user.user_name)
        rd.expire(token, auth_data.token_expire)
        token
    }

    override def goback: model.RootObject = toJsonapi(auth_data)

}
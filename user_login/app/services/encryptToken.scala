package services

import java.util.Date

import play.api.mvc.Request
import com.pharbers.models.auth
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._

import com.pharbers.macros._
import com.pharbers.sercuity.Sercurity
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

case class encryptToken()(implicit val request: Request[model.RootObject]) extends Brick {
    override val brick_name: String = "encrypt token"

    var auth_data: auth = auth()

    override def prepare: Unit = auth_data = formJsonapi[auth](request.body)

    override def exec: Unit = auth_data.token = Sercurity.md5Hash(auth_data.user.get.id + new Date().getTime)

    override def forwardTo(next_brick: String): Unit = {}

    override def goback: model.RootObject = toJsonapi(auth_data)

}
package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.auth.{bind_company_secret, secret}
import com.pharbers.models.request.{request, eq2c}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findPublicKeyByCompanyID()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("auth").get.asInstanceOf[DBTrait[TraitRequest]]

    override val brick_name: String = "find publickey by company id"
    var request_data: request = null
    var secret_data: secret = null

    override def prepare: Unit = request_data = formJsonapi[request](rq.body)

    override def exec: Unit = {
        val bind_data = queryObject[bind_company_secret](request_data)

        secret_data = bind_data.map{ bind =>
            val request = new request()
            request.res = "secret"
            request.eqcond = Some(eq2c("id", bind.secret_id) :: Nil)
            queryObject[secret](request).getOrElse(throw new Exception("secret not exist"))
        }.getOrElse(throw new Exception("secret not exist"))

        secret_data.private_key = ""
    }

    override def goback: model.RootObject = toJsonapi(secret_data)
}
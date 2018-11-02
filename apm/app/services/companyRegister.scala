package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.auth.{bind_company_secret, company, secret}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame.Brick
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import com.pharbers.security.cryptogram.rsa.RSA
import play.api.mvc.Request

case class companyRegister()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "company register"
    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("auth").get.asInstanceOf[DBTrait[TraitRequest]]
    var company_data: company = null

    override def prepare: Unit = company_data = formJsonapi[company](rq.body)

    override def exec: Unit = {
        company_data.id = insertObject[company](company_data).get("_id").toString

        val (puk, prk) = RSA().createKey()
        val secret_data = new secret()
        secret_data.`type` = "secret"
        secret_data.public_key = puk
        secret_data.private_key = prk
        secret_data.id = insertObject[secret](secret_data).get("_id").toString

        val bind_data = new bind_company_secret()
        bind_data.`type` = "bind_company_secret"
        bind_data.company_id = company_data.id
        bind_data.secret_id = secret_data.id

        insertObject[bind_company_secret](bind_data).get("_id").toString
    }

    override def goback: model.RootObject = toJsonapi(company_data)
}
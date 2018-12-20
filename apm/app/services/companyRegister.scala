package services

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}

case class companyRegister()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import io.circe.syntax._

    override val brick_name: String = "company register"
    var company_data: String = ""

    override def prepare: Unit = {}

    override def exec: Unit = {
//        company_data = forward("123.56.179.133", "19003")(api + (cur_step + 1)).post(rq.body.asJson.noSpaces).check()
        company_data = forward("companyregister", "9000")(api + (cur_step + 1)).post(rq.body.asJson.noSpaces).check()
    }

    override def goback: model.RootObject = decodeJson[model.RootObject](parseJson(company_data))
}
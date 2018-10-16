package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.jsonapi.model.{Attribute, Attributes}
import com.pharbers.jsonapi.model.JsonApiObject.StringValue
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.service.callapmr
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.parseToken
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class apm_call_r()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "apm_call_r"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: callapmr = null

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[callapmr](rq.body)
    }

    override def exec: Unit = {
        val result = forward("apm_r", "8000")("/tmist_training/" + request_data.paper_id).get
        if(!result.toString.contains("DONE")) throw new Exception("R call fails")
    }

    override def goback: model.RootObject = model.RootObject(Some(
        model.RootObject.ResourceObject(
            `type` = "result",
            attributes = Some(
                Seq(Attribute("call R", StringValue("success"))).asInstanceOf[Attributes]
            )
        )
    ))
}
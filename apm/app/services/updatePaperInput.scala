package services

import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.jsonapi.model.JsonApiObject.StringValue
import com.pharbers.jsonapi.model.{Attribute, Attributes}
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.paperinput
import com.pharbers.models.request.request
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class updatePaperInput()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "update paperinput"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var update_result: Int = 0

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = update_result = updateObject[paperinput](request_data)

    override def goback: model.RootObject = update_result match {
        case 1 =>
            model.RootObject(Some(
                model.RootObject.ResourceObject(
                    `type` = "result",
                    attributes = Some(
                        Seq(Attribute("update", StringValue("success"))).asInstanceOf[Attributes]
                    )
                )
            ))
        case _ => throw new Exception("update failed for paperinput")
    }
}
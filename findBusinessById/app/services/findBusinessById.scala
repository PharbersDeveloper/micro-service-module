package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.businessreport
import com.pharbers.models.request.request
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame.Brick
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBusinessById()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport{

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find businessreport by id"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var businessreport_data: businessreport = null

    override def prepare: Unit = {
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = businessreport_data =
            queryObject[businessreport](request_data).getOrElse(throw new Exception("Could not find specified business_report"))

    override def goback: model.RootObject = toJsonapi(businessreport_data)
}
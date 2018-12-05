package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.repbehaviorreport
import com.pharbers.models.request.request
import com.pharbers.mongodb.dbtrait.DBTrait

import com.pharbers.pattern.frame.Brick
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findRepBehaviorById()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport{

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "query multi repbehaviorreport by request"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var rep_behavior_data: List[repbehaviorreport] = Nil

    override def prepare: Unit = {
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = rep_behavior_data =
            queryMultipleObject[repbehaviorreport](request_data)//.getOrElse(throw new Exception("Could not find specified rep_behavior_report"))

    override def goback: model.RootObject = toJsonapi(rep_behavior_data)
}
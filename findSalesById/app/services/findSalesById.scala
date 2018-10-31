package services

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.models.entity.sales
import com.pharbers.pattern.frame.Brick
import com.pharbers.models.request.request
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}

case class findSalesById()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find sales by id"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var sales_data: sales = null

    override def prepare: Unit = {
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = sales_data = queryObject[sales](request_data).getOrElse(throw new Exception("Could not find specified sales"))

    override def goback: model.RootObject = toJsonapi(sales_data)
}

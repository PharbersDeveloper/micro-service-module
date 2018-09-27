package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.bind_course_region_goods_ym_sales
import com.pharbers.models.request.request
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindCourseRegionGoodsYmSales()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind course region med ym sales list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var salesIdLst: List[bind_course_region_goods_ym_sales] = Nil

    override def prepare: Unit = request_data = {
        parseToken(rq)
        formJsonapi[request](rq.body)
    }

    override def exec: Unit = salesIdLst = queryMultipleObject[bind_course_region_goods_ym_sales](request_data)

    override def goback: model.RootObject = toJsonapi(salesIdLst)
}
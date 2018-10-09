package services

import com.pharbers.http.HTTP
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region_goods_ym_sales, sales}
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindCourseRegionGoodsYmSales()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind course region med ym sales list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var salesIdLst: List[bind_course_region_goods_ym_sales] = Nil

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = salesIdLst = queryMultipleObject[bind_course_region_goods_ym_sales](request_data, sort= "ym")

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "sales"
        salesIdLst = salesIdLst.map { x =>
            request.eqcond = None
            val ec = eqcond()
            ec.key = "id"
            ec.`val` = x.sales_id
            request.eqcond = Some(List(ec))
//            val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            val str = forward("123.56.179.133", "18003")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            x.sales = Some(formJsonapi[sales](decodeJson[model.RootObject](parseJson(str))))
            x
        }
    }

    override def goback: model.RootObject = toJsonapi(salesIdLst)
}
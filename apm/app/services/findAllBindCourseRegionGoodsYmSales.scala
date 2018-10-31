package services

import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region_goods_ym_sales, sales}
import com.pharbers.models.request.{fmcond, incond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import org.bson.types.ObjectId
import play.api.mvc.Request

case class findAllBindCourseRegionGoodsYmSales()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    var request_data: request = null
    var salesIdLst: List[bind_course_region_goods_ym_sales] = Nil

    override val brick_name: String = "find all bind course region med ym sales list"

     implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        salesIdLst = queryMultipleObject[bind_course_region_goods_ym_sales](request_data, "sales_id")
        val request = new request()
        request.res = "sales"
        var valList: List[ObjectId] = Nil
        salesIdLst.foreach(x => valList = valList :+ new ObjectId(x.sales_id))
        val fm = fmcond()
        fm.take = 1000
        request.fmcond = Some(fm)
        val in = incond()
        in.key = "_id"
        in.`val` = valList.toSet
        request.incond = Some(List(in))
        val salesList: List[sales] = queryMultipleObject[sales](request, "_id")
        val these = salesIdLst.iterator
        val those = salesList.iterator
        while (these.hasNext && those.hasNext){
            these.next().sales = Some(those.next())
        }
    }

    override def goback: model.RootObject = toJsonapi(salesIdLst)
}

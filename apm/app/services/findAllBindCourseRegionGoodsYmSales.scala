package services

import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region_goods_ym_sales, sales}
import com.pharbers.models.request.{fmcond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findAllBindCourseRegionGoodsYmSales()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import io.circe.syntax._
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

    override def exec: Unit = salesIdLst = queryMultipleObject[bind_course_region_goods_ym_sales](request_data)

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "sales"
        var valList: List[String] = Nil
        salesIdLst.foreach(x => {
//            val in = incond()
//            in.key = "_id"
//            in.`val` = x.sales_id
//            valList = valList :+ in
            valList = valList :+ x.sales_id
        })
        val fm = fmcond()
        fm.take = 1000
        request.fmcond = Some(fm)
//        val in = incond()
//        in.key = "_id"
//        in.`val` = valList.toArray
//        request.incond = Some(valList)
//        val str = forward("123.56.179.133", "18003")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
//        val str = forward("127.0.0.1", "9000")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
        var salesList: List[sales] = queryMultipleObject[sales](request)
        salesList = salesList.groupBy(x => valList.toSet.contains(x.id))(true).sortBy(x => x.id)
        salesIdLst.sortBy(x => x.sales_id)
        salesIdLst.zip(salesList).foreach(x => {
            x._2.`type` = "sales"
            x._1.sales = Some(x._2)
        })
//        salesIdLst = salesIdLst.map { x =>
//            x.sales = Some(
//                formJsonapi[sales](decodeJson[model.RootObject](parseJson(str))))
//            x
//        }
    }

    override def goback: model.RootObject = toJsonapi(salesIdLst)
}

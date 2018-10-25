package services

import com.mongodb.casbah.Imports.DBObject
import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region_goods_ym_sales, sales}
import com.pharbers.models.request.{fmcond, incond, request}
import com.pharbers.mongodb.dbconnect.dbInstance
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import org.bson.types.ObjectId
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

    override def exec: Unit = salesIdLst = queryMultipleObject[bind_course_region_goods_ym_sales](request_data, "sales_id")

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "sales"
        var valList: List[ObjectId] = Nil
        salesIdLst.foreach(x => {
//            val in = incond()
//            in.key = "_id"
//            in.`val` = x.sales_id
//            valList = valList :+ in
            valList = valList :+ new ObjectId(x.sales_id)
        })
        val fm = fmcond()
        fm.take = 1000
        request.fmcond = Some(fm)
        val in = incond()
        in.key = "_id"
        in.`val` = valList.toSet
        request.incond = Some(List(in))
//        val str = forward("123.56.179.133", "18003")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
//        val str = forward("127.0.0.1", "9000")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()


//        val di = new dbInstance("pharbers_config/client_connect.xml")
//        val coll = di.getCollection("sales")


//        var conditions: List[DBObject] = Nil
//        valList.foreach(x =>
//            conditions = conditions :+ DBObject("_id" -> x)
//        )
//        val t = coll.find(DBObject("$or" -> conditions)).sort(DBObject("data" -> -1)).skip(0).take(200).toList




//        val conditions = DBObject("_id" -> DBObject("$in" -> valList.toSet))
//        val t = coll.find(conditions).sort(DBObject("data" -> -1)).skip(0).take(200).toList
//
//        println(t.length)



        var salesList: List[sales] = queryMultipleObject[sales](request, "_id")
        println(salesList.length)

//        salesList.sortBy(x => x.id)
//        salesIdLst.sortBy(x => x.sales_id)
//        salesIdLst.zip(salesList).foreach(x => {
//            x._2.`type` = "sales"
//            x._1.sales = Some(x._2)
//        })
//        salesIdLst = salesIdLst.map { x =>
//            x.sales = Some(
//                formJsonapi[sales](decodeJson[model.RootObject](parseJson(str))))
//            x
//        }
    }

    override def goback: model.RootObject = toJsonapi(salesIdLst)
}

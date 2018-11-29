package services

import java.util.Date

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region_goods_time_unit, unit}
import com.pharbers.models.request._
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.PhToken
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findAllBindCourseRegionGoodsTimeUnit()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    var request_data: request = null
    var unitIdLst: List[bind_course_region_goods_time_unit] = Nil

    override val brick_name: String = "find all bind course region med time unit list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    override def prepare: Unit = {
        val start1 = new Date().getTime
        existToken(rq)
        val end1 = new Date().getTime
        println("existToken" + (end1 - start1))
        val start2 = new Date().getTime
        request_data = formJsonapi[request](rq.body)
        val end2 = new Date().getTime
        println("formJsonapi" + (end2 - start2))
    }

    override def exec: Unit = {
        val start1 = new Date().getTime
        unitIdLst = queryMultipleObject[bind_course_region_goods_time_unit](request_data, "unit_id")
        val end1 = new Date().getTime
        println("queryMultipleObject" + (end1 - start1))

        val start2 = new Date().getTime
        val rq = new request()
        rq.res = "unit"
        rq.incond = Some(in2c("_id", unitIdLst.map(_.unit_id)) :: Nil)
        rq.fmcond = Some(fm2c(0, 1000))
        val end2 = new Date().getTime
        println("create req" + (end2 - start2))

        val start3 = new Date().getTime
        val unitList: List[unit] = queryMultipleObject[unit](rq, "_id")
        val end3 = new Date().getTime
        println("query unit lst" + (end3 - start3))

        val start4 = new Date().getTime
        val these = unitIdLst.iterator
        val those = unitList.iterator
        while (these.hasNext && those.hasNext){
            these.next().unit = Some(those.next())
        }
        val end4 = new Date().getTime
        println("iterator" + (end4 - start4))
    }

    override def goback: model.RootObject = {
        val b = unitIdLst.map(_.unit.get)
        val start = new Date().getTime
        val a = toJsonapi(unitIdLst)
        val end = new Date().getTime
        println("toJsonapi" + (end - start))
        println("length" + unitIdLst.length)

        a
    }
}

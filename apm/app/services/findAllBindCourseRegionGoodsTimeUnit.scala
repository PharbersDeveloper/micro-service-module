package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region_goods_time_unit, unit}
import com.pharbers.models.request._
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.parseToken
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findAllBindCourseRegionGoodsTimeUnit()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    var request_data: request = null
    var unitIdLst: List[bind_course_region_goods_time_unit] = Nil

    override val brick_name: String = "find all bind course region med time unit list"

     implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        unitIdLst = queryMultipleObject[bind_course_region_goods_time_unit](request_data, "unit_id")

        val rq = new request()
        rq.res = "unit"
        rq.incond = Some(in2c("_id", unitIdLst.map(_.unit_id)) :: Nil)
        rq.fmcond = Some(fm2c(0, 1000))

        val unitList: List[unit] = queryMultipleObject[unit](rq, "_id")

        val these = unitIdLst.iterator
        val those = unitList.iterator
        while (these.hasNext && those.hasNext){
            these.next().unit = Some(those.next())
        }
    }

    override def goback: model.RootObject = toJsonapi(unitIdLst)
}

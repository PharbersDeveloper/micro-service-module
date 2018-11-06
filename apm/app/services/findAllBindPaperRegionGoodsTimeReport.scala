package services

import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{apm_unit_report, bind_paper_region_goods_time_report}
import com.pharbers.models.request.{eqcond, fmcond, incond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import org.bson.types.ObjectId
import play.api.mvc.Request

case class findAllBindPaperRegionGoodsTimeReport()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind paper region goods time report list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var reportIdLst: List[bind_paper_region_goods_time_report] = Nil

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        reportIdLst = queryMultipleObject[bind_paper_region_goods_time_report](request_data, sort = "report_id")
        val request = new request()
        request.res = "report"
        var valList: List[ObjectId] = Nil
        reportIdLst.foreach(x => valList = valList :+ new ObjectId(x.report_id))
        val fm = fmcond()
        fm.take = 1000
        request.fmcond = Some(fm)
        val in = incond()
        in.key = "_id"
        in.`val` = valList.toSet
        request.incond = Some(List(in))
        val reportList: List[apm_unit_report] = queryMultipleObject[apm_unit_report](request, "_id")
        val these = reportIdLst.iterator
        val those = reportList.iterator
        while (these.hasNext && those.hasNext){
            these.next().apmreport = Some(those.next())
        }
    }

    override def goback: model.RootObject = toJsonapi(reportIdLst)
}
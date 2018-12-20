package services

import play.api.mvc.Request
import org.bson.types.ObjectId
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.models.request._
import com.pharbers.pattern.common.PhToken
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import com.pharbers.models.entity.{apm_unit_report, bind_paper_region_goods_time_report}

case class findAllBindPaperRegionGoodsTimeReport()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind paper region goods time report list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var reportIdLst: List[bind_paper_region_goods_time_report] = Nil

    override def prepare: Unit = {
        existToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        reportIdLst = queryMultipleObject[bind_paper_region_goods_time_report](request_data, sort = "report_id")

        val rq = new request()
        rq.res = "report"
        rq.fmcond = Some(fm2c(0, 1000))
        rq.incond = Some(in2c("_id", reportIdLst.map(x => new ObjectId(x.report_id))) :: Nil)
        val reportList: List[apm_unit_report] = queryMultipleObject[apm_unit_report](rq, "_id")

        val these = reportIdLst.iterator
        val those = reportList.iterator
        while (these.hasNext && those.hasNext){
            these.next().apmreport = Some(those.next())
        }
    }

    override def goback: model.RootObject = toJsonapi(reportIdLst)
}
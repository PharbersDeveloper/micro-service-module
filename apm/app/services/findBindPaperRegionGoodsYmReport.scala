package services

import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{apmreport, bind_paper_region_goods_ym_report}
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindPaperRegionGoodsYmReport()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
    import io.circe.syntax._

    override val brick_name: String = "find bind paper region goods ym report list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var reportIdLst: List[bind_paper_region_goods_ym_report] = Nil

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = reportIdLst = queryMultipleObject[bind_paper_region_goods_ym_report](request_data, sort = "ym").reverse

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "report"

        reportIdLst.map { x =>
            request.eqcond = None
            val ec = eqcond()
            ec.key = "id"
            ec.`val` = x.report_id
            request.eqcond = Some(List(ec))
//            val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            val str = forward("123.56.179.133", "18013")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            x.apmreport = Some(formJsonapi[apmreport](decodeJson[model.RootObject](parseJson(str))))
            x
        }
    }

    override def goback: model.RootObject = toJsonapi(reportIdLst)
}
package services.report

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.entity.tm_phase_report
import com.pharbers.models.request.request
import com.pharbers.models.service.report_table_data
import com.pharbers.pattern.frame.Brick
import play.api.mvc.Request
import services.parseToken

case class tableResource()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {

    override val brick_name: String = "find table with hosp product"

    var request_data: request = new request
    var table_data: report_table_data = new report_table_data

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        import com.pharbers.pattern.mongo.tm_report_db_inst._
        val report = queryObject[tm_phase_report](request_data) match {
            case Some(r) => r
            case None => throw new Exception("Could not find specified report")
        }

        table_data.columns = List(
            Map("label" -> "医院名称", "valuePath" -> "hosp_name", "align" -> "center", "sorted" -> false),
            Map("label" -> "产品名称", "valuePath" -> "prod_name", "align" -> "center", "sorted" -> false),
            Map("label" -> "分配代表", "valuePath" -> "rep_name", "align" -> "center", "sorted" -> false),
            Map("label" -> "时间分配(天)", "valuePath" -> "time", "align" -> "center"),
            Map("label" -> "预算分配", "valuePath" -> "budget", "align" -> "center", "cellComponent" -> "table-number-thousands"),
            Map("label" -> "市场潜力", "valuePath" -> "potential", "align" -> "center", "cellComponent" -> "table-number-thousands"),
            Map("label" -> "市场增长(%)", "valuePath" -> "market_growth", "align" -> "center", "cellComponent" -> "table-number-percent"),
            Map("label" -> "销售额", "valuePath" -> "sales", "align" -> "center", "cellComponent" -> "table-number-thousands"),
            Map("label" -> "销售增长(%)", "valuePath" -> "sales_growth", "align" -> "center", "cellComponent" -> "table-number-percent"),
            Map("label" -> "份额(%)", "valuePath" -> "share", "align" -> "center", "cellComponent" -> "table-number-percent"),
            Map("label" -> "份额增长(%)", "valuePath" -> "share_change", "align" -> "center", "cellComponent" -> "table-number-percent"),
            Map("label" -> "指标", "valuePath" -> "target", "align" -> "center", "cellComponent" -> "table-number-thousands"),
            Map("label" -> "指标达成率(%)", "valuePath" -> "achieve_rate", "align" -> "center", "cellComponent" -> "table-number-percent"),
            Map("label" -> "销售贡献率(%)", "valuePath" -> "contri_rate", "align" -> "center", "cellComponent" -> "table-number-percent")
        )
        table_data.columnsValue = report.reso_allocation_report("value").map(_.asInstanceOf[Map[String, Any]])
    }

    override def goback: model.RootObject = toJsonapi(table_data)
}

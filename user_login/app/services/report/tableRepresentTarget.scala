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

case class tableRepresentTarget()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {

    override val brick_name: String = "find table with represent target"

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
            Map("label" -> "代表名称", "valuePath" -> "rep_name", "align" -> "center", "sorted" -> false),
            Map("label" -> "负责指标", "valuePath" -> "potential", "align" -> "center", "cellComponent" -> "table-number-thousands"),
            Map("label" -> "当期销售额", "valuePath" -> "sales", "align" -> "center", "cellComponent" -> "table-number-thousands"),
            Map("label" -> "指标达成率(%)", "valuePath" -> "achieve_rate", "align" -> "center", "cellComponent" -> "table-number-percent"),
            Map("label" -> "预算比例(%)", "valuePath" -> "budget_rate", "align" -> "center", "cellComponent" -> "table-number-percent"),
            Map("label" -> "销售贡献率(%)", "valuePath" -> "contri_rate", "align" -> "center", "cellComponent" -> "table-number-percent"),
            Map("label" -> "工作天数", "valuePath" -> "workdays", "align" -> "center"),
            Map("label" -> "奖金", "valuePath" -> "target", "align" -> "center", "cellComponent" -> "table-number-thousands")
        )
        table_data.columnsValue = report.rep_ind_resos("value").map(_.asInstanceOf[Map[String, Any]])
    }

    override def goback: model.RootObject = toJsonapi(table_data)
}

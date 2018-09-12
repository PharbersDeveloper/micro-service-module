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

case class tableRepresentAbility()(implicit val rq: Request[model.RootObject])
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
            Map("label" -> "代表名称", "valuePath" -> "rep_name", "align" -> "center", "sorted" -> false),
            Map("label" -> "总能力值", "valuePath" -> "overall_val", "align" -> "center", "cellComponent" -> "table-number-thousands"),
            Map("label" -> "产品知识", "valuePath" -> "prod_knowledge_val", "align" -> "center"),
            Map("label" -> "销售技巧", "valuePath" -> "sales_skills_val", "align" -> "center"),
            Map("label" -> "增长", "valuePath" -> "prod_know", "align" -> "center"),
            Map("label" -> "工作积极性", "valuePath" -> "motivation_val", "align" -> "center")
        )
        table_data.columnsValue = report.rep_ability_report("value").map(_.asInstanceOf[Map[String, Any]])
    }

    override def goback: model.RootObject = toJsonapi(table_data)
}

package services.assess_report

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.entity.assess_report
import com.pharbers.models.request.request
import com.pharbers.models.service.{evaluation_line_intro_data, evaluation_line_data}
import com.pharbers.pattern.frame.Brick
import play.api.mvc.Request
import services.parseToken

case class evaluationLine()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken with dimension {

    override val brick_name: String = "find evaluation line data"

    var request_data: request = new request()
    var report_data: assess_report = new assess_report()
    var eld: evaluation_line_data = new evaluation_line_data()

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        import com.pharbers.pattern.mongo.tm_report_db_inst.db_inst
        queryObject[assess_report](request_data) match {
            case Some(data) => report_data = data
            case None => throw new Exception("Could not find specified report")
        }

        val score_results: List[Map[String, Any]] = report_data.assessment("score_results").asInstanceOf[List[Map[String, Any]]]
        val analysis_results: List[Map[String, Any]] = report_data.assessment("analysis_results").asInstanceOf[List[Map[String, Any]]]

        eld.report_analysi_val = getScore("report_analysi_val")(score_results, mapping)
        eld.market_insight_val = getScore("market_insight_val")(score_results, mapping)
        eld.target_setting_val = getScore("target_setting_val")(score_results, mapping)
        eld.strategy_execution_val = getScore("strategy_execution_val")(score_results, mapping)
        eld.resource_allocation_val = getScore("resource_allocation_val")(score_results, mapping)
        eld.plan_deployment_val = getScore("plan_deployment_val")(score_results, mapping)
        eld.leadership_val = getScore("leadership_val")(score_results, mapping)
        eld.intor = Some(analysis_results.map{x =>
            val clid = new evaluation_line_intro_data()
            clid.id = x("item").asInstanceOf[String].hashCode.toString
            clid.title = x("item").asInstanceOf[String]
            clid.desc = x("analysis_describe").asInstanceOf[List[String]]
            clid
        })
    }

    override def goback: model.RootObject = toJsonapi(eld)
}

package services.assess_report

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.entity.assess_report
import com.pharbers.models.request.request
import com.pharbers.models.service.evaluation_radar_data
import com.pharbers.pattern.frame.Brick
import play.api.mvc.Request
import services.parseToken

case class evaluationRadar()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken with dimension {

    override val brick_name: String = "find evaluation radar data"

    var request_data: request = new request()
    var report_data: assess_report = new assess_report()
    var erd: evaluation_radar_data = new evaluation_radar_data()

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

        erd.report_analysi_val = getScore("report_analysi_val")(score_results, mapping)
        erd.market_insight_val = getScore("market_insight_val")(score_results, mapping)
        erd.target_setting_val = getScore("target_setting_val")(score_results, mapping)
        erd.strategy_execution_val = getScore("strategy_execution_val")(score_results, mapping)
        erd.resource_allocation_val = getScore("resource_allocation_val")(score_results, mapping)
        erd.plan_deployment_val = getScore("plan_deployment_val")(score_results, mapping)
        erd.leadership_val = getScore("leadership_val")(score_results, mapping)
        erd.team_management_val = getScore("team_management_val")(score_results, mapping)
        erd.talent_train_val = getScore("talent_train_val")(score_results, mapping)
    }

    override def goback: model.RootObject = toJsonapi(erd)


}

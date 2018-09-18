package services.assess_report

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.service._
import com.pharbers.pattern.frame.Brick
import play.api.mvc.Request
import services.parseToken

case class assessReportLayout()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {

    override val brick_name: String = "find assess report layout"

    var layout_data: List[evaluation_layout] = Nil

    override def prepare: Unit = parseToken(rq)

    override def exec: Unit = {
        val evaluation1 = new evaluation_layout()
        evaluation1.component_name = "capability-radar"
        val crl = new capablity_radar_layout()
        crl.component_name = "capability-radar"
        evaluation1.radar = Some(crl)

        val evaluation2 = new evaluation_layout()
        evaluation2.component_name = "evaluation-title-card"
        val ecl = new evaluation_card_layout()
        ecl.component_name = "evaluation-title-card"
        evaluation2.evaluationcard = Some(ecl)

        val evaluation3 = new evaluation_layout()
        evaluation3.component_name = "capability-line"
        val ell = new evaluation_line_layout()
        ell.component_name = "capability-line"
        evaluation3.line = Some(ell)

        layout_data = evaluation1 :: evaluation2 :: evaluation3 :: Nil
    }

    override def goback: model.RootObject = toJsonapi(layout_data)
}

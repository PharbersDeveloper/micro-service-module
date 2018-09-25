package services.report

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.models.service.{dropdown_layout, report_card_layout, report_layout, report_table_layout}
import com.pharbers.pattern.frame.Brick
import play.api.mvc.Request
import services.parseToken
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

case class reportLayout()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {

    override val brick_name: String = "find report layout"

    var layout_data: List[report_layout] = Nil

    override def prepare: Unit = parseToken(rq)

    override def exec: Unit = {
        val overview1 = new report_layout()
        overview1.component_name = "which-result"
        val dd = new dropdown_layout()
        dd.id = "dropdown_layout_1"
        dd.whichpage = "index"
        dd.text = "整体销售表现"
        overview1.dropdown = Some(dd)
        val overview2 = new report_layout()
        overview2.component_name = "data-show-card"
        overview2.reportcard = Some(new report_card_layout())
        val overview3 = new report_layout()
        overview3.component_name = "result-table"
        overview3.reporttable = Some(new report_table_layout())

        layout_data = overview1 :: overview2 :: overview3 :: Nil
    }

    override def goback: model.RootObject = toJsonapi(layout_data)
}

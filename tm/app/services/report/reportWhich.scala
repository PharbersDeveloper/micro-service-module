package services.report

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.models.service.dropdown_layout
import com.pharbers.pattern.frame.Brick
import play.api.mvc.Request
import services.parseToken
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

case class reportWhich()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {

    override val brick_name: String = "find drop down list"

    var drop_down_lst: List[dropdown_layout] = Nil

    override def prepare: Unit = parseToken(rq)

    override def exec: Unit = {
        val dd1 = new dropdown_layout()
        dd1.id = "dropdown_layout_1"
        dd1.whichpage = "index"
        dd1.text = "整体销售表现"
        val dd2 = new dropdown_layout()
        dd2.id = "dropdown_layout_2"
        dd2.whichpage = "hosp-product"
        dd2.text = "医院-产品销售报告"
        val dd3 = new dropdown_layout()
        dd3.id = "dropdown_layout_3"
        dd3.whichpage = "represent-product"
        dd3.text = "代表-产品销售报告"
        val dd4 = new dropdown_layout()
        dd4.id = "dropdown_layout_4"
        dd4.whichpage = "resource"
        dd4.text = "资源投入与产出"
        val dd5 = new dropdown_layout()
        dd5.id = "dropdown_layout_5"
        dd5.whichpage = "represent-target"
        dd5.text = "代表指标与资源"
        val dd6 = new dropdown_layout()
        dd6.id = "dropdown_layout_6"
        dd6.whichpage = "represent-ability"
        dd6.text = "代表能力"

        drop_down_lst = dd1 :: dd2 :: dd3 :: dd4 :: dd5 :: dd6 :: Nil
    }

    override def goback: model.RootObject = toJsonapi(drop_down_lst)
}

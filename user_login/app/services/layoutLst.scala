package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.service.{alldecision, hospdecision, mgrdecision}
import com.pharbers.pattern.frame._
import play.api.mvc.Request

case class layoutLst()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {

    override val brick_name: String = "find layout lst"

    var layout_data: List[alldecision] = Nil

    override def prepare: Unit = parseToken(rq)

    override def exec: Unit = {
        val alldecision1 = new alldecision()
        alldecision1.component_name = "hospital-decision"
        alldecision1.hospitaldecison = Some(new hospdecision())
        val alldecision2 = new alldecision()
        alldecision2.component_name = "manager-decision"
        alldecision2.managerdecision = Some(new mgrdecision())

        layout_data = alldecision1 :: alldecision2 :: Nil
    }

    override def goback: model.RootObject = toJsonapi(layout_data)
}
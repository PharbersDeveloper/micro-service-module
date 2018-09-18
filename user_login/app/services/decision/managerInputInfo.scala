package services.decision

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.pattern.frame.Brick
import com.pharbers.models.service.managerinputinfo
import play.api.mvc.Request
import services.parseToken

case class managerInputInfo()(implicit val rq: Request[model.RootObject])
    extends Brick with CirceJsonapiSupport with parseToken {
    override val brick_name: String = "find manager input info"

    var managerinputinfo_data = new managerinputinfo()

    override def prepare: Unit = parseToken(rq)

    override def exec: Unit = {
        managerinputinfo_data.id = "manager_input_info_1"
        managerinputinfo_data.total_day = 123
    }

    override def goback: model.RootObject = toJsonapi(managerinputinfo_data)
}

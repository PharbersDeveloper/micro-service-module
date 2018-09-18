package services.decision

import io.circe.syntax._
import com.pharbers.macros._
import play.api.mvc.Request
import services.parseToken
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame.{Brick, forward}
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.models.service.{allot_result, report_result}

case class taskAllot()(implicit val rq: Request[model.RootObject])
    extends Brick with CirceJsonapiSupport with parseToken {
    override val brick_name: String = "task allot"

    var rr = new report_result()
    var ar = new allot_result()

    override def prepare: Unit = {
        ar = formJsonapi[allot_result](rq.body)
        parseToken(rq)
    }

    override def exec: Unit = ar.uuid = "5b643430e53d3732b00047ea"

    override def forwardTo(next_brick: String): Unit = {
        val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(ar).asJson.noSpaces)
        println(str)
        rr = formJsonapi[report_result](decodeJson[model.RootObject](parseJson(str)))
    }

    override def goback: model.RootObject = toJsonapi(rr)
}

package services.decision

import services.parseToken
import play.api.mvc.Request
import com.pharbers.macros._
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame.{Brick, forward}
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.models.service.{allot_result, report_result}

case class callR()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {
    override val brick_name: String = "call R Job"

    var rr = new report_result()
    var uuid = ""

    override def prepare: Unit = uuid = formJsonapi[allot_result](rq.body).uuid

    override def exec: Unit = {
        val result = forward("123.56.179.133", "18001")("/stp_handler/" + uuid)
                        .get.toString()
//        val result = "{\"outcome\":[\"Done\"],\"report_id\":[\"5b643430e53d3732b00047ea\"], \"assess_report_id\":[\"5ba079e3e6a8a9000964d624\"]}"
        val head = "{\"data\":{\"type\": \"report-result\",\"id\": \"uuid\",\"attributes\":"
        val last = "}}"
        rr = formJsonapi[report_result](decodeJson[model.RootObject](parseJson(head + result + last)))
    }

    override def goback: model.RootObject = toJsonapi(rr)
}

package services.decision

import services.parseToken
import play.api.mvc.Request
import com.pharbers.macros._
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame.{Brick, forward}
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.models.service.report_result

case class callR()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {
    override val brick_name: String = "call R Job"

    var rr = new report_result()
    var uuid = ""

    override def prepare: Unit = uuid = "0542e4c6-b5a8-4518-b7e3-9867c19f2222"

    override def exec: Unit = {
        println("alksdjfalkj")
        //        val result = forward("123.56.179.133", "18001")("/stp_handler/" + uuid)
        //                .get.toString()
        val result = "{\"outcome\":[\"Done\"],\"report_id\":[\"5ba079e2e6a8a9000964d622\"], \"assess_report_id\":[\"5ba079e3e6a8a9000964d624\"]}"
        val head = "{\"data\":{\"type\": \"report-result\",\"id\": \"uuid\",\"attributes\":"
        val last = "}}"
        rr = formJsonapi[report_result](decodeJson[model.RootObject](parseJson(head + result + last)))
        println(rr)
    }

    override def goback: model.RootObject = toJsonapi(rr)
}

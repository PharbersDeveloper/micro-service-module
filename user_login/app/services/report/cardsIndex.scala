package services.report

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.models.entity.tm_phase_report
import com.pharbers.models.request.request
import com.pharbers.models.service.report_card
import com.pharbers.pattern.frame.Brick
import play.api.mvc.Request
import services.parseToken
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

case class cardsIndex()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {

    override val brick_name: String = "find cards with index"

    var request_data: request = new request
    var card_lst: List[report_card] = Nil

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        import com.pharbers.pattern.mongo.tm_report_db_inst._
        val report = queryObject[tm_phase_report](request_data) match {
            case Some(r) => r
            case None => throw new Exception("Could not find specified report")
        }

        card_lst = report.summary_report("overview").map(_.asInstanceOf[Map[String, Any]]).map{ x =>
            val rc = new report_card
            rc.index = x.getOrElse("index", 0).asInstanceOf[Int]
            rc.title = x.getOrElse("title", "").asInstanceOf[String]
            rc.form = x.getOrElse("form", "").asInstanceOf[String]
            rc.value = x.getOrElse("value", null)
            rc.ext = x.get("ext").orNull
            rc
        }
    }

    override def goback: model.RootObject = toJsonapi(card_lst)
}

package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.entity.{representative, scenario}
import com.pharbers.models.service.repinputinfo
import com.pharbers.pattern.frame._
import com.pharbers.pattern.request._
import play.api.mvc.Request
import com.pharbers.pattern.mongo.client_db_inst._

case class queryRepByScen()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {

    override val brick_name: String = "find representative by scenario"

    var request_data: request = new request()
    var rep_lst: List[repinputinfo] = Nil

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        implicit val any2Lst: Any => List[Map[String, Any]] = any => any.asInstanceOf[List[Map[String, Any]]]
        val scenario_data = queryObject[scenario](request_data).getOrElse(throw new Exception("Could not find specified scenario"))
        val connect_rep = scenario_data.current("connect_rep")
        val dest_goods_rep = scenario_data.current("dest_goods_rep")
        val total_day = scenario_data.current("connect_reso")
                .find(_ ("form") == "day")
                .map(_("relationship").asInstanceOf[Map[String, Int]])
                .map(_ ("value")).getOrElse(-1)
        val rep_ids = connect_rep.map(_ ("id").asInstanceOf[String])

        rep_lst = rep_ids.map { rep_id =>
            val rp_info = new repinputinfo()
            rp_info.intro = formatRepIntro(connect_rep, rep_id)
            rp_info.total_days = total_day
            rp_info.used_days = formatRepUserDay(dest_goods_rep, rep_id)
            rp_info.repInfo = findRepInfo(rep_id)
            rp_info
        }
    }

    override def goback: model.RootObject = toJsonapi(rep_lst)

    def formatRepIntro(connect_rep: List[Map[String, Any]], rep_id: String): String =
        connect_rep.find(x => x("id") == rep_id).get("relationship")
                .asInstanceOf[Map[String, List[Map[String, Any]]]]("value")
                .map(y => y("index") + "." + y("news"))
                .mkString(", ")

    def formatRepUserDay(dest_goods_rep: List[Map[String, Any]], rep_id: String): Int =
        dest_goods_rep.filter(x => x("rep_id") == rep_id)
                .map(_ ("relationship").asInstanceOf[Map[String, Any]])
                .map(_ ("user_input_day").asInstanceOf[Int])
                .sum

    def findRepInfo(rep_id: String): Option[representative] = {
        val request = new request()
        request.res = "representative"
        val ec = eqcond()
        ec.key = "id"
        ec.`val` = rep_id
        request.eqcond = Some(List(ec))
        queryObject[representative](request)
    }
}
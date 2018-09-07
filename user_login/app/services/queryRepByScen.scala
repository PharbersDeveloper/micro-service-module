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

    def formatRepInfo(rp_info: repinputinfo, rep_id: String): repinputinfo = {
        val request = new request()
        request.res = "representative"
        val ec = eqcond()
        ec.key = "id"
        ec.`val` = rep_id
        request.eqcond = Some(List(ec))
        val rep = queryObject[representative](request).get
        rp_info.repInfo = Some(rep)
        rp_info
    }

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

    override def exec: Unit = {
        val scenario_data = queryObject[scenario](request_data)
        val connect_rep = scenario_data.get.current("connect_rep").asInstanceOf[List[Map[String, Any]]]
        val dest_goods_rep = scenario_data.get.current("dest_goods_rep").asInstanceOf[List[Map[String, Any]]]
        val total_day = scenario_data.get.current("connect_reso").asInstanceOf[List[Map[String, Any]]]
                .find(_ ("form") == "day").get("relationship").asInstanceOf[Map[String, Int]]("value")
        val rep_ids = connect_rep.map(_ ("id").asInstanceOf[String])

        rep_lst = rep_ids.map { rep_id =>
            val rp_info = new repinputinfo()
            rp_info.intro = formatRepIntro(connect_rep, rep_id)
            rp_info.total_days = total_day
            rp_info.used_days = formatRepUserDay(dest_goods_rep, rep_id)
            formatRepInfo(rp_info, rep_id)
        }
    }

    override def goback: model.RootObject = toJsonapi(rep_lst)
}
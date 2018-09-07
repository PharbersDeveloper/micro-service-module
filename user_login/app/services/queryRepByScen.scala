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

    override val brick_name: String = "find representative id by scenario"

    var request_data: request = new request()
    var rep_lst: List[repinputinfo] = Nil

//    "intro": "医学院校临床专业毕业,是一位善于发现客户需求，善于探查客户心理。但最近由于同事得到提升而垂头丧气，导致对个人未来发展感到茫然",
//    "total_days": 120,
//    "used_days": 33,

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        val scenario_data = queryObject[scenario](request_data)
        val connect_rep = scenario_data.get.current("connect_rep").asInstanceOf[List[Map[String, Any]]]
        val rep_ids = connect_rep.map(_("id").asInstanceOf[String])

        val request = new request()
        request.res = "representative"
        val fm = fmcond()
        request.fmcond = Some(fm)
        rep_lst = rep_ids.map { rep_id =>
            val tmp = new repinputinfo()
            val ec = eqcond()
            ec.key = "id"
            ec.`val` = rep_id
            request.eqcond = Some(List(ec))
            val rep = queryObject[representative](request).get
            tmp.rep_info = Some(rep)
            tmp
        }
    }

    override def goback: model.RootObject = toJsonapi(rep_lst)
}
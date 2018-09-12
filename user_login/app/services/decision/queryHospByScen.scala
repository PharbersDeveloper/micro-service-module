package services.decision

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.models.entity.{hospital, medicine, representative, scenario}
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.models.service.{hospitalbaseinfo, hospmedicinfo}
import com.pharbers.pattern.frame.Brick
import play.api.mvc.Request
import services.parseToken
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.pattern.mongo.client_db_inst._

case class queryHospByScen()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {

    override val brick_name: String = "find hospital detail by scenario"

    var request_data: request = new request()
    var hosp_detail_lst: List[hospitalbaseinfo] = Nil

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        implicit val any2Lst: Any => List[Map[String, Any]] = any => any.asInstanceOf[List[Map[String, Any]]]
        val scenario_data = queryObject[scenario](request_data).getOrElse(throw new Exception("Could not find specified scenario"))
        val dest_goods_rep = scenario_data.current("dest_goods_rep")
        val hosp_ids = scenario_data.current("connect_dest").map(_ ("id").asInstanceOf[String])
        hosp_detail_lst = hosp_ids.map { hosp_id =>
            val hosp_detail = new hospitalbaseinfo()
            hosp_detail.hospital = findHospDetail(hosp_id)
            hosp_detail.id = hosp_id
            hosp_detail.hospmedicinfos = findConnMed(scenario_data, hosp_id)
            hosp_detail.representative = findConnRep(dest_goods_rep, hosp_id)
            hosp_detail
        }
    }

    override def goback: model.RootObject = toJsonapi(hosp_detail_lst)

    def findHospDetail(hosp_id: String): Option[hospital] = {
        val request = new request()
        request.res = "dest"
        val ec = eqcond()
        ec.key = "id"
        ec.`val` = hosp_id
        request.eqcond = Some(List(ec))
        queryObject[hospital](request)
    }

    def findConnMed(scenario_data: scenario, hosp_id: String): Option[List[hospmedicinfo]] = {
        implicit val any2Lst: Any => List[Map[String, Any]] = any => any.asInstanceOf[List[Map[String, Any]]]
        val currnet_phase = scenario_data.current("phase").asInstanceOf[Int]
        val dest_goods = scenario_data.current("dest_goods").filter(_ ("dest_id") == hosp_id)
        val total_budget = scenario_data.current("connect_reso")
                .find(_ ("form") == "money")
                .map(_("relationship").asInstanceOf[Map[String, Long]])
                .map(_ ("value")).getOrElse(-1L)
        val pre_current = scenario_data.past.find(_ ("phase") == currnet_phase - 1)
                .getOrElse(throw new Exception("not next phase of data"))
        val pre_dest_goods = pre_current("dest_goods").filter(_ ("dest_id") == hosp_id)
        val pre_dest_goods_rep = pre_current("dest_goods_rep").filter(_ ("dest_id") == hosp_id)
        val med_ids = dest_goods.map(_ ("goods_id").asInstanceOf[String])

        Some(
            med_ids.map { med_id =>
                val hospmedicinfo = new hospmedicinfo()
                hospmedicinfo.id = med_id
                hospmedicinfo.pre_target = calcPreTarget(med_id, pre_dest_goods_rep)
                hospmedicinfo.total_budget = total_budget
                val med_detail = findMedDetail(med_id).getOrElse(throw new Exception("Could not find specified medicine"))
                hospmedicinfo.prod_category = med_detail.prod_category
                hospmedicinfo.prod_name = med_detail.prod_name
                hospmedicinfo.overview = getOverriew(med_id, dest_goods, pre_dest_goods)
                hospmedicinfo.history = hospmedicinfo.history ++ Map("columnsValue" -> findMedHistory(med_id, scenario_data.past))
                hospmedicinfo.detail = hospmedicinfo.detail ++ Map("columnsValue" -> findMedDetail(med_id, dest_goods))
                hospmedicinfo
            }
        )
    }

    def findConnRep(dest_goods_rep: List[Map[String, Any]], hosp_id: String): Option[representative] =
        dest_goods_rep.filter(_ ("dest_id") == hosp_id)
                .map(_ ("rep_id").asInstanceOf[String])
                .map(findRepDetail)
                .filter(_.isDefined)
                .map(_.get).distinct match {
            case head :: _ => Some(head)
            case _ => None
        }

    def calcPreTarget(med_id: String, pre_dest_goods_rep: List[Map[String, Any]]): Long = {
        pre_dest_goods_rep.filter(_ ("goods_id") == med_id)
                .map(_ ("relationship").asInstanceOf[Map[String, Any]])
                .map(_ ("user_input_target").asInstanceOf[Long])
                .sum
    }

    def findMedDetail(med_id: String): Option[medicine] = {
        val request = new request()
        request.res = "goods"
        val ec = eqcond()
        ec.key = "id"
        ec.`val` = med_id
        request.eqcond = Some(List(ec))
        queryObject[medicine](request)
    }

    def getOverriew(med_id: String,
                    dest_goods: List[Map[String, Any]],
                    pre_dest_goods: List[Map[String, Any]]): List[Map[String, Any]] = {
        val data = dest_goods.find(_ ("goods_id") == med_id)
                .map(_("relationship").asInstanceOf[Map[String, Any]])
                .getOrElse(throw new Exception("Could not find specified medicine"))
        val pr_data = pre_dest_goods.find(_ ("goods_id") == med_id)
                .map(_("relationship").asInstanceOf[Map[String, Any]])
                .getOrElse(throw new Exception("Could not find specified medicine"))

        List(
            Map(
                "key" -> "药品市场潜力",
                "value" -> data("potential").asInstanceOf[Long]
            ),
            Map(
                "key" -> "增长潜力",
                "value" -> data("potential_growth").asInstanceOf[Double]
            ),
            Map(
                "key" -> "上期销售额",
                "value" -> pr_data("sales").asInstanceOf[Long]
            ),
            Map(
                "key" -> "上期增长",
                "value" -> pr_data("sales_growth").asInstanceOf[Double]
            ),
            Map(
                "key" -> "份额",
                "value" -> pr_data("share").asInstanceOf[Double]
            ),
            Map(
                "key" -> "上期贡献率",
                "value" -> pr_data("contri_rate").asInstanceOf[Double]
            )
        )
    }

    def findRepDetail(rep_id: String): Option[representative] = {
        val request = new request()
        request.res = "representative"
        val ec = eqcond()
        ec.key = "id"
        ec.`val` = rep_id
        request.eqcond = Some(List(ec))
        queryObject[representative](request)
    }

    def findMedHistory(med_id: String, past: List[Map[String, Any]]): List[Map[String, Any]] = {
        past.map { phase_data =>
            val phase = phase_data("phase")
            val dest_goods_rep = phase_data("dest_goods_rep").asInstanceOf[List[Map[String, Any]]]
                    .find(_("goods_id") == med_id)
                    .getOrElse(throw new Exception("Could not find specified medicine"))
            val rlsp = dest_goods_rep("relationship").asInstanceOf[Map[String, Any]]

            Map(
                "time" -> ("周期" + phase.toString),
                "rep_name" -> findRepDetail(dest_goods_rep("rep_id").asInstanceOf[String]).get.rep_name,
                "use_day" -> rlsp("user_input_day"),
                "use_budget" -> (rlsp("user_input_money") + " / " + rlsp("budget_proportion")),
                "target" -> (rlsp("user_input_target") + " / " + rlsp("target_growth") + " / " + rlsp("achieve_rate"))
            )
        }
    }

    def findMedDetail(med_id: String, dest_goods: List[Map[String, Any]]): List[Map[String, Any]] = {
        val compete_goods_id = dest_goods.find(_ ("goods_id") == med_id)
                .map(_("relationship").asInstanceOf[Map[String, Any]]("compete_goods"))
                .getOrElse(throw new Exception("Could not find specified medicine"))
                .asInstanceOf[List[Map[String, Any]]]
                .map(_ ("goods_id").asInstanceOf[String])
        compete_goods_id.map { goods_id =>
            val tmp = findMedDetail(goods_id).get
            Map(
                "prod_name" -> tmp.prod_name,
                "launch_time" -> tmp.launch_time,
                "insure_type" -> tmp.insure_type,
                "research_type" -> tmp.research_type,
                "ref_price" -> tmp.ref_price
            )
        }
    }

}

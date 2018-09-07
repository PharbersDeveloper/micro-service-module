package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.entity.{hospital, medicine, representative, scenario}
import com.pharbers.models.service.repinputinfo
import com.pharbers.pattern.frame._
import com.pharbers.pattern.mongo.client_db_inst._
import com.pharbers.pattern.request._
import entity.{hospitalbaseinfo, hospmedicinfo}
import play.api.mvc.Request

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
        val scenario_data = queryObject[scenario](request_data)
        val connect_dest = scenario_data.get.current("connect_dest").asInstanceOf[List[Map[String, Any]]]
        val dest_goods_rep = scenario_data.get.current("dest_goods_rep").asInstanceOf[List[Map[String, Any]]]
        val hosp_ids = connect_dest.map(_ ("id").asInstanceOf[String])
        hosp_detail_lst = hosp_ids.take(1).map { hosp_id =>
            val hosp_detail = new hospitalbaseinfo()
            hosp_detail.hospital = None //findHospDetail(hosp_id)
            hosp_detail.id = hosp_id
            hosp_detail.hospmedicinfos = findConnMed(scenario_data.get, hosp_id)
            hosp_detail.representatives = None //findConnRep(dest_goods_rep, hosp_id)
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

    def findMedDetail(med_id: String): Option[medicine] = {
        val request = new request()
        request.res = "goods"
        val ec = eqcond()
        ec.key = "id"
        ec.`val` = med_id
        request.eqcond = Some(List(ec))
        queryObject[medicine](request)
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

    def getOverriew(hosp_id: String, med_id: String)
                   (dest_goods: List[Map[String, Any]],
                      pre_dest_goods: List[Map[String, Any]]): List[Map[String, Any]] = {
        val data = dest_goods.find(x => x("dest_id") == hosp_id && x("goods_id") == med_id)
                .get("relationship").asInstanceOf[Map[String, Any]]
        val pr_data = pre_dest_goods.find(x => x("dest_id") == hosp_id && x("goods_id") == med_id)
                .get("relationship").asInstanceOf[Map[String, Any]]

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

    def calcPreTarget(): Int = 100

    def findConnMed(scenario_data: scenario, hosp_id: String): Option[List[hospmedicinfo]] = {
        val currnet_phase = scenario_data.current("phase").asInstanceOf[Int]
        val pre_dest_goods = scenario_data.past.find(_ ("phase") == currnet_phase - 1)
                .get("dest_goods").asInstanceOf[List[Map[String, Any]]]
                .filter(_ ("dest_id") == hosp_id)
        val dest_goods = scenario_data.current("dest_goods").asInstanceOf[List[Map[String, Any]]]
        val med_ids = dest_goods.filter(_ ("dest_id") == hosp_id).map(_ ("goods_id").asInstanceOf[String])

        Some(
            med_ids.map { med_id =>
                val hospmedicinfo = new hospmedicinfo()
                hospmedicinfo.id = med_id
                hospmedicinfo.pre_target = calcPreTarget()
                hospmedicinfo.prod_category = findMedDetail(med_id).get.prod_category
                hospmedicinfo.overview = getOverriew(hosp_id, med_id)(dest_goods, pre_dest_goods)
                hospmedicinfo.history = hospmedicinfo.history
                hospmedicinfo.detail = hospmedicinfo.detail
                findMedDetail(med_id)
                hospmedicinfo
            }
        )
    }

    def findConnRep(dest_goods_rep: List[Map[String, Any]], hosp_id: String): Option[List[representative]] =
        Some(
            dest_goods_rep.filter(_ ("dest_id") == hosp_id)
                    .map(_ ("rep_id").asInstanceOf[String])
                    .map(findRepDetail)
                    .filter(_.isDefined)
                    .map(_.get).distinct
        )

}
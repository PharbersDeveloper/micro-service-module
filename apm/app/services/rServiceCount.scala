package services

import akka.japi.Option
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity._
import com.pharbers.models.entity.rCount.{answer, models}
import com.pharbers.models.{request => requestObj}
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

import scala.collection.mutable

case class rServiceCount ()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "r count"

    var bind_paper_region_goods_ym_report_lst: List[bind_paper_region_goods_ym_report] = Nil
    val ym = "18-q1"
    val beforeYm = "17-q4"
    var paperinputLst: List[paperinput] = Nil
    var answerMap:Map[String, answer] = _  //  region_id
    var competLst: List[String] = Nil  //compet_id
    var modelMap:Map[String, Map[Double, Double]] = _  //region_id
    var goodsSalesMap: Map[String, sales] = _  //goods*ym*region_id
    var paperId = ""
    var goodsId = ""
    var courseId = ""
    var salesSum: Double = 0
    override def prepare: Unit = {
        paperinputLst = {
            implicit val db_client: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
            val request_data = formJsonapi[request](rq.body)
            queryMultipleObject[paperinput](request_data)
        }

        if(paperinputLst.nonEmpty) paperId = paperinputLst.head.paper_id

        courseId = {
            implicit val db_client: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
            val requestData = new request()
            val eq = eqcond()
            eq.key = "region_id"
            eq.`val` = paperinputLst.head.region_id
            requestData.eqcond = Some(List(eq))
            requestData.res = "bind_course_region"
            queryObject[bind_course_region](requestData).getOrElse(new bind_course_region()).course_id
        }

        goodsId = {
            implicit val db_client: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
            val requestData = new request()
            val eq = eqcond()
            eq.key = "course_id"
            eq.`val` = courseId
            requestData.eqcond = Some(List(eq))
            requestData.res = "bind_course_goods"
            queryObject[bind_course_goods](requestData).getOrElse(new bind_course_goods()).goods_id
        }

        competLst = {
            implicit val db_client: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
            val requestData = new request()
            val eq = eqcond()
            eq.key = "course_id"
            eq.`val` = courseId
            val eq2 = eqcond()
            eq2.key = "goods_id"
            eq2.`val` = goodsId
            requestData.eqcond = Some(List(eq, eq2))
            requestData.res = "bind_course_goods_compet"
            queryMultipleObject[bind_course_goods_compet](requestData).map(x => x.compet_id) :+ goodsId
        }

        answerMap = selectAnswerMap()

        modelMap = {
            implicit val db_report: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]
            val request = new request()
            request.res = "models"
            val model = queryObject[models](request)
            val result = new mutable.HashMap[String, Map[Double, Double]]()
            model.getOrElse(new models()).value.foreach(x => {
                result(x("region_id").toString) = {
                    val re = new mutable.HashMap[Double, Double]()
                    x("curve_data").asInstanceOf[Iterable[Map[String, Double]]].foreach(x => {
                        re(x("x")) = x("y")
                    })
                    re.toMap
                }
            })
            result.toMap
        }

        goodsSalesMap = {
            implicit val db_client: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
            val result = new mutable.HashMap[String, sales]()
            competLst.foreach(x => {
                val requestData = new request()
                requestData.eqcond = Some(List(requestObj.eq2c("goods_id", x), requestObj.eq2c("region_id", "all"), requestObj.eq2c("ym", beforeYm)))
                requestData.res = "bind_course_region_goods_ym_sales"
                val salesId = queryObject[bind_course_region_goods_ym_sales](requestData).getOrElse(new bind_course_region_goods_ym_sales()).sales_id
                requestData.eqcond = Some(List(requestObj.eq2c("_id", salesId)))
                requestData.res = "sales"
                result(x + "*" + beforeYm + "*all") = queryObject[sales](requestData).getOrElse(new sales)
            })
            val requestData = new request()
            requestData.eqcond = Some(List(requestObj.eq2c("goods_id", goodsId)))
            requestData.incond = Some(List(requestObj.in2c("ym", List(beforeYm, ym))))
            //            requestData.gtecond = Some(List(request.gte2c("ym", beforeYm)))
            //            requestData.ltecond = Some(List(request.lte2c("ym", ym)))
            requestData.fmcond = Some(requestObj.fm2c(100))
            requestData.res = "bind_course_region_goods_ym_sales"
            queryMultipleObject[bind_course_region_goods_ym_sales](requestData).foreach(x => {
                requestData.eqcond= Some(List(requestObj.eq2c("_id", x.sales_id)))
                requestData.res = "sales"
                result(goodsId + "*" + x.ym + "*" + x.region_id) = queryObject[sales](requestData).getOrElse(new sales())
            })
            result.toMap
        }
    }

    override def exec: Unit = {

        val goodsReportMap = {  // regionID
            val result = new mutable.HashMap[String, apmreport]()
            paperinputLst.foreach(x => {
                val report = new apmreport()
                val targetSales = goodsSalesMap(goodsId + "*" + ym + "*" + x.region_id)
                report.sales_growth = getPaperScore(x)
                report.sales = countSales(report.sales_growth, x.region_id).toLong
                report.potential = targetSales.potential
                report.sales_growth = targetSales.potential_growth
                report.potential_contri = targetSales.potential_contri
                report.contri_index = report.sales_contri / report.potential_contri
                report.share = report.sales.toDouble / report.potential.toDouble
                report.share_change = report.share - goodsSalesMap(goodsId + "*" + beforeYm + "*" + x.region_id).share
                report.company_target = targetSales.company_target
                report.achieve_rate = report.sales.toDouble / report.company_target.toDouble
                salesSum += report.sales
                result(x.region_id) = report
            })
            result.foreach(x => {
                x._2.sales_contri = x._2.sales / salesSum
            })
            result.toMap
        }

        val productReportMap = {  // goods_id
            val result = new mutable.HashMap[String, apmreport]()
            val goodsSales: Double = goodsSalesMap(goodsId + "*" + beforeYm +"*all").sales
            var sum = 0.0
            competLst.foreach(x => {
                sum += goodsSalesMap(x + "*" + beforeYm +"*all").sales
            })
            competLst.foreach(x => {
                val report = new apmreport()
                val beforeSales = goodsSalesMap(x + "*" + beforeYm +"*all")
                report.sales = ((sum - salesSum) * beforeSales.sales / (sum - goodsSales)).toLong
                report.share = report.sales / sum
                report.potential = beforeSales.potential
                report.potential_growth = 1
                report.potential_contri = beforeSales.potential_contri
                report.share_change = report.share - beforeSales.share
                result(x) = report
            })

            result.toMap
        }

        bind_paper_region_goods_ym_report_lst = {
            var resultLst: List[bind_paper_region_goods_ym_report] = Nil
            goodsReportMap.foreach(x => {
                resultLst = resultLst :+ update(x._2, x._1, goodsId)
            })
            productReportMap.foreach(x => {
                resultLst = resultLst :+ update(x._2, "all", x._1)
            })
            resultLst
        }
    }

    override def goback: model.RootObject = toJsonapi(bind_paper_region_goods_ym_report_lst)

    private def getPaperScore(paperInput: paperinput): Double = {
        var result = 0.0
        val answer = answerMap(paperInput.region_id)
        def meeting(x: Int, y: Int): Double = {
            if (x == 0) 10 else {
                y.toDouble / x.toDouble match {
                    case i if i > 0.95 => 10
                    case i if i > 0.85 => 8
                    case i if i > 0.7 => 6
                    case i if i > 0.55 => 4
                    case _ => 0
                }
            }
        }
        if (paperInput.sorting != ""){
            answer.sorting.toInt - paperInput.sorting.toInt match {
                case 0 => result += (10 * 0.2)
                case 1 => result += (8 * 0.2)
                case 2 => result += (5 * 0.2)
                case _ => result += 0
            }
        }
        math.abs(answer.predicted_target / paperInput.predicted_target) match {
            case i if i <= 0.005 => result += (10 * 0.1)
            case i if i <= 0.02 => result += (8 * 0.1)
            case i if i <= 0.05 => result += (5 * 0.1)
            case _ => result += 0
        }
        answer.action_plans.intersect(paperInput.action_plans).length match {
            case 2 => result += (10 * 0.4)
            case 1 => result += (5 * 0.4)
            case 0 => result += 0
        }
        result += (((meeting(answer.city_meeting, paperInput.city_meeting)
                + meeting(answer.national_meeting, paperInput.national_meeting)
                + meeting(answer.depart_meeting, paperInput.depart_meeting)
                + meeting(answer.field_work_days, paperInput.field_work_days)) / 4) * 0.3)
        val x1 = result.toInt
        val x2 = x1 + 1
        val y1 = modelMap(paperInput.region_id)(x1)
        val y2 = modelMap(paperInput.region_id)(x2)
        y1 + ((y2 - y1) * (result - x1.toDouble) / (x2.toDouble - x1.toDouble))
    }

    private def countSales(growth: Double, regionId: String): Double = {
        goodsSalesMap(goodsId + "*q4*" + regionId).sales * (1 + growth)
    }

    private def update(report: apmreport, regionId: String, goods:String): bind_paper_region_goods_ym_report ={
        implicit val db_report: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]
        val requestData = new request()
        requestData.eqcond = Some(
            List(
                requestObj.eq2c("goods_id",goods),
                requestObj.eq2c("paper_id",paperId),
                requestObj.eq2c("region_id",regionId),
                requestObj.eq2c("ym",ym),
                requestObj.eq2c("course_id",courseId)
            )
        )
        requestData.res = "bind_paper_region_goods_ym_report"
        val bind = queryObject[bind_paper_region_goods_ym_report](requestData).orNull
        if (bind == null) insert(report, regionId, goods) else {
            requestData.eqcond = Some(List(requestObj.eq2c("_id", bind.report_id)))
            requestData.res = "report"
            report.id = bind.report_id
            requestData.eqcond = Some(
                List(
                    requestObj.eq2c("sales",report.sales),
                    requestObj.eq2c("sales_growth",report.sales_growth),
                    requestObj.eq2c("sales_contri",report.sales_contri),
                    requestObj.eq2c("potential",report.potential),    //need add
                    requestObj.eq2c("potential_growth",report.potential_growth),  //need add
                    requestObj.eq2c("contri_index",report.contri_index)    //need add
                )
            )
            updateObject[apmreport](requestData)
            bind.apmreport = Some(report)
            bind
        }
    }

    private def insert(report: apmreport, regionId: String, goods:String): bind_paper_region_goods_ym_report = {
        implicit val db_report: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]
        report.`type` = "report"
        report.id = insertObject[apmreport](report).get("_id").toString
        val bind = new bind_paper_region_goods_ym_report()
        bind.goods_id = goods
        bind.paper_id = paperId
        bind.region_id = regionId
        bind.course_id = courseId
        bind.ym = ym
        bind.report_id = report.id
        bind.`type` = "bind_paper_region_goods_ym_report"
        bind.id = insertObject[bind_paper_region_goods_ym_report](bind).get("_id").toString
        bind.apmreport = Some(report)
        bind
    }

    private def selectAnswerMap(): Map[String, answer] ={
        implicit val db_report: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]
        val request = new request()
        val eq = eqcond()
        eq.key = "course_id"
        eq.`val` = courseId
        request.eqcond = Some(List(eq))
        request.res = "answer"
        val answerLst = queryMultipleObject[answer](request)
        val result = new mutable.HashMap[String, answer]()
        answerLst.foreach(x => {
            result(x.region_id) = x
        })
        result.toMap
    }


}

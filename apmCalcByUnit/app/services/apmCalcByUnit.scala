package services

import com.mongodb.DBObject
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity._
import com.pharbers.models.entity.apmCalc.{answer, models}
import com.pharbers.models.entity.bind_paper_region_goods_time_report
import com.pharbers.models.request._
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

import scala.collection.mutable

case class apmCalcByUnit()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "apm calc by unit"
    var request_data: request = null
    var courseId: String = ""
    var paperId: String = ""

    val gen_time = "18-q1"
    val before_time = "17-q4"
    val allRegion = "all"

    var allReportLst: List[bind_paper_region_goods_time_report] = Nil

    override def prepare: Unit = {
        request_data = formJsonapi[request](rq.body)
        paperId = request_data.eqcond.get.find(x => x.key == "paper_id").map(x => x.`val`.toString).get
    }

    override def exec: Unit = {
        courseId = queryCourseIdByPaper(paperId)
        val paperinputLst: List[paperinput] = queryPaperInputLst(request_data)

        // 目前只支持单一商品
        val currentGoodsId: String = queryCurrentGoods(courseId).head

        val competIdLst: List[String] = queryCompetGoodsLst(courseId, currentGoodsId)

        val answerLst = queryAnswerLst(courseId)

        val modelMap = queryModelMap()

        val goodsUnitInfoLst = queryGoodsUnit(
            courseId = courseId, allRegion = allRegion,
            beforeTime = before_time, genTime = gen_time,
            currentGoodsId = currentGoodsId, competIdLst = competIdLst
        )

        val goodsReportLstTmp = paperinputLst.map { paperinput =>
            generateSingleReport(currentGoodsId, paperinput)(before_time, gen_time)(goodsUnitInfoLst, answerLst, modelMap)
        }

        val unitSum = goodsReportLstTmp.map(_.apmreport.get.unit).sum

        val goodsReportLst = goodsReportLstTmp.map { bind =>
            val report = bind.apmreport.get
            report.contri = report.unit / unitSum
            report.contri_index = report.contri / report.potential_contri
            bind.apmreport = Some(report)
            bind
        }

        val productReportLst = generateProductReportLst(currentGoodsId, competIdLst)(allRegion, before_time, gen_time)(unitSum, goodsUnitInfoLst)

        allReportLst = goodsReportLst ::: productReportLst
        allReportLst.foreach { reportInfo =>
            val report_id = insertApmReport(reportInfo.apmreport.get).get("_id").toString
            reportInfo.report_id = report_id
            val tmp = reportInfo.apmreport.get
            tmp.id = report_id
            reportInfo.apmreport = Some(tmp)
            insertBindReport(reportInfo)
        }
    }

    override def goback: model.RootObject = toJsonapi(allReportLst)

    def queryCourseIdByPaper(paperId: String): String = {
        implicit val db_client: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
        val rq = new request()
        rq.res = "bind_user_course_paper"
        rq.eqcond = Some(eq2c("paper_id", paperId) :: Nil)
        queryObject[bind_user_course_paper](rq) match {
            case Some(one) => one.course_id
            case None => throw new Exception("Could not find specified course")
        }
    }

    def queryPaperInputLst(request_data: request): List[paperinput] = {
        implicit val db_client: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
        queryMultipleObject[paperinput](request_data)
    }

    def queryCurrentGoods(course_id: String): List[String] = {
        implicit val db_client: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
        val rq = new request()
        rq.res = "bind_course_goods"
        rq.eqcond = Some(eq2c("course_id", course_id) :: Nil)
        queryMultipleObject[bind_course_goods](rq) match {
            case Nil => throw new Exception("Could not find specified medicine")
            case lst => lst.map(_.goods_id)
        }
    }

    def queryCompetGoodsLst(course_id: String, goods_id: String): List[String] = {
        implicit val db_client: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
        val rq = new request()
        rq.res = "bind_course_goods_compet"
        rq.eqcond = Some(eq2c("course_id", course_id) :: eq2c("goods_id", goods_id) :: Nil)
        queryMultipleObject[bind_course_goods_compet](rq) match {
            case Nil => throw new Exception("Could not find specified medicine")
            case lst => lst.map(_.compet_id)
        }
    }

    def queryAnswerLst(courseId: String): List[answer] = {
        implicit val db_report: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]
        val request = new request()
        request.res = "answer"
        request.eqcond = Some(eq2c("course_id", courseId) :: Nil)
        queryMultipleObject[answer](request)
    }

    def queryModelMap(): Map[String, Map[Double, Double]] = {
        implicit val db_report: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]
        val request = new request()
        request.res = "models"

        val result = new mutable.HashMap[String, Map[Double, Double]]()
        queryObject[models](request) match {
            case Some(one) =>
                one.value.foreach(x => {
                    result(x("region_id").toString) = {
                        val re = new mutable.HashMap[Double, Double]()
                        x("curve_data").asInstanceOf[Iterable[Map[String, Double]]].foreach(x => {
                            re(x("x")) = x("y")
                        })
                        re.toMap
                    }
                })
            case None => throw new Exception("")
        }
        result.toMap
    }

    def queryGoodsUnit(courseId: String, allRegion: String,
                        beforeTime: String, genTime: String,
                        currentGoodsId: String, competIdLst: List[String]): List[bind_course_region_goods_time_unit] = {

        implicit val db_client: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

        def queryUnit(unit_id: String): unit = {
            val rq = new request()
            rq.res = "unit"
            rq.eqcond = Some(eq2c("_id", unit_id) :: Nil)
            queryObject[unit](rq) match {
                case Some(one) => one
                case None => throw new Exception("Could not find specified unit")
            }
        }

        def queryUnitAndBindInfo(course_id: String, region_id: String, goods_id: String, time: String): bind_course_region_goods_time_unit = {
            val rq = new request()
            rq.res = "bind_course_region_goods_time_unit"
            rq.eqcond = Some(eq2c("course_id", course_id) :: eq2c("region_id", region_id) :: eq2c("goods_id", goods_id) :: eq2c("time", time) :: Nil)
            queryObject[bind_course_region_goods_time_unit](rq) match {
                case Some(one) =>
                    one.unit = Some(queryUnit(one.unit_id))
                    one
                case None => throw new Exception("Could not find specified unit")
            }
        }

        def queryRegionIdLst(course_id: String): List[String] = {
            val rq = new request()
            rq.res = "bind_course_region"
            rq.eqcond = Some(eq2c("course_id", course_id) :: Nil)
            rq.fmcond = Some(fm2c())
            queryMultipleObject[bind_course_region](rq) match {
                case Nil => throw new Exception("Could not find specified region")
                case lst => lst.map(_.region_id)
            }
        }

        val allGoodsLastAllRegionUnit = (currentGoodsId :: competIdLst).map(queryUnitAndBindInfo(courseId, allRegion, _, beforeTime))

        val reginonIdLst = queryRegionIdLst(courseId).filter(_ != allRegion)
        val curGoodsLastSingleRegionUnit = reginonIdLst.map(regionId => queryUnitAndBindInfo(courseId, regionId, currentGoodsId, beforeTime))
        val curGoodsGenSingleRegionUnit = reginonIdLst.map(regionId => queryUnitAndBindInfo(courseId, regionId, currentGoodsId, genTime))

        allGoodsLastAllRegionUnit ::: curGoodsLastSingleRegionUnit ::: curGoodsGenSingleRegionUnit
    }

    def generateSingleReport(curGoodsId: String, input: paperinput)
                            (beforeTime: String, genTime: String)
                            (goodsUnitInfoLst: List[bind_course_region_goods_time_unit],
                             answerLst: List[answer],
                             modelMap: Map[String, Map[Double, Double]]): bind_paper_region_goods_time_report = {

        val beforeTimeUnit = goodsUnitInfoLst.find(x => x.goods_id == curGoodsId && x.time == beforeTime && x.region_id == input.region_id)
                .get.unit.get
        val genTimeUnit = goodsUnitInfoLst.find(x => x.goods_id == curGoodsId && x.time == genTime && x.region_id == input.region_id)
                .get.unit.get

        def genReport: apm_unit_report = {
            val report = new apm_unit_report()
            report.`type` = "report"
            report.growth = getPaperScore(input)(answerLst, modelMap)
            report.unit = (beforeTimeUnit.unit * (1 + report.growth)).toLong
            report.potential = genTimeUnit.potential
            report.potential_contri = genTimeUnit.potential_contri
            report.share = report.unit / report.potential
            report.share_change = report.share - beforeTimeUnit.share
            report.company_target = genTimeUnit.company_target
            report.achieve_rate = report.unit / report.company_target
            report
        }

        val bind_data = new bind_paper_region_goods_time_report
        bind_data.`type` = "bind_paper_region_goods_time_report"
        bind_data.course_id = courseId
        bind_data.paper_id = paperId
        bind_data.region_id = input.region_id
        bind_data.goods_id = curGoodsId
        bind_data.time_type = "season"
        bind_data.time = genTime
        bind_data.apmreport = Some(genReport)
        bind_data
    }

    def getPaperScore(paperInput: paperinput)(answerLst: List[answer], modelMap: Map[String, Map[Double, Double]]): Double = {
        var result = 0.0
        val answer = answerLst.find(x => x.region_id == paperInput.region_id).get

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

        if (paperInput.sorting != "") {
            (answer.sorting.toInt - paperInput.sorting.toInt).abs match {
                case 0 => result += (10 * 0.2)
                case 1 => result += (8 * 0.2)
                case 2 => result += (5 * 0.2)
                case _ => result += 0
            }
        }

        if (paperInput.predicted_target == 0) result += 0 else {
            ((answer.predicted_target.toDouble / paperInput.predicted_target.toDouble) - 1).abs match {
                case i if i <= 0.005 => result += (10 * 0.1)
                case i if i <= 0.02 => result += (8 * 0.1)
                case i if i <= 0.05 => result += (5 * 0.1)
                case _ => result += 0
            }
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

    def generateProductReportLst(currentGoodsId: String, competIdLst: List[String])
                                (allRegion: String, beforeTime: String, genTime: String)
                                (unitSum: Double, goodsUnitInfoLst: List[bind_course_region_goods_time_unit]): List[bind_paper_region_goods_time_report] = {
        val curGoodsLastAllRegionUnitInfo = goodsUnitInfoLst.find(x => x.goods_id == currentGoodsId && x.time == beforeTime && x.region_id == allRegion).get
        val competLastAllRegionUnitInfo = competIdLst.map( competId =>
            goodsUnitInfoLst.find(x => x.goods_id == competId && x.time == beforeTime && x.region_id == allRegion).get
        )
        val allGoodsLastAllRegionUnitInfo = curGoodsLastAllRegionUnitInfo :: competLastAllRegionUnitInfo
        val curUnit: Double = curGoodsLastAllRegionUnitInfo.unit.get.unit
        val lastSumUnit: Double = allGoodsLastAllRegionUnitInfo.map(_.unit.get.unit).sum

        def genReport(lastUnitInfo: bind_course_region_goods_time_unit): apm_unit_report = {
            val lastUnit = lastUnitInfo.unit.get
            val report = new apm_unit_report()
            report.`type` = "report"
            if(lastUnitInfo.goods_id == currentGoodsId)
                report.unit = (unitSum * 0.86).toLong
            else
                report.unit = ((lastSumUnit - unitSum) * lastUnit.unit / (lastSumUnit - curUnit) * 0.86).toLong
            report.share = report.unit / lastSumUnit
            report.potential = lastUnit.potential
            report.potential_contri = lastUnit.potential_contri
            report.share_change = report.share - lastUnit.share

            report
        }

        allGoodsLastAllRegionUnitInfo.map { lastUnitInfo =>
            val bind_data = new bind_paper_region_goods_time_report
            bind_data.`type` = "bind_paper_region_goods_time_report"
            bind_data.course_id = courseId
            bind_data.paper_id = paperId
            bind_data.region_id = allRegion
            bind_data.goods_id = lastUnitInfo.goods_id
            bind_data.time_type = "season"
            bind_data.time = genTime
            bind_data.apmreport = Some(genReport(lastUnitInfo))
            bind_data
        }
    }

    def insertApmReport(data: apm_unit_report): DBObject = {
        implicit val db_report: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]
        insertObject[apm_unit_report](data)
    }

    def insertBindReport(data: bind_paper_region_goods_time_report): DBObject = {
        implicit val db_report: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]
        insertObject[bind_paper_region_goods_time_report](data)
    }
}
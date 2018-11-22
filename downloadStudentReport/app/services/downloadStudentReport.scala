package services

import java.text.SimpleDateFormat
import java.util.Date

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.models.entity._
import com.pharbers.models.entity.auth.user
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.parseToken
import com.pharbers.models.request.{eq2c, in2c, request}
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import com.pharbers.models.entity.apm.teacher.{bind_teacher_student_time_paper, download_layout, download_paper}
import org.bson.types.ObjectId

case class downloadStudentReport()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "download student report"
    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
    var request_data: request = null
    var outputStream: StringBuffer = new StringBuffer
    val SEP: String = ", "

    override def prepare: Unit = {
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        val bind_lst = queryMultipleObject[bind_teacher_student_time_paper](request_data)

        val regionLst = bind_lst.map(x => x.paper_id).distinct.map { paper_id =>
            queryBindUserCoursePaper(paper_id).course_id
        }.distinct.flatMap { course_id =>
            queryBindCourseRegion(course_id).map(_.region_id)
        }.distinct.map { region_id =>
            queryRegion(region_id)
        }

        val result_lst = bind_lst.map { bind =>
            bind.student = queryUser(bind.student_id)
            bind
        }.map { bind =>
            val layout = new download_layout()
            layout.time = bind.time
            layout.student = bind.student
            val layout_paper = new download_paper()
            layout_paper.inputLst = Some(queryMultiPaperInput(bind.paper_id))
            layout_paper.reportLst = Some(queryMultiBindReport(bind.paper_id))
            layout.paper = Some(layout_paper)
            layout
        }

        val string_head = s"提交时间${SEP}账号${SEP}用户名${SEP}地区名称${SEP}区域名称${SEP}总体分析要点${SEP}排序${SEP}" +
                s"本季预测指标(%)${SEP}辅导协防时间(%)${SEP}全国会名额(%)${SEP}城市会名额(%)${SEP}科室会名额(%)${SEP}" +
                s"行动计划${SEP}地区本季份额(%)${SEP}地区本季销量${SEP}区域本季销量${SEP}区域本季贡献率(%)${SEP}区域本季份额(%)\n"
        outputStream.append(string_head)

        result_lst.foreach { layout =>
            val time = tranTimeToString(layout.time)
            val user_email = layout.student.get.email
            val user_name = layout.student.get.user_name
            val area = "大区名称"

            val reportLst = layout.paper.get.reportLst.get
            val goods_id = reportLst.filter(_.region_id != "all").map(_.goods_id).distinct.head
            val all_share = getReport(goods_id, "all")(reportLst).share.toPercent
            val all_unit = getReport(goods_id, "all")(reportLst).unit.toInt

            layout.paper.get.inputLst.get.foreach { paper_region =>
                outputStream.append(time).append(SEP)
                outputStream.append(user_email).append(SEP)
                outputStream.append(user_name).append(SEP)
                outputStream.append(area).append(SEP)
                outputStream.append(getRegionName(paper_region.region_id)(regionLst)).append(SEP)
                outputStream.append(paper_region.hint).append(SEP)
                outputStream.append(paper_region.sorting).append(SEP)
                outputStream.append(paper_region.predicted_target).append(SEP)
                outputStream.append(paper_region.field_work_days).append(SEP)
                outputStream.append(paper_region.national_meeting).append(SEP)
                outputStream.append(paper_region.city_meeting).append(SEP)
                outputStream.append(paper_region.depart_meeting).append(SEP)
                outputStream.append(paper_region.action_plans.mkString(";")).append(SEP)
                outputStream.append(all_share).append(SEP)
                outputStream.append(all_unit).append(SEP)
                outputStream.append(getReport(goods_id, paper_region.region_id)(reportLst).unit.toPercent).append(SEP)
                outputStream.append(getReport(goods_id, paper_region.region_id)(reportLst).contri.toPercent).append(SEP)
                outputStream.append(getReport(goods_id, paper_region.region_id)(reportLst).share.toPercent).append(SEP)
                outputStream.append("\n")
            }
        }
    }

    override def goback: model.RootObject = model.RootObject(Some(
        model.RootObject.ResourceObject(
            `type` = "result",
            attributes = Some(Seq(
                model.Attribute(
                    "outputStream",
                    model.JsonApiObject.StringValue(outputStream.toString)
                )
            ).asInstanceOf[model.Attributes])
        )
    ))

    def queryBindUserCoursePaper(paper_id: String): bind_user_course_paper = {
        val rq = new request()
        rq.res = "bind_user_course_paper"
        rq.eqcond = Some(eq2c("paper_id", paper_id) :: Nil)
        queryObject[bind_user_course_paper](rq).get
    }

    def queryBindCourseRegion(course_id: String): List[bind_course_region] = {
        val rq = new request()
        rq.res = "bind_course_region"
        rq.eqcond = Some(eq2c("course_id", course_id) :: Nil)
        queryMultipleObject[bind_course_region](rq)
    }

    def queryRegion(region_id: String): region = {
        val rq = new request()
        rq.res = "region"
        rq.eqcond = Some(eq2c("id", region_id) :: Nil)
        queryObject[region](rq) match {
            case Some(one) => one
            case None => throw new Exception("Could not find specified region")
        }
    }

    def queryUser(user_id: String): Option[user] = {
        implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("auth").get.asInstanceOf[DBTrait[TraitRequest]]
        val rq = new request()
        rq.res = "user"
        rq.eqcond = Some(eq2c("id", user_id) :: Nil)
        queryObject[user](rq)
    }

    def queryMultiPaperInput(paper_id: String): List[paperinput] = {
        val rq = new request()
        rq.res = "paperinput"
        rq.eqcond = Some(eq2c("paper_id", paper_id) :: Nil)
        queryMultipleObject[paperinput](rq)
    }

    def queryMultiBindReport(paper_id: String): List[bind_paper_region_goods_time_report] = {
        implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]
        val bind_rq = new request()
        bind_rq.res = "bind_paper_region_goods_time_report"
        bind_rq.eqcond = Some(eq2c("paper_id", paper_id) :: Nil)
        val bindLst = queryMultipleObject[bind_paper_region_goods_time_report](bind_rq, sort = "report_id")

        val in_rq = new request()
        in_rq.res = "report"
        in_rq.incond = Some(in2c("id", bindLst.map(_.report_id).map(new ObjectId(_))) :: Nil)
        val reportLst = queryMultipleObject[apm_unit_report](in_rq, sort = "_id")

        val these = bindLst.iterator
        val those = reportLst.iterator
        while (these.hasNext && those.hasNext){
            these.next().apmreport = Some(those.next())
        }
        bindLst
    }

    def tranTimeToString(time: Long): String = {
        new SimpleDateFormat("yyyy/MM/dd").format(new Date(time))
    }

    def getRegionName(region_id: String)(implicit regionLst: List[region]): String = {
        regionLst.find(_.id == region_id).get.name
    }

    def getReport(goods_id: String, region_id: String)(implicit reportLst: List[bind_paper_region_goods_time_report]): apm_unit_report = {
        reportLst.find(x => x.goods_id == goods_id && x.region_id == region_id).get.apmreport.get
    }

    implicit class double2percent(double: Double) {
        def toPercent: Int = (double * 100).toInt
    }
}

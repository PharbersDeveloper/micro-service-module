package services

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.models.service.auth
import com.pharbers.models.entity.auth.user
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.parseToken
import com.pharbers.models.request.{eq2c, request}
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.models.entity.{bind_paper_region_goods_time_report, bind_user_course_paper, course, paperinput}
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import com.pharbers.models.entity.apm.teacher.{bind_teacher_student_time_paper, download_layout, download_paper}

case class downloadStudentReport()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "download student report"
    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
    var request_data: request = null
    var bind_lst: List[bind_teacher_student_time_paper] = Nil
    var result_lst: List[download_layout] = Nil

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        bind_lst = queryMultipleObject[bind_teacher_student_time_paper](request_data)
        bind_lst = bind_lst.map { x =>
            x.student = queryUser(x.student_id)
            x.course = queryCourse(queryBindUserCoursePaper(x.paper_id).course_id)
            x
        }

        val tmp = bind_lst.head

        val ddd = new download_layout()
        ddd.time = tmp.time
        ddd.student = tmp.student
        val ppp = new download_paper()
        ppp.inputLst = Some(queryMultiPaperInput(tmp.paper_id))
        ppp.reportLst = Some(queryMultiBindReport(tmp.paper_id))
        ddd.paper = Some(ppp)
        result_lst = ddd :: Nil
    }

    override def goback: model.RootObject = toJsonapi(result_lst)

    def queryUser(user_id: String): Option[user] = {
        implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("auth").get.asInstanceOf[DBTrait[TraitRequest]]
        val rq = new request()
        rq.res = "user"
        rq.eqcond = Some(eq2c("id", user_id) :: Nil)
        queryObject[user](rq)
    }

    def queryBindUserCoursePaper(paper_id: String): bind_user_course_paper = {
        val rq = new request()
        rq.res = "bind_user_course_paper"
        rq.eqcond = Some(eq2c("paper_id", paper_id) :: Nil)
        queryObject[bind_user_course_paper](rq).get
    }

    def queryCourse(course_id: String): Option[course] = {
        val rq = new request()
        rq.res = "course"
        rq.eqcond = Some(eq2c("id", course_id) :: Nil)
        queryObject[course](rq)
    }

    def queryMultiPaperInput(paper_id: String): List[paperinput] = {
        val rq = new request()
        rq.res = "paperinput"
        rq.eqcond = Some(eq2c("paper_id", paper_id) :: Nil)
        queryMultipleObject[paperinput](rq)
    }

    def queryMultiBindReport(paper_id: String): List[bind_paper_region_goods_time_report] = {
        val rq = new request()
        rq.res = "bind_paper_region_goods_time_report"
        rq.eqcond = Some(eq2c("paper_id", paper_id) :: Nil)
        queryMultipleObject[bind_paper_region_goods_time_report](rq)
    }

}

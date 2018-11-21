package services

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame.Brick
import com.pharbers.models.service.auth
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.parseToken
import com.pharbers.models.request.{eq2c, request}
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import com.pharbers.models.entity.{bind_user_course_paper, course}
import com.pharbers.models.entity.apm.teacher.bind_teacher_student_time_paper
import com.pharbers.models.entity.auth.user

case class findBindTeacherStudentTimePaper()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find BindTeacherStudentTimePaper"
    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
    var auth: auth = null
    var request_data: request = null
    var bind_lst: List[bind_teacher_student_time_paper] = Nil

    override def prepare: Unit = {
        auth = parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        bind_lst = queryMultipleObject[bind_teacher_student_time_paper](request_data)
        bind_lst = bind_lst.map { x =>
            x.student = queryUser(x.student_id)
            x.course = queryCourse(queryBindUserCoursePaper(x.paper_id).course_id)
            x
        }
    }

    override def goback: model.RootObject = toJsonapi(bind_lst)

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
}
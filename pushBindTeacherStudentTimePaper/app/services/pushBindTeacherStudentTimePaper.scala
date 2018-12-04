package services

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame.Brick
import com.pharbers.models.entity.paper
import com.pharbers.models.service.auth
import com.pharbers.pattern.common.PhToken
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.models.request.{eq2c, request}
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import com.pharbers.models.entity.apm.teacher.bind_teacher_student_time_paper

case class pushBindTeacherStudentTimePaper()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "push BindTeacherStudentTimePaper"
    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
    var auth: auth = null
    var insert_data: bind_teacher_student_time_paper = null

    override def prepare: Unit = {
        auth = parseToken(rq)
        insert_data = formJsonapi[bind_teacher_student_time_paper](rq.body)
    }

    override def exec: Unit = {
        val req = new request
        req.res = "bind_teacher_student_time_paper"
        req.eqcond = Some(
            eq2c("teacher_id", insert_data.teacher_id) :: eq2c("paper_id", insert_data.paper_id) :: Nil
        )
        queryObject[bind_teacher_student_time_paper](req) match {
            case Some(one) => insert_data = one
            case None =>
                insert_data.`type` = "bind_teacher_student_time_paper"
                insert_data.student_id = auth.user.get.id
                insert_data.time = queryPaper(insert_data.paper_id).end_time

                insert_data.id = insertObject(insert_data).get("_id").toString
        }
    }

    override def goback: model.RootObject = toJsonapi(insert_data)

    def queryPaper(paper_id: String): paper = {
        val rq = new request()
        rq.res = "paper"
        rq.eqcond = Some(eq2c("id", paper_id) :: Nil)
        queryObject[paper](rq) match {
            case Some(one) => one
            case None => throw new Exception("Could not find specified paper")
        }
    }
}

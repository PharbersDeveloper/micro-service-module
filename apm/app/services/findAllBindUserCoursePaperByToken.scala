package services

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.models.request._
import com.pharbers.models.service.auth
import com.pharbers.pattern.common.PhToken
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import com.pharbers.models.entity.{bind_user_course_paper, course, paper}

case class findAllBindUserCoursePaperByToken()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find all paper by token"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var auth_data: auth = null
    var paperLst: List[paper] = Nil

    override def prepare: Unit = auth_data = parseToken(rq)

    override def exec: Unit = {
        val findBindLst_rq = new request()
        findBindLst_rq.res = "bind_user_course_paper"
        findBindLst_rq.fmcond = Some(fm2c(0, 1000))
        findBindLst_rq.eqcond = Some(eq2c("user_id", auth_data.user.get.id) :: Nil)
        val bindIdLst = queryMultipleObject[bind_user_course_paper](findBindLst_rq, "_id")

        val findCourseLst_rq = new request()
        findCourseLst_rq.res = "course"
        findCourseLst_rq.fmcond = Some(fm2c(0, 1000))
        findCourseLst_rq.incond = Some(in2c("_id", bindIdLst.map(x => x.course_id).distinct) :: Nil)
        val courseLst = queryMultipleObject[course](findCourseLst_rq).map{course =>
            course.describe = ""
            course.prompt = ""
            course
        }

        val findPaperLst_rq = new request()
        findPaperLst_rq.res = "paper"
        findPaperLst_rq.fmcond = Some(fm2c(0, 1000))
        findPaperLst_rq.incond = Some(in2c("_id", bindIdLst.map(x => x.paper_id).distinct) :: Nil)
        paperLst = queryMultipleObject[paper](findPaperLst_rq).map{ paper =>
            paper.course = findCourseByBindPaper(paper.id)(bindIdLst, courseLst)
            paper
        }.reverse
    }

    def findCourseByBindPaper(paper_id: String)(bindIdLst:List[bind_user_course_paper], courseLst: List[course]): Option[course] = {
        bindIdLst.find(x => x.paper_id == paper_id).map{bind =>
            courseLst.find(x => x.id == bind.course_id).get
        }
    }

    override def goback: model.RootObject = {
        if(paperLst.isEmpty) model.RootObject(Some(
            model.RootObject.ResourceObjects(Nil)
        ))
        else toJsonapi(paperLst)
    }
}
package services

import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_user_course_paper, course, paper}
import com.pharbers.models.request.{eqcond, fmcond, incond, request}
import com.pharbers.models.service.auth
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import org.bson.types.ObjectId
import play.api.mvc.Request

import scala.collection.mutable

case class findAllPaperById()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find many paper by id"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var paper_dataLst: List[paper] = null
    var auth_data: auth = null

    override def prepare: Unit = {
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        request_data.incond = request_data.incond.map{ x =>
            x.map{ incond =>
                incond.`val` = incond.`val`.asInstanceOf[List[String]].map{ id => new ObjectId(id) }
                incond
            }
        }

        paper_dataLst = queryMultipleObject[paper](request_data)

        val courseIdLst = {
            val request = new request()
            request.res = "bind_user_course_paper"

            var valList: List[String] = Nil
            paper_dataLst.foreach(x => valList = valList :+ x.id)
            val fm = fmcond()
            fm.take = 1000
            request.fmcond = Some(fm)
            val in = incond()
            in.key = "paper_id"
            in.`val` = valList.toSet
            request.incond = Some(List(in))
            queryMultipleObject[bind_user_course_paper](request, "paper_id")
        }

        val courseLst = {
            val request = new request()
            request.res = "course"
            var valList: List[ObjectId] = Nil
            courseIdLst.foreach(x => valList = valList :+ new ObjectId(x.course_id))
            val fm = fmcond()
            fm.take = 1000
            request.fmcond = Some(fm)
            val in = incond()
            in.key = "_id"
            in.`val` = valList.toSet
            request.incond = Some(List(in))
            val map = new mutable.HashMap[String, course]()
            queryMultipleObject[course](request, "_id").foreach(x => map(x.id) = x)
            map
        }

        val these = paper_dataLst.iterator
        val those = courseIdLst.iterator
        while (these.hasNext && those.hasNext){
            these.next().course = Some(courseLst(those.next().course_id))
        }
    }

    override def goback: model.RootObject = toJsonapi(paper_dataLst)
}
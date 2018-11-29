package services

import java.util.Date

import com.pharbers.pattern.common.PhToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_region_rep, representative}
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindCourseRegionRep()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind course region representative list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var repIdLst: List[bind_course_region_rep] = Nil
    var repLst: List[representative] = Nil

    override def prepare: Unit = {
//        val start1 = new Date().getTime
        parseToken(rq)
//        val end1 = new Date().getTime
//        println("parseToken" + (end1 - start1))
//        val start2 = new Date().getTime
        request_data = formJsonapi[request](rq.body)
//        val end2 = new Date().getTime
//        println("formJsonapi" + (end2 - start2))
    }

    override def exec: Unit = {
//        val start = new Date().getTime
        repIdLst = queryMultipleObject[bind_course_region_rep](request_data)
//        val end = new Date().getTime
//        println("queryMultipleObject" + (end - start))
    }

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "representative"

//        val start = new Date().getTime
        repLst = repIdLst.map { x =>
            request.eqcond = None
            val ec = eqcond()
            ec.key = "id"
            ec.`val` = x.rep_id
            request.eqcond = Some(List(ec))
//            val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            val str = forward("123.56.179.133", "18007")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            formJsonapi[representative](decodeJson[model.RootObject](parseJson(str)))
        }
//        val end = new Date().getTime
//        println("queryObject" + (end - start))
    }

    override def goback: model.RootObject = {
//        val start = new Date().getTime
        val a = toJsonapi(repLst)
//        val end = new Date().getTime
//        println("toJsonapi" + (end - start))
        a
    }
}
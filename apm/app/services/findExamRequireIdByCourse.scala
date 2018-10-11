package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_exam_require, examrequire}
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.parseToken
import com.pharbers.pattern.frame.{Brick, forward}
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import com.pharbers.pattern.frame._
import play.api.mvc.Request

case class findExamRequireIdByCourse()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {
    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find exam_require id by course id in bind_course_exam_require"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var bind_course_exam_require_datas : List[bind_course_exam_require] = Nil
    var request_data : request = null
    var exam_requires : List[examrequire] = Nil

    override def prepare: Unit = {
//        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        bind_course_exam_require_datas = queryMultipleObject[bind_course_exam_require](request_data)
    }

    override def forwardTo(next_brick: String): Unit = {
        exam_requires = bind_course_exam_require_datas.map(x =>{
            val request = new request
            request.res = "examrequire"
            val cond = eqcond()
            cond.key = "_id"
            cond.`val` = x.exam_require_id
            request.eqcond = Some(List(cond))
            val res = forward("127.0.0.1", "18014")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            formJsonapi[examrequire](decodeJson[model.RootObject](parseJson(res)))
        })
    }

    override def goback: model.RootObject = toJsonapi(exam_requires)
}

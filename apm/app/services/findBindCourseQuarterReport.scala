package services

import com.pharbers.pattern.common.PhToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_course_goods_quarter_report}
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindCourseQuarterReport()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind course quarter_report list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var qrIdLst: List[bind_course_goods_quarter_report] = Nil
    var apm_quarter_report_str: String = null

    override def prepare: Unit = {
        existToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = qrIdLst = queryMultipleObject[bind_course_goods_quarter_report](request_data)

    override def forwardTo(next_brick: String): Unit = {
        qrIdLst match {
            case one :: Nil =>
                val request = new request()
                request.res = "apm_quarter_report"
                val ec = eqcond()
                ec.key = "id"
                ec.`val` = one.quarter_report_id
                request.eqcond = Some(List(ec))
//                apm_quarter_report = forward("123.56.179.133", "18012")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
                apm_quarter_report_str = forward("apm_findquarterreportbyid", "9000")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            case _ => throw new Exception("find more quarter_report")

        }
    }

    override def goback: model.RootObject = decodeJson[model.RootObject](parseJson(apm_quarter_report_str))
}
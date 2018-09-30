package services

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.models.request.{eqcond, request}
import com.pharbers.models.entity.{bind_course_goods, medicine}
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}

case class findBindCourseGoods()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind course med list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var medIdLst: List[bind_course_goods] = Nil
    var medLst: List[medicine] = Nil

    override def prepare: Unit = request_data = {
        parseToken(rq)
        formJsonapi[request](rq.body)
    }

    override def exec: Unit = medIdLst = queryMultipleObject[bind_course_goods](request_data)

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "goods"

        medLst = medIdLst.map{ x =>
            request.eqcond = None
            val ec = eqcond()
            ec.key = "id"
            ec.`val` = x.goods_id
            request.eqcond = Some(List(ec))
            val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
            formJsonapi[medicine](decodeJson[model.RootObject](parseJson(str)))
        }
    }

    override def goback: model.RootObject = toJsonapi(medLst)
}
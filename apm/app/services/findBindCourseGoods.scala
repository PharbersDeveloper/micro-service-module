package services

import com.pharbers.pattern.common.PhToken
import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.models.request.{eqcond, fm2c, in2c, request}
import com.pharbers.models.entity.{bind_course_goods, medicine}
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}

case class findBindCourseGoods()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind course med list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var medIdLst: List[bind_course_goods] = Nil
    var medLstStr: String = ""

    override def prepare: Unit = request_data = {
        existToken(rq)
        formJsonapi[request](rq.body)
    }

    override def exec: Unit = medIdLst = queryMultipleObject[bind_course_goods](request_data)

    override def forwardTo(next_brick: String): Unit = {
        val request = new request()
        request.res = "goods"
        request.fmcond = Some(fm2c(0, 1000))
        request.incond = Some(in2c("_id", medIdLst.map(x => x.goods_id)) :: Nil)
//        medLstStr = forward("123.56.179.133", "19007")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
        medLstStr = forward("findmed", "9000")(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces).check()
    }

    override def goback: model.RootObject = decodeJson[model.RootObject](parseJson(medLstStr))
}
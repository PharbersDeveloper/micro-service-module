package services

import java.util.Date

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.models.request._
import com.pharbers.pattern.common.PhToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}

case class findAllBindCourseRegionGoodsTimeUnit()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import io.circe.syntax._

    var request_data: request = null
    var result: String = ""

    override val brick_name: String = "find all bind course region med time unit list"

    override def prepare: Unit = {}

    override def exec: Unit = {
        result = forward("findallbindcourseregiongoodstimeunit", "9000")(api + (cur_step + 1)).post(rq.body.asJson.noSpaces).check()
//        result = forward("123.56.179.133", "19005")(api + (cur_step + 1)).post(rq.body.asJson.noSpaces).check()
    }

    override def goback: model.RootObject = {
//        val start = new Date().getTime
        val a = decodeJson[model.RootObject](parseJson(result))
//        val end = new Date().getTime
//        println("goback" + (end - start))
        a
    }
}

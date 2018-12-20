package services

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.models.request._
import com.pharbers.pattern.common.PhToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.models.service.auth
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}

case class findAllBindCourseRegionGoodsTimeUnit()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import io.circe.syntax._

    var request_data: request = null
    var result: String = ""

    override val brick_name: String = "find all bind course region med time unit list"

    override def prepare: Unit = {}

    override def exec: Unit = {
        val key = rq.body.asJson.noSpaces
        result = if (rd.exsits(key)) rd.getString(key) else {
//            val tmp = forward("123.56.179.133", "19005")(api + (cur_step + 1)).post(key).check()
            val tmp = forward("findallbindcourseregiongoodstimeunit", "9000")(api + (cur_step + 1)).post(key).check()
            rd.addString(key, tmp)
            rd.expire(key, new auth().token_expire)
            tmp
        }
    }

    override def goback: model.RootObject = decodeJson[model.RootObject](parseJson(result))
}

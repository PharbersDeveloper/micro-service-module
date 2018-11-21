package services

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.models.request._
import com.pharbers.pattern.frame._
import com.pharbers.pattern.common.parseToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}

case class findAllBindCourseRegionGoodsTimeSales()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import io.circe.syntax._

    var request_data: request = null
    var result: String = ""

    override val brick_name: String = "find all bind course region med time sales list"

    override def prepare: Unit = {}

    override def exec: Unit = {
        result = forward("123.56.179.133", "19004")(api + (cur_step + 1)).post(rq.body.asJson.noSpaces).check()
    }

    override def goback: model.RootObject = decodeJson[model.RootObject](parseJson(result))
}

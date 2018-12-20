package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.models.request._
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class downloadStudentReport()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import io.circe.syntax._

    override val brick_name: String = "push BindTeacherStudentTimePaper"
    var result: String = ""

    override def prepare: Unit = {}

    override def exec: Unit = {
//        result = forward("123.56.179.133", "18027")(api + (cur_step + 1)).post(rq.body.asJson.noSpaces).check()
        result = forward("apm_downloadstudentreport", "9000")(api + (cur_step + 1)).post(rq.body.asJson.noSpaces).check()
    }

    override def goback: model.RootObject = decodeJson[model.RootObject](parseJson(result))
}

object RootObject2DownloadStream {
    implicit class impl(obj: model.RootObject) {
        def toDownloadStream: Array[Byte] =
            obj.data.get.asInstanceOf[model.RootObject.ResourceObject]
                    .attributes.get.head.value
                    .asInstanceOf[model.JsonApiObject.StringValue]
                    .value.getBytes("GB2312")
    }
}
package services

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.models.request._
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.PhToken
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}

case class findBindTeacherStudentTimePaperCount()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken{

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    override val brick_name: String = "find BindTeacherStudentTimePaper count"
    var request_data: request = null
    var result: Long = 0L

    override def prepare: Unit = {
        existToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        result = queryCount(request_data)
    }

    override def goback: model.RootObject = model.RootObject(Some(
        model.RootObject.ResourceObject(
            id = Some("countresult"),
            `type` = "countresult",
            attributes = Some(Seq(
                model.Attribute(
                    "count",
                    model.JsonApiObject.StringValue(result.toString)
                )
            ).asInstanceOf[model.Attributes])
        )
    ))
}
package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.auth.user
import com.pharbers.models.request.request
import com.pharbers.models.service.call_result
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame.Brick
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class emailVerify()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("auth").get.asInstanceOf[DBTrait[TraitRequest]]

    override val brick_name: String = "email verify"
    var request_data: request = null

    override def prepare: Unit = request_data = formJsonapi[request](rq.body)

    override def exec: Unit = queryObject[user](request_data) match {
        case Some(_) => throw new Exception("user email has been use")
        case None => Unit
    }

    override def goback: model.RootObject = {
        val tmp = call_result()
        tmp.state = true
        tmp.des = "email verify success"

        toJsonapi[call_result](tmp)
    }
}
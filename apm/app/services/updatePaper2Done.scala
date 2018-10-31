package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.jsonapi.model.JsonApiObject.StringValue
import com.pharbers.jsonapi.model.{Attribute, Attributes}
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.paper
import com.pharbers.models.request._
import com.pharbers.models.request.request
import com.pharbers.models.service.{call_result, callapmr}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.parseToken
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class updatePaper2Done()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "update paper to done"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: callapmr = null

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[callapmr](rq.body)
    }

    override def exec: Unit = {
        val req = new request
        req.res = "paper"
        req.eqcond = Some(eq2c("_id", request_data.paper_id) :: Nil)
        req.upcond = Some(up2c("state", false) :: up2c("end_time", System.currentTimeMillis()) :: Nil)

        updateObject[paper](req) match {
            case 1 => Unit
            case _ => throw new Exception("update failed for paper")
        }
    }

    override def forwardTo(next_brick: String): Unit = {
        forward("123.56.179.133", "18020")(api + (cur_step + 1)).post(rq.body.asJson.noSpaces).check()
    }

    override def goback: model.RootObject = {
        val tmp = call_result()
        tmp.state = true
        tmp.des = "call R success"

        toJsonapi[call_result](tmp)
    }
}
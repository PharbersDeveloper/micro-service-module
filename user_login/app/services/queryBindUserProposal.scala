package services

import io.circe.syntax._
import play.api.mvc.Request
import com.pharbers.models.{auth, proposal, user}
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.pattern.BrickRegistry
import com.pharbers.pattern.request.request
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.macros._
import com.pharbers.pattern.mongo.client_db_inst._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

case class queryBindUserProposal()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {
    override val brick_name: String = "get proposal list"

    var auth_data = auth()

    var request_data: request = new request
    var user_data = user()

    override def prepare: Unit = auth_data = parseToken(rq)

    override def exec: Unit = {
        println(auth_data.token)
        println(auth_data.user.get)
        val a = new proposal()
        queryObject[user](request_data) match {
            case Some(data) => user_data = data
            case None => throw new Exception("Email is not registered or the password is not correct")
        }
    }

    override def done: Option[String] = {
        val bricks = BrickRegistry().registryRoute(api)
        if (bricks.size - 1 <= cur_step)
            None
        else
            Some(bricks(cur_step + 1))
    }

    override def forwardTo(next_brick: String): Unit = {
        forward(next_brick)(api + (cur_step + 1)).post(rq.body.asJson.noSpaces)
    }

    override def goback: model.RootObject = {
        val out_data = toJsonapi(user_data)
//        phLog(s"out_data = $out_data")
        out_data
    }

}
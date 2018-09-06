package services

import io.circe.syntax._
import play.api.mvc.Request
import com.pharbers.models.user
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.pattern.BrickRegistry
import com.pharbers.pattern.request.request
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport._

import com.pharbers.macros._
import com.pharbers.pattern.mongo.client_db_inst._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

case class encryptToken()(implicit val request: Request[model.RootObject]) extends Brick {
    override val brick_name: String = "encrypt token"

    var request_data: request = null
    var user_data: user = null
    var token: String = ""

    override def prepare: Unit = request_data = formJsonapi[request](request.body)

    override def exec: Unit = queryObject[user](request_data) match {
        case Some(data) => user_data = data
        case None => throw new Exception("Email is not registered or the password is not correct")
    }

    override def done: Option[String] = {
        val bricks = BrickRegistry().registryRoute(api)
        if (bricks.size - 1 <= cur_step)
            None
        else
            Some(bricks(cur_step + 1))
    }

    override def forwardTo(next_brick: String): Unit = {
        forward(next_brick)(api + (cur_step + 1)).post(request.body.asJson.noSpaces)
    }

    override def goback: model.RootObject = {
        val out_data = toJsonapi(user_data)
//        phLog(s"out_data = $out_data")
        out_data
    }

}
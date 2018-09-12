package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.entity.proposal
import com.pharbers.pattern.frame._
import com.pharbers.pattern.mongo.client_db_inst._
import com.pharbers.models.request.request
import play.api.mvc.Request

case class findProposal()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {
    override val brick_name: String = "find proposal list"

    var request_data: request = new request
    var proposal_data = new proposal()

    override def prepare: Unit = request_data = formJsonapi[request](rq.body)

    override def exec: Unit = queryObject[proposal](request_data) match {
        case Some(data) => proposal_data = data
        case None => throw new Exception("Could not find specified proposal")
    }

    override def forwardTo(next_brick: String): Unit = {}

    override def goback: model.RootObject = toJsonapi(proposal_data)
}
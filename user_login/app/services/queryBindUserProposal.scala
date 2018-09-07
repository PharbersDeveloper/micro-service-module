package services

import io.circe.syntax._
import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame._
import com.pharbers.pattern.request.{eqcond, fmcond, request}
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.macros._
import com.pharbers.pattern.mongo.client_db_inst._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.entity.{bind_user_proposal, proposal}
import com.pharbers.models.service.auth

case class queryBindUserProposal()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {
    override val brick_name: String = "get proposal list"

    var auth_data = new auth()
    var bind_proposal: List[bind_user_proposal] = Nil
    var proposalLst: List[proposal] = Nil

    override def prepare: Unit = auth_data = parseToken(rq)

    override def exec: Unit = {
        val request = new request()
        val ec = eqcond()
        val fm = fmcond()
        ec.key = "user_id"
        ec.`val` = auth_data.user.get.id
        request.res = "bind_user_proposal"
        request.eqcond = Some(List(ec))
        request.fmcond = Some(fm)

        bind_proposal = queryMultipleObject[bind_user_proposal](request)
    }

    override def forwardTo(next_brick: String): Unit = {
        bind_proposal.foreach { bind_pps =>
            val request = new request()
            val eq1 = eqcond()
            eq1.key = "id"
            eq1.`val` = bind_pps.proposal_id
            request.res = "proposal"
            request.eqcond = Some(List(eq1))
            val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces)
            val result = decodeJson[model.RootObject](parseJson(str))
            val proposal = formJsonapi[proposal](result)
            proposal.default_phases = Nil
            proposalLst = proposalLst :+ proposal
        }
    }

    override def goback: model.RootObject = {
        toJsonapi(proposalLst)
    }

}
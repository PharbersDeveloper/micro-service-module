package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_paper_region_goods_time_report, paper}
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

    var request_data: request = null

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        val paper_id = request_data.eqcond.get.find(x => x.key == "paper_id").get.`val`.toString
        val req = new request
        req.res = "paper"
        req.eqcond = Some(eq2c("_id", paper_id) :: Nil)

        queryObject[paper](req) match {
            case Some(one) =>
                if (one.state) updatePaper(paper_id)
                else deleteLastReport(paper_id)
            case None => throw new Exception("update failed for paper")
        }
    }

    override def forwardTo(next_brick: String): Unit = {
        forward("127.0.0.1", "18020")(api + (cur_step + 1)).post(rq.body.asJson.noSpaces).check()
    }

    override def goback: model.RootObject = {
        val tmp = call_result()
        tmp.state = true
        tmp.des = "apm calc success"

        toJsonapi[call_result](tmp)
    }

    def updatePaper(paper_id: String): Unit = {
        val req = new request
        req.res = "paper"
        req.eqcond = Some(eq2c("_id", paper_id) :: Nil)
        req.upcond = Some(up2c("state", false) :: up2c("end_time", System.currentTimeMillis()) :: Nil)
        updateObject[paper](req) match {
            case 1 => Unit
            case _ => throw new Exception("update failed for paper")
        }
    }

    def deleteLastReport(paper_id: String): Unit = {
        implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("apm_report").get.asInstanceOf[DBTrait[TraitRequest]]
        val bind_req = new request
        bind_req.res = "bind_paper_region_goods_time_report"
        bind_req.eqcond = Some(eq2c("paper_id", paper_id) :: Nil)
        val bind_lst = queryMultipleObject[bind_paper_region_goods_time_report](bind_req)
        deleteObject(bind_req)

        val report_req = new request
        report_req.res = "report"
        report_req.incond = Some(in2c("_id", bind_lst.map(_.report_id)) :: Nil)
        deleteObject(report_req)
    }
}
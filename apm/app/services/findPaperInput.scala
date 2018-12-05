package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.paperinput
import com.pharbers.models.request.request
import com.pharbers.models.service.paperinputstep
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.PhToken
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findPaperInput()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with PhToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
    import io.circe.syntax._

    override val brick_name: String = "find paper_input list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var paperInputLst: List[paperinput] = Nil

    override def prepare: Unit = {
        existToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        paperInputLst = queryMultipleObject[paperinput](request_data)

        val step = calcStep(paperInputLst)

        paperInputLst = paperInputLst.map { x =>
            x.paperinputstep = step
            x
        }
    }

    def calcStep(pis: List[paperinput]): Option[paperinputstep] = {
        pis match {
            case one :: _ =>
                val tmp = new paperinputstep
                tmp.id = "paperinputstep"

                if(one.hint == "") tmp.step = 0
                else if(one.sorting == "") tmp.step = 1
                else if(one.predicted_target == -1) tmp.step = 2
                else if(one.field_work_days == -1 ||
                        one.national_meeting == -1 ||
                        one.city_meeting == -1 ||
                        one.depart_meeting == -1) tmp.step = 3
                else if(one.action_plans == Nil) tmp.step = 4
                else tmp.step = 0

                Some(tmp)
            case Nil => None
        }
    }

    override def goback: model.RootObject = toJsonapi(paperInputLst)
}
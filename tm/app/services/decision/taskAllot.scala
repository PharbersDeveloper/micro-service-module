package services.decision

import io.circe.syntax._
import com.pharbers.macros._
import play.api.mvc.Request
import services.parseToken
import com.pharbers.jsonapi.model
import com.pharbers.models.entity.scenario
import com.pharbers.models.service.allot_result
import com.pharbers.pattern.frame.{Brick, forward}
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.models.request.{eqcond, request, upcond}

case class taskAllot()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {
    override val brick_name: String = "task allot"

    var ar = new allot_result()
    var report_result: model.RootObject = model.RootObject()

    override def prepare: Unit = {
        parseToken(rq)
        ar = formJsonapi[allot_result](rq.body)
    }

    override def exec: Unit = {
        val scenario = queryScenario(ar.uuid).getOrElse(throw new Exception("Could not find specified scenario"))
        var current = scenario.current

        val connect_reso = {
            val connect_reso = scenario.current("connect_reso").asInstanceOf[List[Map[String, Any]]]
            val manager = connect_reso.filter(_ ("form") == "manager")
            val mii = ar.managerinputinfo.getOrElse(throw new Exception("allot_result not find managerinputinfo"))
            val relp = Map(
                "sales_train" -> mii.sales_train,
                "admin_work" -> mii.admin_work,
                "team_meet" -> mii.team_meet,
                "field_work" -> mii.field_work,
                "kpi_analysis" -> mii.kpi_analysis
            )
            (manager.head ++ Map("relationship" -> relp)) :: (connect_reso diff manager)
        }

        val hospitalbaseinfo = {
            ar.hospitalbaseinfo.getOrElse(throw new Exception("allot_result not find hospitalbaseinfo"))
                    .filter(_.representative.isDefined)
        }

        val reso_rep = {
            val manager_reso_id = connect_reso.find(_ ("form") == "manager").get("id").asInstanceOf[String]
            val repinputinfo = ar.repinputinfo.getOrElse(throw new Exception("allot_result not find repinputinfo"))

            repinputinfo.map { input =>
                Map(
                    "reso_id" -> manager_reso_id,
                    "rep_id" -> input.repInfo.get.id,
                    "relationship" -> Map(
                        "team_meet" -> input.team_meet,
                        "product_train" -> input.product_train,
                        "sales_train" -> input.sales_train,
                        "field_work" -> hospitalbaseinfo.filter(x => x.representative.get.id == input.repInfo.get.id)
                                .map(x => x.managerwith).sum
                    )
                )
            }
        }

        val dest_goods_rep = {
            hospitalbaseinfo.map { input =>
                Map(
                    "dest_id" -> input.hospital.get.id,
                    "goods_id" -> input.hospmedicinfos.get.head.id,
                    "rep_id" -> input.representative.get.id,
                    "relationship" -> Map(
                        "user_input_day" -> input.asignday,
                        "budget_proportion" -> 0,
                        "user_input_target" -> input.target,
                        "target_growth" -> 0,
                        "achieve_rate" -> 0,
                        "user_input_money" -> input.budget
                    )
                )
            }
        }


        current = current ++ Map(
            "report_id" -> "",
            "reso_rep" -> reso_rep,
            "dest_goods_rep" -> dest_goods_rep,
            "connect_reso" -> connect_reso
        )

        val up_current = upcond()
        up_current.key = "current"
        up_current.`val` = current

        val up_assess_report = upcond()
        up_assess_report.key = "assess_report"
        up_assess_report.`val` = ""

        updateScenario(ar.uuid)(List(up_current, up_assess_report))
    }

    override def forwardTo(next_brick: String): Unit = {
        val str = forward(next_brick)(api + (cur_step + 1)).post(rq.body.asJson.noSpaces)
        report_result = decodeJson[model.RootObject](parseJson(str))
    }

    override def goback: model.RootObject = report_result

    def queryScenario(uuid: String): Option[scenario] = {
        val request = new request()
        request.res = "scenario"
        val ec = eqcond()
        ec.key = "uuid"
        ec.`val` = uuid
        request.eqcond = Some(List(ec))
        import com.pharbers.pattern.mongo.client_db_inst.db_inst
        queryObject[scenario](request)
    }

    def updateScenario(uuid: String)(ucs: List[upcond]): Int = {
        val request = new request()
        request.res = "scenario"

        val ec = eqcond()
        ec.key = "uuid"
        ec.`val` = uuid
        request.eqcond = Some(List(ec))

        request.upcond = Some(ucs)
        import com.pharbers.pattern.mongo.client_db_inst.db_inst
        updateObject[scenario](request)
    }
}

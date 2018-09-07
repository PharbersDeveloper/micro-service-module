package services

import io.circe.syntax._
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.entity.{medicine, notice, scenario}
import com.pharbers.models.service.medicsnotice
import com.pharbers.pattern.frame._
import com.pharbers.pattern.mongo.client_db_inst._
import com.pharbers.pattern.request.{eqcond, request}
import play.api.mvc.Request

/**
  * @ ProjectName user_login.services.findScenario
  * @ author jeorch
  * @ date 18-9-7
  * @ Description: TODO
  */
case class findScenarioConnectGoods()(implicit val rq: Request[model.RootObject])
    extends Brick with CirceJsonapiSupport with parseToken {
    override val brick_name: String = "find scenario connect goods list"

    var request_data: request = new request
    var scenario_data = new scenario()
    var medicsnotice = new medicsnotice()
    var connect_goods: List[Map[String, Any]] = Nil
    var medicines: List[medicine] = Nil

    override def prepare: Unit = request_data = formJsonapi[request](rq.body)

    override def exec: Unit = queryObject[scenario](request_data) match {
        case Some(data) =>
            scenario_data = data
            connect_goods = scenario_data.current("connect_goods").asInstanceOf[List[Map[String, Any]]]
            val newsLst = connect_goods.flatMap(g => g("relationship").asInstanceOf[Map[String, Any]]("value").asInstanceOf[List[Map[String, Any]]].map(one => one("news").asInstanceOf[String]))
            medicsnotice.notices = Some(newsLst.map(x => {
                val n = new notice()
                n.news = x
                n
            }))

        case None => throw new Exception("Could not find specified scenario")
    }

    override def forwardTo(next_brick: String): Unit = {

        connect_goods.foreach { g =>
            val request = new request()
            val eq1 = eqcond()
            eq1.key = "id"
            eq1.`val` = g("id")
            request.res = "goods"
            request.eqcond = Some(List(eq1))
            val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(request).asJson.noSpaces)
            val result = decodeJson[model.RootObject](parseJson(str))
            val med = formJsonapi[medicine](result)
            medicines = medicines :+ med
        }
        medicsnotice.medicines = Some(medicines)

    }

    override def goback: model.RootObject = toJsonapi(medicsnotice)
}
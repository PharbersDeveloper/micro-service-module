package services.assess_report

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.entity.assess_report
import com.pharbers.models.request.request
import com.pharbers.models.service.{capablity_card_ability, evaluation_card_data}
import com.pharbers.pattern.frame.Brick
import play.api.mvc.Request
import services.parseToken

case class evaluationCards()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport with parseToken {

    override val brick_name: String = "find evaluation cards data"

    var request_data: request = new request()
    var report_data: assess_report = new assess_report()
    var ecd_Lst: List[evaluation_card_data] = Nil

    override def prepare: Unit = {
        parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        import com.pharbers.pattern.mongo.tm_report_db_inst.db_inst
        queryObject[assess_report](request_data) match {
            case Some(data) => report_data = data
            case None => throw new Exception("Could not find specified report")
        }

        val score_results: List[Map[String, Any]] = report_data.assessment("score_results").asInstanceOf[List[Map[String, Any]]]

        val map2cca: Map[String, Any] => capablity_card_ability = { x =>
            val cca = new capablity_card_ability()
            cca.title = x("item").asInstanceOf[String]
            cca.level = x("score").asInstanceOf[String]
            cca.desc = Map("title" -> "考核点", "content" -> x("test_point").asInstanceOf[List[String]]) ::
                    Map("title" -> "能力", "content" -> x("ability_describe").asInstanceOf[String]) :: Nil
            cca
        }

        val ecd1 = new evaluation_card_data
        ecd1.title = "战略规划"
        ecd1.content = "掌握信息是..."
        ecd1.ability = Some(score_results.take(3).map(map2cca))

        val ecd2 = new evaluation_card_data
        ecd2.title = "业务发展"
        ecd2.content = "掌握信息是..."
        ecd2.ability = Some(score_results.drop(3).dropRight(3).map(map2cca))

        val ecd3 = new evaluation_card_data
        ecd3.title = "团队管理能力"
        ecd3.content = "掌握信息是..."
        ecd3.ability = Some(score_results.drop(6).map(map2cca))

        ecd_Lst = ecd1 :: ecd2 :: ecd3 :: Nil
    }

    override def goback: model.RootObject = toJsonapi(ecd_Lst)
}

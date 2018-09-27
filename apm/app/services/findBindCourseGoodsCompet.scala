package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.bind_course_goods_compet
import com.pharbers.models.request.request
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findBindCourseGoodsCompet()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find bind course med compet list"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = null
    var competIdLst: List[bind_course_goods_compet] = Nil

    override def prepare: Unit = request_data = {
        parseToken(rq)
        formJsonapi[request](rq.body)
    }

    override def exec: Unit = competIdLst = queryMultipleObject[bind_course_goods_compet](request_data)

    override def goback: model.RootObject = toJsonapi(competIdLst)
}
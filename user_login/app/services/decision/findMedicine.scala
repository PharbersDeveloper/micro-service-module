package services.decision

import services.parseToken
import play.api.mvc.Request
import com.pharbers.macros._
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame.Brick
import com.pharbers.models.entity.medicine
import com.pharbers.models.request.request
import com.pharbers.pattern.mongo.client_db_inst._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport

/**
  * @ ProjectName user_login.services.findMedicine
  * @ author jeorch
  * @ date 18-9-7
  * @ Description: TODO
  */
case class findMedicine()(implicit val rq: Request[model.RootObject])
    extends Brick with CirceJsonapiSupport with parseToken {
    override val brick_name: String = "find medicine list"

    var request_data: request = new request
    var medicine_data = new medicine()

    override def prepare: Unit = request_data = formJsonapi[request](rq.body)

    override def exec: Unit = queryObject[medicine](request_data) match {
        case Some(data) => medicine_data = data
        case None => throw new Exception("Could not find specified medicine")
    }

    override def forwardTo(next_brick: String): Unit = {}

    override def goback: model.RootObject = toJsonapi(medicine_data)
}

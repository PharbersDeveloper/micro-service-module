package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.{bind_role_view_viewblock, bind_viewblock_viewblock, viewblock}
import com.pharbers.models.request.{eq2c, request}
import com.pharbers.models.service.auth
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.common.parseToken
import com.pharbers.pattern.frame.Brick
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

case class findLayout()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport with parseToken {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "find layout"
    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("auth").get.asInstanceOf[DBTrait[TraitRequest]]
    var auth: auth = null
    var request_data: request = null
    var viewblock_lst_data: List[viewblock] = Nil

    override def prepare: Unit = {
        auth = parseToken(rq)
        request_data = formJsonapi[request](rq.body)
    }

    override def exec: Unit = {
        val role_id_lst = auth.role.get.map(_.id)
        val bind_role_view_viewblock_data = queryMultipleObject[bind_role_view_viewblock](request_data)
        val viewblock_id_lst = bind_role_view_viewblock_data
                .filter(x => role_id_lst.contains(x.role_id))
                .map(x => x.viewblock_id)
        viewblock_lst_data = viewblock_id_lst.map(queryViewblock).map{x =>
            val tmp = querySubsViewblock(x.id)
            x.subs = if (tmp.isEmpty) None else Some(tmp)
            x
        }
    }

    override def goback: model.RootObject = toJsonapi(viewblock_lst_data)

    def queryViewblock(viewblock_id: String): viewblock = {
        val rq = new request
        rq.res = "viewblock"
        rq.eqcond = Some(eq2c("id", viewblock_id) :: Nil)
        queryObject[viewblock](rq).getOrElse(throw new Exception("viewblock not exist"))
    }

    def querySubsViewblock(parent_id: String): List[viewblock] = {
        val rq = new request
        rq.res = "bind_viewblock_viewblock"
        rq.eqcond = Some(eq2c("parent", parent_id) :: Nil)
        queryMultipleObject[bind_viewblock_viewblock](rq).map(x => queryViewblock(x.sub))
    }

}
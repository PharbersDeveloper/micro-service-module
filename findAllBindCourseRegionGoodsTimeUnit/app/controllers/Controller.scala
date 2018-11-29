package controllers

import java.util.Date

import play.api.mvc._
import io.circe.syntax._
import akka.actor.ActorSystem
import play.api.libs.circe.Circe
import javax.inject.{Inject, Singleton}
import com.pharbers.pattern.frame.PlayEntry
import com.pharbers.jsonapi.model.RootObject
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}

@Singleton
class Controller @Inject()(implicit val cc: ControllerComponents,
                           implicit val actorSystem: ActorSystem,
                           implicit val dbt: DBManagerModule,
                           implicit val rd: RedisManagerModule)
        extends AbstractController(cc) with Circe with CirceJsonapiSupport {

    import services.findAllBindCourseRegionGoodsTimeUnit

    def routes(pkg: String, step: Int): Action[RootObject] = Action(circe.json[RootObject]) { implicit request =>
        val start1 = new Date().getTime
        val a = Ok(PlayEntry().excution(findAllBindCourseRegionGoodsTimeUnit()).asJson)

        val end1 = new Date().getTime
        println("excution" + (end1 - start1))
        a
    }

    def routes2(pkg1: String  , pkg2: String, step: Int): Action[RootObject] = routes(pkg1 + "/" + pkg2, step)
}

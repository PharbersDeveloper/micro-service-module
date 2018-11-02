package controllers

import play.api.mvc._
import io.circe.syntax._
import akka.actor.ActorSystem
import play.api.libs.circe.Circe
import javax.inject.{Inject, Singleton}
import com.pharbers.pattern.frame.PlayEntry
import com.pharbers.jsonapi.model.RootObject
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import services.apmCalc

@Singleton
class Controller @Inject()(implicit val cc: ControllerComponents,
                           implicit val actorSystem: ActorSystem,
                           implicit val dbt: DBManagerModule,
                           implicit val rd: RedisManagerModule)
        extends AbstractController(cc) with Circe with CirceJsonapiSupport {



    def routes(pkg: String, step: Int): Action[RootObject] = Action(circe.json[RootObject]) { implicit request =>
        Ok(PlayEntry().excution(apmCalc()).asJson)
    }

    def routes2(pkg1: String  , pkg2: String, step: Int): Action[RootObject] = routes(pkg1 + "/" + pkg2, step)
}

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

@Singleton
class Controller @Inject()(implicit val cc: ControllerComponents,
                           implicit val actorSystem: ActorSystem,
                           implicit val dbt: DBManagerModule,
                           implicit val rd: RedisManagerModule)
        extends AbstractController(cc) with Circe with CirceJsonapiSupport {

    import services._

    def routes(pkg: String, step: Int): Action[RootObject] = Action(circe.json[RootObject]) { implicit request =>
        Ok(
            (pkg, step) match {
                case ("login", 0) => PlayEntry().excution(login()).asJson
                case ("login", 1) => PlayEntry().excution(encryptToken()).asJson

                case ("courseLst", 0) => PlayEntry().excution(findBindUserCourse()).asJson
                case ("courseLst", 1) => PlayEntry().excution(findCourseById()).asJson

                case ("exam", 0) => PlayEntry().excution(findCourseById()).asJson

                case ("findMedById", 0) => PlayEntry().excution(findMedById()).asJson
                case ("findBindCourseGoods", 0) => PlayEntry().excution(findBindCourseGoods()).asJson
                case ("findBindCourseGoodsCompet", 0) => PlayEntry().excution(findBindCourseGoodsCompet()).asJson

                case ("regionLst", 0) => PlayEntry().excution(findBindCourseRegion()).asJson
                case ("regionLst", 1) => PlayEntry().excution(findRegionById()).asJson

                case ("findSalesById", 0) => PlayEntry().excution(findSalesById()).asJson
                case ("findBindCourseRegionGoodsYmSales", 0) => PlayEntry().excution(findBindCourseRegionGoodsYmSales()).asJson

                case ("findRepById", 0) => PlayEntry().excution(findRepById()).asJson
                case ("findBindCourseRegionRep", 0) => PlayEntry().excution(findBindCourseRegionRep()).asJson

                case ("findRadarById", 0) => PlayEntry().excution(findRadarById()).asJson
                case ("findBindCourseRegionRadar", 0) => PlayEntry().excution(findBindCourseRegionRadar()).asJson

                case ("findBusinessById", 0) => PlayEntry().excution(findBusinessById()).asJson
                case ("findBindCourseRegionBusiness", 0) => PlayEntry().excution(findBindCourseRegionBusiness()).asJson

                case ("findRepBehaviorById", 0) => PlayEntry().excution(findRepBehaviorById()).asJson
                case ("findBindCourseRegionYmRepBehavior", 0) => PlayEntry().excution(findBindCourseRegionYmRepBehavior()).asJson

                case (_, _) => throw new Exception("Bad Request for input")
            }
        )
    }

    def routes2(pkg1: String, pkg2: String, step: Int): Action[RootObject] = routes(pkg1 + "/" + pkg2, step)
}

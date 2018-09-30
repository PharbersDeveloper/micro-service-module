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

                case ("exam", 0) => PlayEntry().excution(pushPaper()).asJson
                case ("exam", 1) => PlayEntry().excution(pushBindUserCoursePaper()).asJson
                case ("exam", 2) => PlayEntry().excution(pushPaperInputByCourse()).asJson
                case ("exam", 3) => PlayEntry().excution(pushPaperInput()).asJson

                case ("findCourseGoods", 0) => PlayEntry().excution(findBindCourseGoods()).asJson
                case ("findCourseGoods", 1) => PlayEntry().excution(findMedById()).asJson

                case ("findCompetGoods", 0) => PlayEntry().excution(findBindCourseGoodsCompet()).asJson
                case ("findCompetGoods", 1) => PlayEntry().excution(findMedById()).asJson

                case ("regionLst", 0) => PlayEntry().excution(findBindCourseRegion()).asJson
                case ("regionLst", 1) => PlayEntry().excution(findRegionById()).asJson

                case ("findMedSales", 0) => PlayEntry().excution(findBindCourseRegionGoodsYmSales()).asJson
                case ("findMedSales", 1) => PlayEntry().excution(findSalesById()).asJson

                case ("findRegionRep", 0) => PlayEntry().excution(findBindCourseRegionRep()).asJson
                case ("findRegionRep", 1) => PlayEntry().excution(findRepById()).asJson

                case ("findRadarById", 0) => PlayEntry().excution(findRadarById()).asJson
                case ("findBindCourseRegionRadar", 0) => PlayEntry().excution(findBindCourseRegionRadar()).asJson

                case ("findBusinessById", 0) => PlayEntry().excution(findBusinessById()).asJson
                case ("findBindCourseRegionBusiness", 0) => PlayEntry().excution(findBindCourseRegionBusiness()).asJson

                case ("findRepBehaviorById", 0) => PlayEntry().excution(findRepBehaviorById()).asJson
                case ("findBindCourseRegionYmRepBehavior", 0) => PlayEntry().excution(findBindCourseRegionYmRepBehavior()).asJson

                case ("findPaper", 0) => PlayEntry().excution(findBindUserCoursePaperByToken()).asJson
                case ("findPaper", 1) => PlayEntry().excution(findPaperById()).asJson
                case ("findPaper", 2) => PlayEntry().excution(findBindUserCoursePaperByPaper()).asJson
                case ("findPaper", 3) => PlayEntry().excution(findCourseById()).asJson

                case ("answer", 0) => PlayEntry().excution(updatePaperInput()).asJson

                case ("actionPlanLst", 0) => PlayEntry().excution(findBindCourseActionPlan()).asJson
                case ("actionPlanLst", 1) => PlayEntry().excution(findActionPlanById()).asJson

                case ("findQuarterReport", 0) => PlayEntry().excution(findBindCourseQuarterReport()).asJson
                case ("findQuarterReport", 1) => PlayEntry().excution(findQuarterReportById()).asJson

                case ("findReportMedSales", 0) => PlayEntry().excution(findBindPaperRegionGoodsYmReport()).asJson
                case ("findReportMedSales", 1) => PlayEntry().excution(findReportSalesById()).asJson

                case (_, _) => throw new Exception("Bad Request for input")
            }
        )
    }

    def routes2(pkg1: String, pkg2: String, step: Int): Action[RootObject] = routes(pkg1 + "/" + pkg2, step)
}

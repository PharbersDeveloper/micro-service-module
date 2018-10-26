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
                case ("companyRegister", _) => PlayEntry().excution(companyRegister()).asJson
                case ("login", 0) => PlayEntry().excution(login()).asJson
                case ("layout", _) => PlayEntry().excution(findLayout()).asJson
                case ("exam", 0) => PlayEntry().excution(pushPaper()).asJson
                case ("findPaper", 0) => PlayEntry().excution(findBindUserCoursePaperByToken()).asJson
                case ("courseLst", 0) => PlayEntry().excution(findBindUserCourse()).asJson
                case ("findCourseGoods", 0) => PlayEntry().excution(findBindCourseGoods()).asJson
                case ("findCompetGoods", 0) => PlayEntry().excution(findBindCourseGoodsCompet()).asJson
                case ("regionLst", 0) => PlayEntry().excution(findBindCourseRegion()).asJson
                case ("findMedSales", 0) => PlayEntry().excution(findBindCourseRegionGoodsYmSales()).asJson
                case ("findRegionRep", 0) => PlayEntry().excution(findBindCourseRegionRep()).asJson
                case ("answer", 0) => PlayEntry().excution(updatePaperInput()).asJson
                case ("findRadarFigure", 0) => PlayEntry().excution(findBindCourseRegionRadar()).asJson
                case ("findBusinessReport", 0) => PlayEntry().excution(findBindCourseRegionBusiness()).asJson
                case ("findRepBehavior", 0) => PlayEntry().excution(findBindCourseRegionYmRepBehavior()).asJson
                case ("actionPlanLst", 0) => PlayEntry().excution(findBindCourseActionPlan()).asJson
                case ("findQuarterReport", 0) => PlayEntry().excution(findBindCourseQuarterReport()).asJson
                case ("callAPMr", 0) => PlayEntry().excution(updatePaper2Done()).asJson
                case ("findReportMedSales", 0) => PlayEntry().excution(findBindPaperRegionGoodsYmReport()).asJson
                case ("paperInputLst", 0) => PlayEntry().excution(findPaperInput()).asJson
                case ("findExamRequire", 0) => PlayEntry().excution(findExamRequireIdByCourse()).asJson
                case ("findAllMedSales", 0) => PlayEntry().excution(findAllBindCourseRegionGoodsYmSales()).asJson
                case ("findAllReportMedSales", 0) => PlayEntry().excution(findAllBindPaperRegionGoodsYmReport()).asJson
                case ("findAllPaper", 0) => PlayEntry().excution(findAllBindUserCoursePaperByToken()).asJson

                case (_, _) => throw new Exception("Bad Request for input")
            }
        )
    }

    def routes2(pkg1: String  , pkg2: String, step: Int): Action[RootObject] = routes(pkg1 + "/" + pkg2, step)
}

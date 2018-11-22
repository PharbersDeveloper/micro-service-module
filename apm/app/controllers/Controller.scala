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
    import services.RootObject2DownloadStream.impl

    def routes(pkg: String, step: Int): Action[RootObject] = Action(circe.json[RootObject]) { implicit request =>
        (pkg, step) match {
            case ("login", 0) => Ok(PlayEntry().excution(login()).asJson)
            case ("emailVerify", 0) => Ok(PlayEntry().excution(emailVerify()).asJson)
            case ("userRegister", 0) => Ok(PlayEntry().excution(userRegister()).asJson)
            case ("companyRegister", 0) => Ok(PlayEntry().excution(companyRegister()).asJson)
            case ("layout", 0) => Ok(PlayEntry().excution(findLayout()).asJson)
            case ("exam", 0) => Ok(PlayEntry().excution(pushPaper()).asJson)
            case ("courseLst", 0) => Ok(PlayEntry().excution(findBindUserCourse()).asJson)
            case ("findCourseGoods", 0) => Ok(PlayEntry().excution(findBindCourseGoods()).asJson)
            case ("findCompetGoods", 0) => Ok(PlayEntry().excution(findBindCourseGoodsCompet()).asJson)
            case ("regionLst", 0) => Ok(PlayEntry().excution(findBindCourseRegion()).asJson)
            case ("findRegionRep", 0) => Ok(PlayEntry().excution(findBindCourseRegionRep()).asJson)
            case ("findRadarFigure", 0) => Ok(PlayEntry().excution(findBindCourseRegionRadar()).asJson)
            case ("findBusinessReport", 0) => Ok(PlayEntry().excution(findBindCourseRegionBusiness()).asJson)
            case ("findRepBehavior", 0) => Ok(PlayEntry().excution(findBindCourseRegionTimeRepBehavior()).asJson)
            case ("answer", 0) => Ok(PlayEntry().excution(updatePaperInput()).asJson)
            case ("actionPlanLst", 0) => Ok(PlayEntry().excution(findBindCourseActionPlan()).asJson)
            case ("findQuarterReport", 0) => Ok(PlayEntry().excution(findBindCourseQuarterReport()).asJson)
            case ("apmCalc", 0) => Ok(PlayEntry().excution(updatePaper2Done()).asJson)
            case ("paperInputLst", 0) => Ok(PlayEntry().excution(findPaperInput()).asJson)
            case ("findExamRequire", 0) => Ok(PlayEntry().excution(findExamRequireIdByCourse()).asJson)
            case ("findAllPaper", 0) => Ok(PlayEntry().excution(findAllBindUserCoursePaperByToken()).asJson)
            case ("findAllMedSales", 0) => Ok(PlayEntry().excution(findAllBindCourseRegionGoodsTimeSales()).asJson)
            case ("findAllMedUnit", 0) => Ok(PlayEntry().excution(findAllBindCourseRegionGoodsTimeUnit()).asJson)
            case ("findAllMedPatient", 0) => Ok(PlayEntry().excution(findAllBindCourseRegionGoodsTimePatient()).asJson)
            case ("findAllReportMedUnit", 0) => Ok(PlayEntry().excution(findAllBindPaperRegionGoodsTimeReport()).asJson)
            case ("pushBindTeacherStudentTimePaper", 0) => Ok(PlayEntry().excution(pushBindTeacherStudentTimePaper()).asJson)
            case ("findBindTeacherStudentTimePaper", 0) => Ok(PlayEntry().excution(findBindTeacherStudentTimePaper()).asJson)
            case ("downloadStudentReport", 0) => Ok(PlayEntry().excution(downloadStudentReport()).toDownloadStream).as("excel/csv")

            case (_, _) => throw new Exception("Bad Request for input")
        }
    }

    def routes2(pkg1: String, pkg2: String, step: Int): Action[RootObject] = routes(pkg1 + "/" + pkg2, step)
}

package entity

import com.pharbers.driver.PhRedisDriver

object writeBrickRegistry extends App {
    private val routes: Map[String, List[String]] = Map(
        "/api/v1/login/" -> List("127.0.0.1", "127.0.0.1"),
        "/api/v1/proposalLst/" -> List("127.0.0.1", "127.0.0.1"),
        "/api/v1/layoutLst/" -> List("127.0.0.1"),
        "/api/v1/medicsnotices/" -> List("127.0.0.1", "127.0.0.1"),
        "/api/v1/managerInputInfo/" -> List("127.0.0.1"),
        "/api/v1/hospitalinfo/" -> List("127.0.0.1"),
        "/api/v1/repinputcards/" -> List("127.0.0.1"),
        "/api/v1/taskAllot/" -> List("127.0.0.1", "127.0.0.1"),
        "/api/v1/reportLayout/" -> List("127.0.0.1"),
        "/api/v1/reportWhich/" -> List("127.0.0.1"),
        "/api/v1/cardsIndex/" -> List("127.0.0.1"),
        "/api/v1/tableIndex/" -> List("127.0.0.1"),
        "/api/v1/cardsHospProduct/" -> List("127.0.0.1"),
        "/api/v1/tableHospProduct/" -> List("127.0.0.1"),
        "/api/v1/cardsRepresentProduct/" -> List("127.0.0.1"),
        "/api/v1/tableRepresentProduct/" -> List("127.0.0.1"),
        "/api/v1/cardsResource/" -> List("127.0.0.1"),
        "/api/v1/tableResource/" -> List("127.0.0.1"),
        "/api/v1/cardsRepresentTarget/" -> List("127.0.0.1"),
        "/api/v1/tableRepresentTarget/" -> List("127.0.0.1"),
        "/api/v1/cardsRepresentAbility/" -> List("127.0.0.1"),
        "/api/v1/tableRepresentAbility/" -> List("127.0.0.1"),
        "/api/v1/assessReportLayout/" -> List("127.0.0.1"),
        "/api/v1/evaluationRadar/" -> List("127.0.0.1"),
        "/api/v1/evaluationLine/" -> List("127.0.0.1"),
        "/api/v1/evaluationCards/" -> List("127.0.0.1")
    ) ++ Map(
        "/api/v1/login/" -> List("127.0.0.1", "127.0.0.1"),
        "/api/v1/courseLst/" -> List("127.0.0.1", "apm_findcoursebyid"),
        "/api/v1/exam/" -> List("127.0.0.1", "127.0.0.1", "127.0.0.1", "127.0.0.1"),
        "/api/v1/findCourseGoods/" -> List("127.0.0.1", "findmedbyid"),
        "/api/v1/findCompetGoods/" -> List("127.0.0.1", "findmedbyid"),
        "/api/v1/regionLst/" -> List("127.0.0.1", "apm_findregionbyid"),
        "/api/v1/findMedSales/" -> List("127.0.0.1", "apm_findsalesbyid"),
        "/api/v1/findRegionRep/" -> List("127.0.0.1", "apm_findregionbyid"),

        "/api/v1/findPaper/" -> List("127.0.0.1", "127.0.0.1", "127.0.0.1", "apm_findcoursebyid"),
        "/api/v1/answer/" -> List("127.0.0.1"),
        "/api/v1/actionPlanLst/" -> List("127.0.0.1", "apm_findactionplanbyid"),
        "/api/v1/findQuarterReport/" -> List("127.0.0.1", "apm_findquarterreportbyid"),
        "/api/v1/callAPMr/" -> List("127.0.0.1"),
        "/api/v1/findReportMedSales/" -> List("127.0.0.1", "apm_findreportsalesbyid"),
        "/api/v1/findRadarFigure/" -> List("127.0.0.1", "apm_findradarbyid"),
        "/api/v1/findBusinessReport/" -> List("127.0.0.1", "apm_findbusinessbyid"),
        "/api/v1/findRepBehavior/" -> List("127.0.0.1", "apm_findrepbehaviorbyid"),
        "/api/v1/paperInputLst/" -> List("127.0.0.1"),
        "/api/v1/findExamRequire/" -> List("127.0.0.1", "127.0.0.1")
    )

    val rd = new PhRedisDriver()
    routes.foreach { x =>
        rd.delete(x._1)
        rd.addListRight(x._1, x._2: _*)
    }

    val test_route = "/api/v1/callAPMr/"
    println(rd.getListSize(test_route))
    println(rd.getListAllValue(test_route))
    println("write brick registry success")
}

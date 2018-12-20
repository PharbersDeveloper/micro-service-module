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
    )

    val rd = new PhRedisDriver()
    routes.foreach { x =>
        rd.delete(x._1)
        rd.addListRight(x._1, x._2: _*)
    }

    val test_route = "/api/v1/taskAllot/"
    println(rd.getListSize(test_route))
    println(rd.getListAllValue(test_route))
    println("write brick registry success")
}

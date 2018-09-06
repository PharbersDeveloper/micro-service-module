import com.pharbers.driver.PhRedisDriver

object writeBrickRegistry extends App {
    private val routes: Map[String, List[String]] = Map(
        "/api/v1/login/" -> List("127.0.0.33", "127.0.0.1"),
        "/api/v1/proposalLst/" -> List("127.0.0.1"),
        "/api/v1/layoutLst/" -> List("127.0.0.1")
    )

    val rd = new PhRedisDriver()
    routes.foreach { x =>
        rd.delete(x._1)
        rd.addListRight(x._1, x._2: _*)
    }

    val test_route = "/api/v1/proposalLst/"
    println(rd.getListSize(test_route))
    println(rd.getListAllValue(test_route))
    println("write brick registry success")
}

import com.pharbers.driver.PhRedisDriver

object writeBrickRegistry extends App {
    private val routes: Map[String, List[String]] = Map(
        "/api/v1/login/" -> List("127.0.0.1", "127.0.0.1"),
        "/api/v1/courseLst/" -> List("127.0.0.1", "127.0.0.1"),
        "/api/v1/findMedById/" -> List("127.0.0.1"),
        "/api/v1/findBindCourseGoods/" -> List("127.0.0.1"),
        "/api/v1/findBindCourseGoodsCompet/" -> List("127.0.0.1"),
        "/api/v1/regionLst/" -> List("127.0.0.1", "127.0.0.1"),
        "/api/v1/findSalesById/" -> List("127.0.0.1"),
        "/api/v1/findBindCourseRegionGoodsYmSales/" -> List("127.0.0.1"),
        "/api/v1/findRepById/" -> List("127.0.0.1"),
        "/api/v1/findBindCourseRegionRep/" -> List("127.0.0.1"),
        "/api/v1/findRadarById/" -> List("127.0.0.1"),
        "/api/v1/findBindCourseRegionRadar/" -> List("127.0.0.1"),
        "/api/v1/findBusinessById/" -> List("127.0.0.1"),
        "/api/v1/findBindCourseRegionBusiness/" -> List("127.0.0.1"),
        "/api/v1/findRepBehaviorById/" -> List("127.0.0.1"),
        "/api/v1/findBindCourseRegionYmRepBehavior/" -> List("127.0.0.1")
    )

    val rd = new PhRedisDriver()
    routes.foreach { x =>
        rd.delete(x._1)
        rd.addListRight(x._1, x._2: _*)
    }

    val test_route = "/api/v1/regionLst/"
    println(rd.getListSize(test_route))
    println(rd.getListAllValue(test_route))
    println("write brick registry success")
}

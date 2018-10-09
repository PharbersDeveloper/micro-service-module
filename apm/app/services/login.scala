package services

import java.util.concurrent.{ExecutorService, Executors}

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame.{forward, _}
import com.pharbers.models.entity.user
import com.pharbers.models.service.auth
import com.pharbers.models.request.request
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.module.DBManagerModule
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.macros.convert.mongodb.TraitRequest

case class login()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule)
        extends Brick with CirceJsonapiSupport {

    import io.circe.syntax._
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "verify email"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]

    var request_data: request = new request()
    var auth_data: auth = new auth()

    override def prepare: Unit = request_data = formJsonapi[request](rq.body)

    override def exec: Unit = {
        val user = queryObject[user](request_data).getOrElse(throw new Exception("email or password error"))
        user.password = ""
        auth_data.user = Some(user)
    }

    override def forwardTo(next_brick: String): Unit = {
//        val threadPool: ExecutorService = Executors.newFixedThreadPool(5)
//        for(i <- 1 to 3){
//            println(i)
////            forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(auth_data).asJson.noSpaces).check()
//            threadPool.execute(new ThreadDemo(next_brick))
//        }

        val str = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(auth_data).asJson.noSpaces).check()
//        println(str)
//        val str1 = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(auth_data).asJson.noSpaces).check()
//        println(str1)
//        val str2 = forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(auth_data).asJson.noSpaces).check()
//        println(str2)
        val rootObject = decodeJson[model.RootObject](parseJson(str))
        auth_data.token = formJsonapi[auth](rootObject).token
    }

    //定义线程类
    class ThreadDemo(next_brick: String) extends Runnable{
        override def run(){
//            forward(next_brick)(api + (cur_step + 1)).post(toJsonapi(auth_data).asJson.noSpaces).check()
            println(forward(next_brick)("/api/v1/courseLst/0").post("{}").check())
        }
    }

    override def goback: model.RootObject = toJsonapi(auth_data)
}

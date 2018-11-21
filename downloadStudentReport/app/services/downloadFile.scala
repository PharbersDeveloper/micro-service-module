package services

import java.io.{File, FileInputStream}

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.representative
import com.pharbers.models.request.request
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame._
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.mvc.Request

//case class downloadFile()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
//        extends Brick with CirceJsonapiSupport {
//
//    import com.pharbers.macros._
//    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
//
//    override val brick_name: String = "download paperInput"
//
//    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
//
//    var request_data: request = null
//    var rep_data: representative = null
//
//    override def prepare: Unit = {
//        request_data = formJsonapi[request](rq.body)
//    }
//
//    override def exec: Unit = rep_data =
//            queryObject[representative](request_data).getOrElse(throw new Exception("Could not find specified representative"))
//
//    override def goback: model.RootObject = toJsonapi(rep_data)
//}
case class downloadFile() {
    def download(name : String) : Array[Byte] = {
        val reVal2 = "abc".getBytes()
        val filepath = "/Users/clock/Downloads/knowledge-framework.png"
        val file = new File(filepath)
        val reVal : Array[Byte] = new Array[Byte](file.length.intValue)
        new FileInputStream(file).read(reVal)
        reVal2
    }
}

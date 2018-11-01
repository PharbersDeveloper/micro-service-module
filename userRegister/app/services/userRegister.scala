package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity._
import com.pharbers.models.request._
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame.Brick
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import org.apache.commons.codec.digest.DigestUtils
import play.api.mvc.Request

case class userRegister()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
    import io.circe.syntax._

    override val brick_name: String = "user register"
    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("auth").get.asInstanceOf[DBTrait[TraitRequest]]
    var user_data: user = null

    override def prepare: Unit = user_data = formJsonapi[user](rq.body)

    override def exec: Unit = {
        val company_id: String = "5bd16a83ed925c081c056966"
        val role_id: String = "5bd1a068eeefcc015029cb88"
        val course_id_1: String = "5baa0e58eeefcc05923c9414"
        val course_id_2: String = "5baa1d78eeefcc05923c9424"
        emailVerify(user_data.email)
        val user_id = insertUser(user_data, company_id)
        insertBindCompanyUser(company_id, user_id)
        insertBindUserRole(user_id, role_id)
        insertBindUserCourse(user_id, course_id_1)
        insertBindUserCourse(user_id, course_id_2)
    }

    override def goback: model.RootObject = toJsonapi(user_data)

    // 验证邮箱是否被使用
    def emailVerify(email: String): Unit = {
        val rq = new request()
        rq.res = "user"
        rq.eqcond = Some(eq2c("email", email) :: Nil)
        println(rq)
        queryObject[user](rq) match {
            case Some(_) => throw new Exception("user email has been use")
            case None => Unit
        }
    }

    def insertUser(user_data: user, company_id: String): String = {
        user_data.image = "https://pharbers-images.oss-cn-beijing.aliyuncs.com/pharbers-tm-hospital-list-ember-addon/hosp_avatar.png"

        // 使用默认公司加密密码
        user_data.password = DigestUtils.md5Hex(user_data.password).toUpperCase
        insertObject[user](user_data).get("_id").toString
    }

    def insertBindCompanyUser(company_id: String, user_id: String): Unit = {
        val bind_data = new bind_company_user
        bind_data.`type` = "bind_company_user"
        bind_data.company_id = company_id
        bind_data.user_id = user_id
        insertObject[bind_company_user](bind_data)
    }

    def insertBindUserRole(user_id: String, role_id: String): Unit = {
        val bind_data = new bind_user_role
        bind_data.`type` = "bind_user_role"
        bind_data.user_id = user_id
        bind_data.role_id = role_id
        insertObject[bind_user_role](bind_data)
    }

    def insertBindUserCourse(user_id: String, course_id: String): Unit = {
        implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("client").get.asInstanceOf[DBTrait[TraitRequest]]
        val bind_data = new bind_user_course
        bind_data.`type` = "bind_user_course"
        bind_data.user_id = user_id
        bind_data.course_id = course_id
        insertObject[bind_user_course](bind_data)
    }

}
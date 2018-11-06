package services

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.entity.auth._
import com.pharbers.models.request._
import com.pharbers.models.service.auth
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.frame.Brick
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import com.pharbers.security.cryptogram.rsa.RSA
import org.apache.commons.codec.digest.DigestUtils
import org.bson.types.ObjectId
import play.api.mvc.Request

case class login()(implicit val rq: Request[model.RootObject], dbt: DBManagerModule, rd: RedisManagerModule)
        extends Brick with CirceJsonapiSupport {

    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
    import io.circe.syntax._

    override val brick_name: String = "login"
    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("auth").get.asInstanceOf[DBTrait[TraitRequest]]
    var login_data: request = null
    val auth = new auth()

    override def prepare: Unit = login_data = formJsonapi[request](rq.body)

    override def exec: Unit = {

        // 保存用户输入的密码密文
        val password = login_data.eqcond.map(x => x.find(_.key == "password").map(_.`val`.toString).getOrElse("")).get
        // 用户登录来源
        val login_source = login_data.eqcond.map(x => x.find(_.key == "login_source").map(_.`val`.toString).getOrElse("")).get

        login_data.eqcond = login_data.eqcond.map(x => x.filter(_.key == "email"))
        val user_data = queryObject[user](login_data).getOrElse(throw new Exception("user not exist"))


        // 查看用户所属公司是否有登录来源的访问权限
        val bind_company_user_data = queryBindCompanyUser(user_data.id)
        val bind_company_product_data = queryBindCompanyProduct(bind_company_user_data.company_id)
        val product_lst_data = bind_company_product_data.map(x => queryProduct(x.product_id))

        if(!product_lst_data.map(_.name).contains(login_source)) throw new Exception("user not exist")


        // 获得用户所属公司的私钥并验证密码
        val bind_company_secret_data = queryBindCompanySecret(bind_company_user_data.company_id)
        val secret_data = querySecret(bind_company_secret_data.secret_id)

        val decrypted = try {
            RSA(prk = secret_data.private_key).decrypt(password)
        } catch {
            case _: Exception => throw new Exception("email or password error")
        }

        if (DigestUtils.md5Hex(decrypted).toUpperCase != user_data.password)
            throw new Exception("email or password error")


        // 获得公司信息和角色信息
        val company_data = queryCompany(bind_company_user_data.company_id)
        val bind_user_role_data = queryBindUserRole(user_data.id)
        val role_list_data = bind_user_role_data.map(x => queryRole(x.role_id))


        // 以token为键写入redis
        auth.company = Some(company_data)
        auth.user = Some(user_data)
        auth.role = Some(role_list_data)
        auth.product = Some(product_lst_data)

        auth.token = ObjectId.get().toString
        rd.addString(auth.token, toJsonapi(auth).asJson.noSpaces)
        rd.expire(auth.token, auth.token_expire)
    }

    override def goback: model.RootObject = toJsonapi(auth)

    def queryBindCompanyUser(user_id: String): bind_company_user = {
        val rq = new request
        rq.res = "bind_company_user"
        rq.eqcond = Some(eq2c("user_id", user_id) :: Nil)
        queryObject[bind_company_user](rq).getOrElse(throw new Exception(""))
    }

    def queryBindCompanyProduct(company_id: String): List[bind_company_product] = {
        val rq = new request
        rq.res = "bind_company_product"
        rq.eqcond = Some(eq2c("company_id", company_id) :: Nil)
        queryMultipleObject[bind_company_product](rq)
    }

    def queryProduct(product_id: String): product = {
        val rq = new request
        rq.res = "product"
        rq.eqcond = Some(eq2c("id", product_id) :: Nil)
        queryObject[product](rq).getOrElse(throw new Exception("product not exist"))
    }

    def queryBindCompanySecret(company_id: String): bind_company_secret = {
        val rq = new request
        rq.res = "bind_company_secret"
        rq.eqcond = Some(eq2c("company_id", company_id) :: Nil)
        queryObject[bind_company_secret](rq).getOrElse(throw new Exception(""))
    }

    def querySecret(secret_id: String): secret = {
        val rq = new request
        rq.res = "secret"
        rq.eqcond = Some(eq2c("id", secret_id) :: Nil)
        queryObject[secret](rq).getOrElse(throw new Exception("secret not exist"))
    }

    def queryCompany(company_id: String): company = {
        val rq = new request
        rq.res = "company"
        rq.eqcond = Some(eq2c("id", company_id) :: Nil)
        queryObject[company](rq).getOrElse(throw new Exception("company not exist"))
    }

    def queryBindUserRole(user_id: String): List[bind_user_role] = {
        val rq = new request
        rq.res = "bind_user_role"
        rq.eqcond = Some(eq2c("user_id", user_id) :: Nil)
        queryMultipleObject[bind_user_role](rq)
    }

    def queryRole(role_id: String): role = {
        val rq = new request
        rq.res = "role"
        rq.eqcond = Some(eq2c("id", role_id) :: Nil)
        queryObject[role](rq).getOrElse(throw new Exception("role not exist"))
    }

}
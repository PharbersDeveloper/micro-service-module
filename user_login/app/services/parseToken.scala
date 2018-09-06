package services

import play.api.mvc.Request
import com.pharbers.jsonapi.model
import com.pharbers.models.{auth, user}
import com.pharbers.driver.PhRedisDriver

trait parseToken {
    def parseToken(request: Request[model.RootObject]): auth = {
        val token = request.headers.get("Authorization")
                .getOrElse(throw new Exception("token parse error"))
                .split(" ").last
        val rd = new PhRedisDriver()
        val u = user()
        u.id = rd.getMapValue(token, "user_id")
        u.email = rd.getMapValue(token, "email")
        u.user_name = rd.getMapValue(token, "user_name")
        val a = auth()
        a.token = token
        a.user = Some(u)
        a
    }
}
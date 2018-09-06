package entity

import com.pharbers.models.user
import com.pharbers.macros.api.commonEntity
import com.pharbers.macros.common.connecting.One2OneConn

@One2OneConn[user]("user")
case class auth2() extends commonEntity {
    var token: String = ""
}

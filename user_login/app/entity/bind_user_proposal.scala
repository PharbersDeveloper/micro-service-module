package entity

import com.pharbers.models.user
import com.pharbers.macros.api.commonEntity
import com.pharbers.macros.common.connecting.One2OneConn

case class bind_user_proposal() extends commonEntity {
    var user_id: String = ""
    var proposal_id: String = ""
}

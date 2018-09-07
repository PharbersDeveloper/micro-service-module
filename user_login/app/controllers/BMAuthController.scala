package controllers

import play.api.mvc._
import io.circe.syntax._
import akka.actor.ActorSystem
import play.api.libs.circe.Circe
import javax.inject.{Inject, Singleton}
import com.pharbers.pattern.frame.PlayEntry
import com.pharbers.jsonapi.model.RootObject
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport

@Singleton
class BMAuthController @Inject()(implicit val cc: ControllerComponents, implicit val actorSystem: ActorSystem)
        extends AbstractController(cc) with Circe with CirceJsonapiSupport {

    import services._

    def routes(pkg: String, step: Int): Action[RootObject] = Action(circe.json[RootObject]) { implicit request =>
        Ok(
            (pkg, step) match {
                case ("login", 0) => PlayEntry().excution(login()).asJson
                case ("login", 1) => PlayEntry().excution(encryptToken()).asJson
                case ("proposalLst", 0) => PlayEntry().excution(queryBindUserProposal()).asJson
                case ("proposalLst", 1) => PlayEntry().excution(findProposal()).asJson
                case ("medicsnotices", 0) => PlayEntry().excution(findScenarioConnectGoods()).asJson
                case ("medicsnotices", 1) => PlayEntry().excution(findMedicine()).asJson
                case ("layoutLst", 0) => PlayEntry().excution(layoutLst()).asJson
                case ("repinputcards", 0) => PlayEntry().excution(queryRepByScen()).asJson
                case ("test", 0) => PlayEntry().excution(findProposal()).asJson
                case (_, _) => throw new Exception("Bad Request for input")
            }
        )
    }

    def routes2(pkg1: String, pkg2: String, step: Int): Action[RootObject] = routes(pkg1 + "/" + pkg2, step)
}

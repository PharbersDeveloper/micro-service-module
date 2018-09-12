package controllers

import play.api.mvc._
import io.circe.syntax._
import akka.actor.ActorSystem
import play.api.libs.circe.Circe
import javax.inject.{Inject, Singleton}
import com.pharbers.pattern.frame.PlayEntry
import com.pharbers.jsonapi.model.RootObject
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import services.decision._
import services.report._

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
                case ("layoutLst", 0) => PlayEntry().excution(layoutLst()).asJson
                case ("medicsnotices", 0) => PlayEntry().excution(findScenarioConnectGoods()).asJson
                case ("medicsnotices", 1) => PlayEntry().excution(findMedicine()).asJson
                case ("hospitalinfo", 0) => PlayEntry().excution(queryHospByScen()).asJson
                case ("repinputcards", 0) => PlayEntry().excution(queryRepByScen()).asJson
                case ("reportLayout", 0) => PlayEntry().excution(reportLayout()).asJson
                case ("reportWhich", 0) => PlayEntry().excution(reportWhich()).asJson
                case ("cardsIndex", 0) => PlayEntry().excution(cardsIndex()).asJson
                case ("tableIndex", 0) => PlayEntry().excution(tableIndex()).asJson
                case ("cardsHospProduct", 0) => PlayEntry().excution(cardsHospProduct()).asJson
                case ("tableHospProduct", 0) => PlayEntry().excution(tableHospProduct()).asJson
                case ("cardsRepresentProduct", 0) => PlayEntry().excution(cardsRepresentProduct()).asJson
                case ("tableRepresentProduct", 0) => PlayEntry().excution(tableRepresentProduct()).asJson
                case ("cardsResource", 0) => PlayEntry().excution(cardsResource()).asJson
                case ("tableResource", 0) => PlayEntry().excution(tableResource()).asJson
                case ("cardsRepresentTarget", 0) => PlayEntry().excution(cardsRepresentTarget()).asJson
                case ("tableRepresentTarget", 0) => PlayEntry().excution(tableRepresentTarget()).asJson
                case ("cardsRepresentAbility", 0) => PlayEntry().excution(cardsRepresentAbility()).asJson
                case ("tableRepresentAbility", 0) => PlayEntry().excution(tableRepresentAbility()).asJson
                case ("test", 0) => PlayEntry().excution(findProposal()).asJson
                case (_, _) => throw new Exception("Bad Request for input")
            }
        )
    }

    def routes2(pkg1: String, pkg2: String, step: Int): Action[RootObject] = routes(pkg1 + "/" + pkg2, step)
}

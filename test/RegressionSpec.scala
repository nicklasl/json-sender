import controllers.TestHelper._
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Cookie}
import play.api.test.Helpers._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class RegressionSpec extends Specification {


  "Regression" should {
    sequential

    val sampleJson = Json.parse("{\"widget\": { \"debug\": \"on\", \"window\": { \"title\": \"Sample Konfabulator Widget\", \"name\": \"main_window\", \"width\": 500, \"height\": 500 }, \"image\": { \"src\": \"Images/Sun.png\", \"name\": \"sun1\", \"hOffset\": 250, \"vOffset\": 250, \"alignment\": \"center\" }, \"text\": { \"data\": \"Click Here\", \"size\": 36, \"style\": \"bold\", \"name\": \"text1\", \"hOffset\": 250, \"vOffset\": 100, \"alignment\": \"center\", \"onMouseUp\": \"sun1.opacity = (sun1.opacity / 100) * 90;\" }}} ")
    val otherSampleJson = Json.parse("{\"menu\": { \"id\": \"file\", \"value\": \"File\", \"popup\": { \"menuitem\": [ {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"}, {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"}, {\"value\": \"Close\", \"onclick\": \"CloseDoc()\"} ] }}}")
    var cookieForLogin: Option[Cookie] = None

    def fakeLoggedInJsonRequest(method: String, uri: String, json: JsValue): FakeRequest[AnyContentAsJson] = {
      FakeRequest(method, uri).withJsonBody(json).withCookies(cookieForLogin.get)
    }

    "Produce empty html list" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get
      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must not contain ("<li>")
    }

    "Post without being logged in should be forbidden" in new WithApplication(fakeAppWithAdditionalConf) {
      val posted = route(FakeRequest(POST, "/api/example").withJsonBody(sampleJson)).get
      status(posted) must equalTo(FORBIDDEN)
    }

    "Login" in new WithApplication(fakeAppWithAdditionalConf) {
      val json = JsObject(Seq("username" -> JsString("test"), "password" -> JsString("testpass")))
      val login = route(FakeRequest(POST, "/login").withJsonBody(json)).get
      status(login) must equalTo(OK)
      cookieForLogin = cookies(login).get("PLAY_SESSION")
      cookieForLogin must beSome
    }

    "Post new json document" in new WithApplication(fakeAppWithAdditionalConf) {
      val posted = route(fakeLoggedInJsonRequest(POST, "/api/example", sampleJson)).get
      status(posted) must equalTo(OK)
    }

    "Get a document" in new WithApplication(fakeAppWithAdditionalConf) {
      val gotIt = route(FakeRequest(GET, "/api/example")).get
      status(gotIt) must equalTo(OK)
      contentAsJson(gotIt) must beEqualTo(sampleJson)
    }

    "Post same json with other name" in new WithApplication(fakeAppWithAdditionalConf) {
      val posted = route(fakeLoggedInJsonRequest(POST, "/api/other_example", sampleJson)).get
      status(posted) must equalTo(OK)
    }

    "Post other json with same name" in new WithApplication(fakeAppWithAdditionalConf) {
      val posted = route(fakeLoggedInJsonRequest(POST, "/api/example", otherSampleJson)).get
      status(posted) must equalTo(BAD_REQUEST)
      contentAsString(posted) must contain("duplicate name")
    }

    "Produce html list with both uri's" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain(">example</a>")
      contentAsString(home) must contain(">other_example</a>")
    }

    "Update without being logged in should be forbidden" in new WithApplication(fakeAppWithAdditionalConf) {
      val updateResult = route(FakeRequest(PUT, "/api/example").withJsonBody(sampleJson)).get
      status(updateResult) must equalTo(FORBIDDEN)
    }

    "Update a document" in new WithApplication(fakeAppWithAdditionalConf) {
      private val priorJson: String = contentAsString(route(FakeRequest(GET, "/api/example")).get)
      val updateResult = route(fakeLoggedInJsonRequest(PUT, "/api/example", otherSampleJson)).get
      status(updateResult) must equalTo(OK)
      private val currentJson: String = contentAsString(route(FakeRequest(GET, "/api/example")).get)
      priorJson must not equalTo currentJson
      currentJson must equalTo(otherSampleJson.toString())
    }


  }
}

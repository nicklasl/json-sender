import controllers.TestHelper._
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {


  "Regression" should {
    sequential

    "List empty html list" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must not contain ("<li>")
    }

    "Login" in new WithApplication(fakeAppWithAdditionalConf) {
      val json = JsObject(Seq("username" -> JsString("test"), "password" -> JsString("testpass")))
      val login = route(FakeRequest(POST, "/login").withJsonBody(json)).get
      status(login) must equalTo(OK)
      cookies(login).get("PLAY_SESSION") must beSome
    }

    /*
    "Post new json document" in new in new WithApplication(fakeAppWithAdditionalConf) {

    }
    */
  }
}

package controllers

import controllers.TestHelper._
import org.specs2.mutable.Specification
import play.api.test.WithApplication


class PasswordValidatorSpec extends Specification {

  "PasswordValidator" should {

    "validate from config" in new WithApplication(fakeAppWithAdditionalConf) {
      PasswordValidator.valid("test", "testpass") must beTrue
      PasswordValidator.valid("test", "testpass2") must beFalse
      PasswordValidator.valid("test2", "testpass") must beFalse
      PasswordValidator.valid("tes", "testpas") must beFalse
      PasswordValidator.valid("", "") must beFalse
    }

  }
}

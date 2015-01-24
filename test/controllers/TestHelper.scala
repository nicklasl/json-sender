package controllers

import play.api.test.FakeApplication

object TestHelper {

  val fakeAppWithAdditionalConf = FakeApplication(additionalConfiguration = Map("username" -> "test", "password" -> "testpass"))
}

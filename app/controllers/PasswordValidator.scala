package controllers

import play.api.Play

object PasswordValidator {

  def valid(username: String, password: String): Boolean = {
    val validUsernameOption = Play.current.configuration.getString("username")
    val validPasswordOption = Play.current.configuration.getString("password")

    (validUsernameOption, validPasswordOption) match {
      case (Some(validUsername), Some(validPassword)) => validUsername.equalsIgnoreCase(username) && validPassword.equalsIgnoreCase(password)
      case _ => false
    }
  }
}

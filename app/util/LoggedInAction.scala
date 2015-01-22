package util


import play.api.Logger
import play.api.mvc._

import scala.concurrent.Future

class UserRequest[A](val username: String, request: Request[A]) extends WrappedRequest[A](request)

object LoggedInAction extends ActionBuilder[UserRequest] {

  override def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[Result]): Future[Result] =
    request.session.get("user").map {
      username =>
        block(new UserRequest[A](username, request))
    }.getOrElse {
      Logger.info(s"User from IP ${request.remoteAddress} tried to access ${request.uri} without being logged in.")
      Future.successful(Results.Forbidden)
    }
}
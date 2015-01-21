package util

import play.api.mvc._

import scala.concurrent.Future

class UserRequest[A](val username: String, request: Request[A]) extends WrappedRequest[A](request)

object LoggedInAction extends ActionBuilder[UserRequest] {

  override def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[Result]): Future[Result] =
    request.session.get("username").map {
      username =>
       block(new UserRequest[A](username, request))
    }.getOrElse(throw new Exception())
}
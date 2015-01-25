package controllers

import play.api.Play.current
import play.api.db.DB
import play.api.mvc._
import play.api.{Logger, Play}
import play.mvc.Http.MimeTypes
import util.LoggedInAction

object Application extends Controller {

  val storage: StorageFacade = new StorageFacadeImpl()

  def index = Action {
    val urls = storage.keys
    Logger.debug(s"urls=$urls")
    Logger.debug(s"Got a db.default.url=${Play.configuration.getString("db.default.url")}")
    DB.withConnection {
      connection =>
        Logger.debug(s"Got a db connection: $connection")
    }
    Ok(views.html.index(urls))
  }


  def login = Action {
    implicit request =>
      request.body.asJson.map {
        json =>
          val maybeUsername: Option[String] = json.\("username").asOpt[String]
          val maybePassword: Option[String] = json.\("password").asOpt[String]
          (maybeUsername, maybePassword) match {
            case (Some(username), Some(password)) => {
              if (PasswordValidator.valid(username, password)) Ok.withSession(Session(Map("user" -> username)))
              else Forbidden
            }
            case _ => Forbidden
          }
      }.getOrElse(BadRequest)
  }

  def getDocument(docName: String) = Action {
    storage.get(docName).map { json =>
      Ok(json).as(MimeTypes.JSON)
    }.getOrElse(NotFound)
  }

  def addDocument(docName: String) = LoggedInAction {
    implicit request =>
      Logger.debug(s"addDocument as ${request.username}")
      if (storage.get(docName).isDefined) {
        BadRequest("duplicate name, use PUT to replace.")
      } else {
        request.body.asJson.map {
          json =>
            storage.put(docName, json)
            Ok
        }.getOrElse(BadRequest)
      }
  }

  def replaceDocument(docName: String) = LoggedInAction {
    implicit request =>
      Logger.debug(s"replaceDocument as ${request.username}")
      request.body.asJson.map {
        json =>
          storage.put(docName, json)
          Ok
      }.getOrElse(BadRequest)
  }

  def logout = LoggedInAction {
    implicit request =>
      Logger.debug(s"Logging out ${request.username}")
      Ok.withNewSession
  }

}
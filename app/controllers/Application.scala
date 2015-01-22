package controllers

import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.{Configuration, Logger, Play}
import play.mvc.Http.MimeTypes
import util.LoggedInAction

import scala.collection.mutable

object Application extends Controller {

  val memory: mutable.Map[String, JsValue] = new mutable.HashMap[String, JsValue]()

  def index = Action {
    val urls = memory.keys.toList
    Logger.debug(s"urls=$urls")
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
              if (valid(username, password)) Ok.withSession(Session(Map("user" -> username)))
              else Forbidden
            }
            case _ => Forbidden
          }
      }.getOrElse(BadRequest)
  }

  def getDocument(docName: String) = Action {
    memory.get(docName).map { json =>
      Ok(json).as(MimeTypes.JSON)
    }.getOrElse(NotFound)
  }

  def addDocument(docName: String) = LoggedInAction {
    implicit request =>
      Logger.debug(s"addDocument as ${request.username}")
      if (memory.get(docName).isDefined) {
        BadRequest("duplicate name, use PUT to replace.")
      } else {
        request.body.asJson.map {
          json =>
            memory.put(docName, json)
            Ok
        }.getOrElse(BadRequest)
      }
  }

  def replaceDocument(docName: String) = LoggedInAction {
    implicit request =>
      Logger.debug(s"replaceDocument as ${request.username}")
      request.body.asJson.map {
        json =>
          memory.put(docName, json)
          Ok
      }.getOrElse(BadRequest)
  }

  def logout = LoggedInAction {
    implicit request =>
      Logger.debug(s"Logging out ${request.username}")
      Ok.withNewSession
  }

  private def valid(username: String, password: String): Boolean = {
    val logins: Seq[Configuration] = Play.current.configuration.getConfigSeq("logins").get
    val option: Option[Configuration] = logins.find(entry => entry.getString("username").getOrElse("").equalsIgnoreCase(username))
    option.exists(conf => if (conf.getString("password").exists(p => p.equalsIgnoreCase(password))) {
      true
    } else false)
  }
}
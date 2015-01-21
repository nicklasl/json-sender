package controllers

import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.{Configuration, Play}

import scala.collection.mutable

object Application extends Controller {

  val memory: mutable.Map[String, JsValue] = new mutable.HashMap[String, JsValue]()

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }


  def login = Action {
    implicit request =>
      request.body.asJson.map {
        json =>
          val maybeUsername: Option[String] = json.\("username").asOpt[String]
          val maybePassword: Option[String] = json.\("password").asOpt[String]
          (maybeUsername, maybePassword) match {
            case (Some(username), Some(password)) => validate(username, password)
            case _ => Forbidden
          }
      }.getOrElse(BadRequest)
  }

  def getDocument(docName: String) = Action {
    memory.get(docName).map{ json =>
      Ok(json).as("application/json")
    }.getOrElse(NotFound)
  }

  def addDocument(docName: String) = Action {
    implicit request =>
      if (memory.get(docName).isDefined) {
        BadRequest("duplicate name, use put to replace.")
      } else {
        request.body.asJson.map {
          json =>
            memory.put(docName, json)
        }
        Ok
      }
  }


  private def validate(username: String, password: String): Result = {
    val logins: Seq[Configuration] = Play.current.configuration.getConfigSeq("logins").get
    val option: Option[Configuration] = logins.find(entry => entry.getString("username").getOrElse("").equalsIgnoreCase(username))
    option.map { conf => if (conf.getString("password").exists(p => p.equalsIgnoreCase(password))) {
      Ok.withSession(Session(Map("user"->username)))
    } else Forbidden
    }.getOrElse(Forbidden)
  }
}
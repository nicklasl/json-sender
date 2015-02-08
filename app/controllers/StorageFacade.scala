package controllers

import anorm._
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable

trait StorageFacade {

  def keys: List[String]

  def get(key: String): Option[JsValue]

  def put(key: String, value: JsValue): Option[JsValue]

}

class HashmapStorage extends StorageFacade {
  private val memory: mutable.Map[String, JsValue] = new mutable.HashMap[String, JsValue]()

  override def keys: List[String] = memory.keys.toList

  override def get(key: String): Option[JsValue] = memory.get(key)

  override def put(key: String, value: JsValue): Option[JsValue] = memory.put(key, value)
}

class DbStorage extends StorageFacade {


  override def keys: List[String] = DB.withConnection {
    implicit connection =>
      SQL("SELECT name FROM document").as(stringParser *)
  }

  override def get(key: String): Option[JsValue] = DB.withConnection {
    implicit connection =>
      val jsValues: List[JsValue] = SQL("SELECT doc FROM document WHERE name = {name}").on('name -> key).as(jsParser *)
      jsValues.headOption
  }

  override def put(key: String, value: JsValue): Option[JsValue] = DB.withConnection {
    implicit connection =>
      val pgObject = new org.postgresql.util.PGobject()
      pgObject.setType("json")
      pgObject.setValue(value.toString())
      val sql = SQL("insert into document(name, doc) values ({name}, {doc})")
        .on("name" -> key)
        .on("doc" -> anorm.Object(pgObject))
      sql.executeUpdate()
      get(key)
  }

  implicit def rowToJsValue: Column[JsValue] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case pgo: org.postgresql.util.PGobject => Right(Json.parse(pgo.getValue))
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" +
        value.asInstanceOf[AnyRef].getClass + " to JsValue for column " + qualified))
    }
  }

  private val jsParser = {
    anorm.SqlParser.get[JsValue]("doc") map {
      case doc => doc
    }
  }

  private val stringParser = {
    anorm.SqlParser.get[String]("name") map {
      case name => name
    }
  }

}

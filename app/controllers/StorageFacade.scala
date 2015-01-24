package controllers

import play.api.libs.json.JsValue

import scala.collection.mutable

trait StorageFacade {

  def keys: List[String]

  def get(key: String): Option[JsValue]

  def put(key: String, value: JsValue): Option[JsValue]

}

class StorageFacadeImpl extends StorageFacade {
  private val memory: mutable.Map[String, JsValue] = new mutable.HashMap[String, JsValue]()

  override def keys: List[String] = memory.keys.toList

  override def get(key: String): Option[JsValue] = memory.get(key)

  override def put(key: String, value: JsValue): Option[JsValue] = memory.put(key, value)

}

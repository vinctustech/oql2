package com.vinctus.oql

import com.vinctus.sjs_utils.{jsObject, toJS, toMap}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

class Mutation_JS_NodePG private[oql] (oql: OQL_NodePG_JS, entity: Entity) extends Mutation(oql, entity) {

  @JSExport("insert")
  def jsInsert(obj: js.Dictionary[js.Any]): js.Promise[js.Any] = insert(toMap(obj, false)) map toJS toJSPromise

  @JSExport("delete")
  def jsDelete(e: js.Any): js.Promise[Unit] = delete(
    if (jsObject(e)) e.asInstanceOf[js.Dictionary[String]](entity.pk.get.name) else e
  ) toJSPromise

  @JSExport("bulkDelete")
  def jsBulkDelete(es: js.Array[js.Any]): js.Promise[Unit] = bulkDelete(es.toList) toJSPromise

  @JSExport("link")
  def jsLink(e1: js.Any, resource: String, e2: js.Any): js.Promise[Unit] = {
    val id1: js.Any = if (jsObject(e1)) e1.asInstanceOf[js.Dictionary[String]](entity.pk.get.name) else e1
    val id2: js.Any = if (jsObject(e2)) e2.asInstanceOf[js.Dictionary[String]](entity.pk.get.name) else e2

    link(id1, resource, id2) toJSPromise
  }

  @JSExport("unlink")
  def jsUnlink(e1: js.Any, resource: String, e2: js.Any): js.Promise[Unit] = {
    val id1: Any = if (jsObject(e1)) e1.asInstanceOf[js.Dictionary[String]](entity.pk.get.name) else e1
    val id2: Any = if (jsObject(e2)) e2.asInstanceOf[js.Dictionary[String]](entity.pk.get.name) else e2

    unlink(id1, resource, id2) toJSPromise
  }

  @JSExport("update")
  def jsUpdate(e: js.Any, updates: js.Any): js.Promise[js.Any] =
    update(
      if (jsObject(e)) e.asInstanceOf[js.Dictionary[Any]](entity.pk.get.name) else e,
      toMap(updates, false)
    ) map toJS toJSPromise

  @JSExport("bulkUpdate")
  def jsBulkUpdate(updates: js.Array[js.Array[js.Any]]): js.Promise[Unit] =
    bulkUpdate(updates.toList map { (u: js.Array[js.Any]) =>
      (if (jsObject(u.head)) u.head.asInstanceOf[js.Dictionary[Any]](entity.pk.get.name) else u.head) -> toMap(
        u.tail.head
      )
    }).toJSPromise

}

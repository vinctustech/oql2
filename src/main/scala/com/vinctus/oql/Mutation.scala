package com.vinctus.oql

//import com.vinctus.mappable.{Mappable, map2cc}
import com.vinctus.sjs_utils.DynamicMap

import scala.collection.immutable.VectorMap
import scala.concurrent.Future
import scala.language.postfixOps
import scala.scalajs.js

class Mutation private[oql] (oql: AbstractOQL, entity: Entity)(implicit ec: scala.concurrent.ExecutionContext) {

//  def insert[T <: Product: Mappable](obj: T): Future[T] =
//    insert(implicitly[Mappable[T]].toMap(obj)) map map2cc[T] //implicitly[Mappable[T]].fromMap(m))

  def insert(obj: collection.Map[String, Any]): Future[VectorMap[String, Any]] =
//    // check if the object has a primary key
//    entity.pk foreach { pk =>
//      // object being inserted should not have a primary key property
//      if (obj.contains(pk.name)) // todo: && obj(pk.name).asInstanceOf[js.UndefOr[_]] != js.undefined
//        sys.error(s"insert(): object has a primary key property: $pk = ${obj(pk.name)}")
//    }

    // get sub-map of all column attributes
    val attrs =
      entity.attributes
        .filter {
          case (_, Attribute(name, column, pk, required, typ)) if typ.isColumnType => true
          case _                                                                   => false
        }

    // get sub-map of column attributes excluding primary key
    val attrsNoPK = entity.pk.fold(attrs)(attrs - _.name)

    // get key set of all column attributes that are required
    val attrsRequired =
      attrsNoPK.filter {
        case (_, Attribute(name, column, pk, true, typ)) if typ.isColumnType => true
        case _                                                               => false
      } keySet

    // get object's key set
    val keyset = obj.keySet

    // get key set of all attributes
    val allKeys = entity.attributes.keySet

    // check if object contains properties not defined for a entity
    if ((keyset diff allKeys).nonEmpty)
      sys.error(
        s"insert(): found properties not defined for entity '${entity.name}': ${(keyset diff allKeys) map (p => s"'$p'") mkString ", "}"
      )

    // check if object contains all required column attribute properties
    if (!(attrsRequired subsetOf keyset))
      sys.error(
        s"insert(): missing required properties for entity '${entity.name}': ${(attrsRequired diff keyset) map (p => s"'$p'") mkString ", "}"
      )

    val command = new StringBuilder

    // build list of values to insert
    val pairs =
      attrs flatMap {
        case (k, Attribute(name, column, pk, required, typ)) if typ.isDataType && obj.contains(k) =>
          List(k -> oql.render(obj(k), Option.when(typ == JSONType)(typ.asDatatype)))
        case (k, Attribute(_, _, _, _, ManyToOneType(mtoEntity))) if obj contains k =>
          val v = obj(k)

          List(
            k -> oql.render(
              if (v.isInstanceOf[Map[_, _]]) v.asInstanceOf[Map[String, Any]](mtoEntity.pk.get.name) else v
            )
          )
        case (k, _) => if (attrsRequired(k)) sys.error(s"attribute '$k' is required") else Nil
      }

    // check for empty insert
    if (pairs.isEmpty) sys.error("empty insert")

    val (keys, values) = pairs.unzip

    // transform list of keys into un-aliased column names
    val columns = keys map (k => attrs(k).column)

    // build insert command
    command append s"INSERT INTO ${entity.table} (${columns mkString ", "}) VALUES\n"
    command append s"  (${values mkString ", "})\n"
    entity.pk foreach (pk => command append s"  RETURNING ${pk.column}\n")
    oql.show(command.toString)

    // execute insert command (to get a future)
    oql.connect.command(command.toString) map { rs =>
      if (!rs.next)
        sys.error("insert: empty result set")

      entity.pk match {
        case None     => obj to VectorMap
        case Some(pk) => (VectorMap(pk.name -> rs.get(0).value) ++ obj) to VectorMap
      }
    }
  end insert

  def delete(id: Any): Future[Unit] = {
    val command = new StringBuilder

    // build delete command
    command append s"DELETE FROM ${entity.table}\n"
    command append s"  WHERE ${entity.pk.get.column} = ${oql.render(id)}\n"
    oql.show(command.toString)

    // execute update command (to get a future)
    oql.connect.command(command.toString) map (_ => ())
  }

  def bulkDelete(ids: List[Any]): Future[Unit] =
    val command = new StringBuilder

    // build delete command
    command append s"DELETE FROM ${entity.table}\n"
    command append s"  WHERE ${entity.pk.get.column} IN (${ids map (id => oql.render(id)) mkString ", "})\n"
    oql.show(command.toString)

    // execute update command (to get a future)
    oql.connect.command(command.toString) map (_ => ())
  end bulkDelete

  def link(id1: Any, attribute: String, id2: Any): Future[Unit] =
    entity.attributes get attribute match {
      case Some(Attribute(name, column, pk, required, ManyToManyType(mtmEntity, link, self, target))) =>
        new Mutation(oql, link).insert(Map(self.name -> id1, target.name -> id2)) map (_ => ())
      case Some(_) => sys.error(s"attribute '$attribute' is not many-to-many")
      case None    => sys.error(s"attribute '$attribute' does not exist on entity '${entity.name}'")
    }

  def unlink(id1: Any, attribute: String, id2: Any): Future[Unit] =
    entity.attributes get attribute match {
      case Some(Attribute(name, column, pk, required, ManyToManyType(mtmEntity, link, self, target))) =>
        val command = new StringBuilder

        // build delete command
        command append s"DELETE FROM ${link.table}\n"
        command append s"  WHERE ${self.column} = ${oql.render(id1)} AND ${target.column} = ${oql.render(id2)}\n"
        oql.show(command.toString)

        // execute update command (to get a future)
        oql.connect.command(command.toString) map (_ => ())
      case Some(_) => sys.error(s"attribute '$attribute' is not many-to-many")
      case None    => sys.error(s"attribute '$attribute' does not exist on entity '${entity.name}'")
    }

  def update(id: Any, updates: collection.Map[String, Any]): Future[VectorMap[String, Any]] =
    // check if updates has a primary key
    entity.pk foreach (pk =>
      // object being updated should not have it's primary key changed
      if (updates.contains(pk.name))
        sys.error(s"update: primary key ('$pk') value cannot be changed: $id")
    )

    // get sub-map of all column attributes
    val attrs =
      entity.attributes
        .filter {
          case (_, Attribute(name, column, pk, required, typ)) if typ.isColumnType => true
          case _                                                                   => false
        }

    // get sub-map of column attributes excluding primary key
    val attrsNoPK = entity.pk.fold(attrs)(attrs - _.name)

    // get key set of column attributes excluding primary key
    val attrsNoPKKeys = attrsNoPK.keySet

    // get updates key set
    val keyset = updates.keySet

    // check if object contains extrinsic attributes
    if ((keyset diff attrsNoPKKeys).nonEmpty)
      sys.error(
        s"extrinsic properties not found in entity '${entity.name}': ${(keyset diff attrsNoPKKeys) map (p => s"'$p'") mkString ", "}"
      )

    // removed properties with a value of undefined
    val updatesWithoutUndefined = updates filterNot { case (_, v) => v == js.undefined }

    // check if updates is empty
    if (updatesWithoutUndefined.isEmpty)
      sys.error(s"update: empty update: $id")

    // build list of attributes to update
    val pairs =
      updatesWithoutUndefined map { case (k, v) =>
        val v1 =
          if (v.isInstanceOf[Map[_, _]])
            entity.attributes(k) match {
              case Attribute(_, _, _, _, ManyToOneType(mtoEntity)) =>
                v.asInstanceOf[Map[String, Any]](mtoEntity.pk.get.name)
              case _ => sys.error(s"attribute '$k' of entity '${entity.name}' is not an entity attribute")
            }
          else v
        val attr = attrs(k)

        attr.column -> oql.render(v1, Option.when(attr.typ == JSONType)(attr.typ.asDatatype))
      }

    val command = new StringBuilder

    // build update command
    command append s"UPDATE ${entity.table}\n"
    command append s"  SET ${pairs map { case (k, v) => s"$k = $v" } mkString ", "}\n"
    command append s"  WHERE ${entity.pk.get.column} = ${oql.render(id)}\n"
    oql.show(command.toString)

    // execute update command (to get a future)
    oql.connect.command(command.toString) map { _ =>
      entity.pk match {
        case None     => updatesWithoutUndefined to VectorMap
        case Some(pk) => (VectorMap(pk.name -> id) ++ updatesWithoutUndefined) to VectorMap
      }
    }
  end update

  def bulkUpdate(updates: List[(Any, collection.Map[String, Any])]): Future[Unit] = {
    if (updates.isEmpty)
      sys.error(s"update: empty updates list")

    // get updates key set
    val keyset = updates.head._2.keySet

    // check if keys in update objects are all the same
    for ((_, u) <- updates drop 1)
      if (u.keySet != keyset)
        sys.error(s"update: key set mismatch: ${u.mkString("{", ", ", "}")}")

    // check if updates is empty
    if (updates.head._2.isEmpty)
      sys.error(s"update: empty updates")

    // check if updates has a primary key
    entity.pk foreach (pk =>
      // object being updated should not have it's primary key changed
      if (updates.head._2.contains(pk.name))
        sys.error(s"update: primary key ('$pk') value cannot be changed")
    )

    // get sub-map of all column attributes
    val attrs =
      entity.attributes
        .filter {
          case (_, Attribute(name, column, pk, required, typ)) if typ.isColumnType => true
          case _                                                                   => false
        }

    // get sub-map of column attributes excluding primary key
    val attrsNoPK = entity.pk.fold(attrs)(attrs - _.name)

    // get key set of column attributes excluding primary key
    val attrsNoPKKeys = attrsNoPK.keySet

    // check if object contains extrinsic attributes
    if ((keyset diff attrsNoPKKeys).nonEmpty)
      sys.error(
        s"extrinsic properties not found in entity '${entity.name}': ${(keyset diff attrsNoPKKeys) map (p => s"'$p'") mkString ", "}"
      )

    val command = new StringBuilder

    // build update command
    val keys = updates.head._2.keys
    val columns = keys.map(k => attrsNoPK(k).column)

    command append s"UPDATE  ${entity.table}\n"
    command append s"  SET   ${columns map { k =>
        s"$k = __data__.$k"
      } mkString ", "}\n"
    command append s"  FROM  (VALUES ${updates map { case (id, update) =>
        s"(${oql.render(id, Some(entity.pk.get.typ.asDatatype))}, ${keys map (update andThen (x => oql.render(x))) mkString ", "})"
      } mkString ", "}) AS __data__ (${entity.pk.get.column}, ${columns mkString ", "})\n"
    command append s"  WHERE ${entity.table}.${entity.pk.get.column} = __data__.${entity.pk.get.column}\n"
    oql.show(command.toString)

    // execute update command (to get a future)
    oql.connect.command(command.toString) map (_ => ())
  }

}

/*
UPDATE  workflows
  SET   kioskEnabled = __data__.kioskEnabled
  FROM  (VALUES ('3d37a404-5433-4bd7-bbd7-88d01b3e5d3e'::UUID, true), ('63871ef9-bbda-498a-93c7-02d5418031c9'::UUID, true), ('f851b568-f07d-4128-b143-e7b29cf03611'::UUID, false), ('1165d4c0-7221-4522-a293-3d1c0c86c951'::UUID, false), ('bd38cb89-b3b5-43aa-99b9-2971948d11cf'::UUID, false), ('af2dac35-0c5c-4678-84ad-085620dd684b'::UUID, false), ('f6d1ba00-62dd-4b2b-ae50-6c04899d549b'::UUID, false)) AS __data__ (id, kioskEnabled)
  WHERE workflows.id = __data__.id
 */

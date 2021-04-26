package com.vinctus.oql2

import scalajs.js

class PG_Node(val host: String,
              val port: Int,
              val database: String,
              val user: String,
              val password: String,
              val ssl: Boolean,
              val idleTimeoutMillis: Int,
              val max: Int)
    extends PGDataSource {

  val name: String = "PostgreSQL (node-pg)"

  def timestamp(t: String): Any = new js.Date(t)

  def uuid(id: String): Any = id

  def connect: OQLConnection = new PGNodeConnection(this)

  val platformSpecific: PartialFunction[Any, String] = {
    case d: js.Date => s""""${d.toISOString()}""""
  }

}

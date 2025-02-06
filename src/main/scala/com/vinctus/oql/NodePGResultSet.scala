package com.vinctus.oql

import typings.pg.mod.QueryArrayResult

import scala.scalajs.js

class NodePGResultSet(rs: QueryArrayResult[js.Array[js.Any]]) extends OQLResultSet with JSONResultSet {
  private var first = false
  private var ridx: Int = _
  private var row: js.Array[js.Any] = _

  def next: Boolean = {
    if (first)
      ridx += 1
    else {
      first = true
      ridx = 0
    }

    if rs.rowCount == null then sys.error("QueryArrayResult.rowCount is null")
    else if (ridx >= rs.rowCount.asInstanceOf[Double])
      false
    else {
      row = rs.rows(ridx)
      true
    }
  }

  def get(idx: Int): NodePGResultSetValue = NodePGResultSetValue(row(idx))

  def getString(idx: Int): String = row(idx).toString

}

case class NodePGResultSetValue(value: Any) extends OQLResultSetValue

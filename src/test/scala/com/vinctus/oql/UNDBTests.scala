package com.vinctus.oql

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import typings.pg.mod.types
import typings.pgTypes.mod.TypeId

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

class UNDBTests extends AsyncFreeSpec with Matchers {

  g.require("source-map-support").install()
  types.setTypeParser(114.asInstanceOf[TypeId], (s: String) => s) // tell node-pg not to parse JSON
  types.setTypeParser(1114.asInstanceOf[TypeId], (s: String) => new js.Date(s"$s+00:00"))

  implicit override def executionContext = scala.concurrent.ExecutionContext.Implicits.global

  val db =
    new OQL_NodePG(g.require("fs").readFileSync("test/un.dm").toString, "localhost", 5432, "postgres", "postgres", "docker", false, 1000, 5)

  def test(oql: String, parameters: (String, Any)*): Future[String] = db.json(oql, parameters: _*)

  "simple one-to-one query" in {
    test("country { * rep { name } }") map { result =>
      result shouldBe
        """
          |[
          |  {
          |    "id": 1,
          |    "name": "Nigeria",
          |    "rep": {
          |      "name": "Abubakar Ahmad"
          |    }
          |  },
          |  {
          |    "id": 2,
          |    "name": "Ghana",
          |    "rep": {
          |      "name": "Joseph Nkrumah"
          |    }
          |  },
          |  {
          |    "id": 3,
          |    "name": "South Africa",
          |    "rep": {
          |      "name": "Lauren Zuma"
          |    }
          |  },
          |  {
          |    "id": 4,
          |    "name": "Republic of China (Taiwan)",
          |    "rep": null
          |  }
          |]
          |""".trim.stripMargin
    }
  }

}

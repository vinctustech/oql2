package com.vinctus.oql

import typings.node.global.console
import typings.pg.mod.types
import typings.pgTypes.mod.TypeId

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import js.Dynamic.global as g
import scala.util.{Failure, Success}

import io.github.edadma.rdb

@main def run(): Unit = {

  def readFile(f: String) = g.require("fs").readFileSync(f).toString

  g.require("source-map-support").install()
  types.setTypeParser(114.asInstanceOf[TypeId], (s: String) => s) // tell node-pg not to parse JSON
  types.setTypeParser(1114.asInstanceOf[TypeId], (s: String) => new js.Date(s"$s+00:00"))

//  val db = new OQL_RDB_ScalaJS(
//    """
//      |entity a {
//      | *id: int
//      |  t: timestamp
//      |}
//      |""".trim.stripMargin
//  )

//  val db = new OQL_RDB_ScalaJS(
//    """
//      |entity employee {
//      | *id: bigint
//      |  firstName: text
//      |  lastName: text
//      |  manager: employee
//      |  job: job
//      |  department: department
//      |  previous: department
//      |}
//      |
//      |entity job {
//      | *id: bigint
//      |  jobTitle: text
//      |  employees: [employee]
//      |  departments: [department] (employee)
//      |}
//      |
//      |entity department {
//      | *id: bigint
//      |  departmentName: text
//      |  employees: [employee]
//      |  jobs: [job] (employee)
//      |}
//      |""".trim.stripMargin
//  )

  //  val db: OQL_NodePG_JS =
//    new OQL_NodePG_JS(
//      """
//        |entity test {
//        | *id: integer
//        |  a: integer
//        |  b: integer
//        |}
//        |""".stripMargin,
//      "localhost",
//      5432,
//      "postgres",
//      "postgres",
//      "docker",
//      false,
//      1000,
//      5
//    )
//
//  db.showQuery()
//
//  db.entity("test").jsUpdate(1, js.Dynamic.literal(ax = js.undefined)).toFuture.onComplete {
//    case Failure(exception) => exception.printStackTrace()
//    case Success(u) =>
//      db.jsQueryMany("test").toFuture.onComplete {
//        case Failure(exception) => exception.printStackTrace()
//        case Success(r) =>
//          println(stringify(u))
//          println(stringify(r))
//      }
//  }

//  rdb.executeSQL(
//    """
//      |CREATE TABLE "a" (
//      |  "id" INTEGER PRIMARY KEY,
//      |  "t" TIMESTAMP
//      |);
//      |INSERT INTO "a" ("id", "t") VALUES
//      |  (3, '2021-04-21T06:30:00.000Z'),
//      |  (4, '2022-04-21T06:30:00.000Z');
//      |""".stripMargin
//  )(
//    db.connect
//      .asInstanceOf[RDBConnection]
//      .db
//  )

//  rdb.executeSQL(
//    """
//      |CREATE TABLE "job" (
//      |  "id" INTEGER PRIMARY KEY,
//      |  "jobTitle" TEXT
//      |);
//      |CREATE TABLE "department" (
//      |  "id" INTEGER AUTO PRIMARY KEY,
//      |  "departmentName" TEXT
//      |);
//      |CREATE TABLE "employee" (
//      |  "id" INTEGER PRIMARY KEY,
//      |  "firstName" TEXT,
//      |  "lastName" TEXT,
//      |  "manager" INTEGER,
//      |  "job" INTEGER,
//      |  "department" INTEGER,
//      |  "previous" INTEGER
//      |);
//      |ALTER TABLE "employee" ADD FOREIGN KEY ("manager") REFERENCES "employee";
//      |ALTER TABLE "employee" ADD FOREIGN KEY ("job") REFERENCES "job";
//      |ALTER TABLE "employee" ADD FOREIGN KEY ("department") REFERENCES "department";
//      |ALTER TABLE "employee" ADD FOREIGN KEY ("previous") REFERENCES "department";
//      |INSERT INTO "job" ("id", "jobTitle") VALUES
//      |  (4, 'President'),
//      |  (5, 'Administration Vice President'),
//      |  (9, 'Programmer'),
//      |  (20, 'IT Manager');
//      |INSERT INTO "department" ("id", "departmentName") VALUES
//      |  (9, 'Executive'),
//      |  (5, 'Research'),
//      |  (6, 'IT');
//      |INSERT INTO "employee" ("id", "firstName", "lastName", "manager", "job", "department", "previous") VALUES
//      |  (100, 'Steven', 'King', NULL, 4, 9, NULL),
//      |  (101, 'Neena', 'Kochhar', 100, 5, 9, NULL),
//      |  (102, 'Lex', 'De Haan', 100, 5, 9, NULL),
//      |  (103, 'Alexander', 'Hunold', 102, 20, 6, NULL),
//      |  (104, 'Bruce', 'Ernst', 103, 9, 6, 5);
//      |""".stripMargin
//  )(
//    db.connect
//      .asInstanceOf[RDBConnection]
//      .db
//  )

//  val db = new OQL_RDB_ScalaJS(
//    """
//      |enum color { red blue black white gray silver green yellow }
//      |
//      |entity car (cars) {
//      |  make: text
//      |  color: color
//      |}
//      |""".stripMargin
//  )

//  db.showQuery()
//
//  (for
//    //    _ <- db.create
////    u <- db.entity("employee").update(104, Map("firstName" -> js.undefined, "lastName" -> "Lee"))
////    i <- { db.entity("department").insert(Map("id" -> 123, "departmentName" -> "RnR")) }
//    r <- { db.showQuery(); db.queryMany("a [t::timestamp = '2021-04-21T06:30:00.000Z'::timestamp]") } // a {* bs}
//  yield (r))
//    .onComplete {
//      case Failure(exception) => exception.printStackTrace()
//      case Success((r))       =>
////        println(u)
//        println(r)
//    }

//    new OQL_NodePG_ScalaJS(g.require("fs").readFileSync("test/json.dm").toString, "localhost", 5432, "postgres", "postgres", "docker", false, 1000, 5)
  // new OQL_NodePG_JS(g.require("fs").readFileSync("test/json.dm").toString, "localhost", 5432, "postgres", "postgres", "docker", false, 1000, 5)
//    new OQL_NodePG_ScalaJS(g.require("fs").readFileSync("test/book.dm").toString, "localhost", 5432, "postgres", "postgres", "docker", false, 1000, 5)
//    new OQL_NodePG_JS(g.require("fs").readFileSync("test/event.dm").toString, "localhost", 5432, "postgres", "postgres", "docker", false, 1000, 5)
//  new OQL_NodePG(g.require("fs").readFileSync("test/accounts.dm").toString, "localhost", 5432, "postgres", "postgres", "docker", false, 1000, 5)
//    println(await(db.entity("info").insert(Map("main" -> 1, "data" -> List(1, Map("asdf" -> 345), 3)))))
//    println(await(db.entity("info").update(3, Map("data" -> List(5, 6, 7)))))
//      db.showQuery()
//    println(await(db.queryMany("main {* infos}")))
//    println(stringify(await(db.jsQueryMany("main {* infos}").toFuture)))
//    println(await(db.queryMany("info")))
//    println(stringify(await(db.jsQueryMany("info").toFuture)))
//    println(await(db.queryMany("job { jobTitle employees { firstName } }")))
//    println(await(db.queryMany("author {* books: books {count(*)}}")))
  // println(await(db.queryMany("book {* author: author.name} <id>")))
//    println(await(db.json("car")))
//    println(await(db.queryMany("""account""", "account", 2)))
//    println(await(db.queryMany("""vehicle""", "account", 2)))
//    println(await(db.queryMany("""store""", "account", 2)))
//    println(await(db.jsQueryMany("attendee { * events <when> } <name>").toFuture map (v => JSON(v, db.ds.platformSpecific, 2, true))))

//  def stringify(x: Any) = js.JSON.stringify(x.asInstanceOf[js.Any], null.asInstanceOf[js.Array[js.Any]], 2)
//
//}

//package com.vinctus.oql
//
//import com.vinctus.sjs_utils.Mappable
//import typings.node.global.console
//
//import *$1*
//import scala.async.Async.{async, await}
//
//import scala.scalajs.js
//import js.Dynamic.{global => g}
//
//object Main extends App {
//
//  g.require("source-map-support").install()
//
//  val dm: String =
//    """
//      |entity t {
//      | *id: int
//      |  s: text
//      |}
//      |""".stripMargin
//  val data =
//    """
//      |t
//      | id: integer, pk, auto   s: text
//      |""".stripMargin
//  val db = new OQL_RDB(dm, data)
//
//  async {
//    db.showQuery()
//    await(db.entity("t").insert(Map("s" -> "asdf'zxcv")))
//    db.showQuery()
//    println(await(db.queryMany("""t""")))
//  }
//
//}

//package com.vinctus.oql
//
//import com.vinctus.sjs_utils.Mappable
//import typings.node.global.console
//
//import *$1*
//import scala.scalajs.js
//import js.Dynamic.{global => g}
//import scala.util.{Failure, Success}
//import scala.async.Async.{async, await}
//
//object Main extends App {
//
//  g.require("source-map-support").install()
//
//  val dm: String =
//    """
//        |entity t {
//        | *id: int
//        |  n: int
//        |}
//        |""".stripMargin
//  val db = new OQL_NodePG_JS(dm, "localhost", 5432, "postgres", "postgres", "docker", false, 1000, 5)
//
//  db.showQuery()
//
//  async {
//    console.log(await(db.jsQueryMany("""t { average: avg(n) }""").toFuture))
//  }
//
//}

  val dm: String =
    """
      |entity t {
      | *id: int
      |  a: json
      |}
      |""".stripMargin
  val db = new OQL_NodePG_JS(dm, "localhost", 5432, "postgres", "postgres", "docker", false, 1000, 5)

  db.showQuery()
//  db.entity("t").jsInsert(js.Dictionary("s" -> "asdf'zxcv")).toFuture onComplete {
//    case Success(value) =>
//      console.log(value)
//
//      val id = value.asInstanceOf[js.Dictionary[Int]]("id")

  db.jsQueryMany("t").toFuture onComplete {
    case Success(value) =>
      console.log("result: ", value)

//          db.showQuery()
//          db.entity("t").jsUpdate(id, js.Dictionary("s" -> "this is\nanother test")).toFuture onComplete {
//            case Success(value) =>
//              console.log(value)
//
//              db.jsqueryMany("t").toFuture onComplete {
//                case Success(value)     => console.log(value)
//                case Failure(exception) => exception.printStackTrace()
//              }
//            case Failure(exception) => exception.printStackTrace()
//          }
    case Failure(exception) => exception.printStackTrace()
  }
//    case Failure(exception) => exception.printStackTrace()
//  }

}

// todo: RDB insert without primary key value doesn't work
// todo: error check for query type projects that are really datatype attributes: make sure there's no select, order, ...
// todo: add a unit test that has a deep (more than one) many-to-one reference in the select condition
// todo: unit test: "author { name } [EXISTS (books [author.name = 'Charles Dickens'])]" ~~> "Charles Dickens"
// todo: unit test: "book.dm { author ref: &author }" (reference)

/*

HashMap(
  vehicle ->
    Map(Entity(account,account) ->
      List(
        AttributeOQLExpression(List(Ident(driver,null), Ident(account,null), Ident(id,null)),List((Entity(user,user),Attribute(driver,driver,false,false,ManyToOneType(Entity(user,user)))), (Entity(account,account),Attribute(account,account,false,true,ManyToOneType(Entity(account,account)))), (Entity(account,account),Attribute(id,id,true,false,IntegerType)))), AttributeOQLExpression(List(Ident(store,null), Ident(account,null), Ident(id,null)),List((Entity(store,store),Attribute(store,store,false,true,ManyToOneType(Entity(store,store)))), (Entity(account,account),Attribute(account,account,false,true,ManyToOneType(Entity(account,account)))), (Entity(account,account),Attribute(id,id,true,false,IntegerType)))))),
  trip ->
    Map(Entity(account,account) ->
      List(
        AttributeOQLExpression(List(Ident(customer,null), Ident(store,null), Ident(account,null), Ident(id,null)),List((Entity(customer,customer),Attribute(customer,customer,false,false,ManyToOneType(Entity(customer,customer)))), (Entity(store,store),Attribute(store,store,false,true,ManyToOneType(Entity(store,store)))), (Entity(account,account),Attribute(account,account,false,true,ManyToOneType(Entity(account,account)))), (Entity(account,account),Attribute(id,id,true,false,IntegerType)))),
        AttributeOQLExpression(List(Ident(store,null), Ident(account,null), Ident(id,null)),List((Entity(store,store),Attribute(store,store,false,true,ManyToOneType(Entity(store,store)))), (Entity(account,account),Attribute(account,account,false,true,ManyToOneType(Entity(account,account)))), (Entity(account,account),Attribute(id,id,true,false,IntegerType)))), AttributeOQLExpression(List(Ident(vehicle,null), Ident(driver,null), Ident(account,null), Ident(id,null)),List((Entity(vehicle,vehicle),Attribute(vehicle,vehicle,false,false,ManyToOneType(Entity(vehicle,vehicle)))), (Entity(user,user),Attribute(driver,driver,false,false,ManyToOneType(Entity(user,user)))), (Entity(account,account),Attribute(account,account,false,true,ManyToOneType(Entity(account,account)))), (Entity(account,account),Attribute(id,id,true,false,IntegerType)))),
        AttributeOQLExpression(List(Ident(vehicle,null), Ident(store,null), Ident(account,null), Ident(id,null)),List((Entity(vehicle,vehicle),Attribute(vehicle,vehicle,false,false,ManyToOneType(Entity(vehicle,vehicle)))), (Entity(store,store),Attribute(store,store,false,true,ManyToOneType(Entity(store,store)))), (Entity(account,account),Attribute(account,account,false,true,ManyToOneType(Entity(account,account)))), (Entity(account,account),Attribute(id,id,true,false,IntegerType)))))),
  store ->
    Map(Entity(account,account) ->
      List(
        AttributeOQLExpression(List(Ident(account,null), Ident(id,null)),List((Entity(account,account),Attribute(account,account,false,true,ManyToOneType(Entity(account,account)))), (Entity(account,account),Attribute(id,id,true,false,IntegerType)))))),
  customer ->
    Map(Entity(account,account) ->
      List(
        AttributeOQLExpression(List(Ident(store,null), Ident(account,null), Ident(id,null)),List((Entity(store,store),Attribute(store,store,false,true,ManyToOneType(Entity(store,store)))), (Entity(account,account),Attribute(account,account,false,true,ManyToOneType(Entity(account,account)))), (Entity(account,account),Attribute(id,id,true,false,IntegerType)))))),
  users_stores ->
    Map(Entity(account,account) ->
      List(
        AttributeOQLExpression(List(Ident(user,null), Ident(account,null), Ident(id,null)),List((Entity(user,user),Attribute(user,user,false,false,ManyToOneType(Entity(user,user)))), (Entity(account,account),Attribute(account,account,false,true,ManyToOneType(Entity(account,account)))), (Entity(account,account),Attribute(id,id,true,false,IntegerType)))),
        AttributeOQLExpression(List(Ident(store,null), Ident(account,null), Ident(id,null)),List((Entity(store,store),Attribute(store,store,false,false,ManyToOneType(Entity(store,store)))), (Entity(account,account),Attribute(account,account,false,true,ManyToOneType(Entity(account,account)))), (Entity(account,account),Attribute(id,id,true,false,IntegerType)))))),
  account ->
    Map(Entity(account,account) ->
      List(
        AttributeOQLExpression(List(Ident(id,null)),List((Entity(account,account),Attribute(id,id,true,false,IntegerType)))))),
  user ->
    Map(Entity(account,account) ->
      List(
        AttributeOQLExpression(List(Ident(account,null), Ident(id,null)),List((Entity(account,account),Attribute(account,account,false,true,ManyToOneType(Entity(account,account)))), (Entity(account,account),Attribute(id,id,true,false,IntegerType)))))))

 */

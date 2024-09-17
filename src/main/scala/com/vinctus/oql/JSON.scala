package com.vinctus.oql

import java.time.{Instant, ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter
import scala.annotation.tailrec
import scala.collection.immutable.{ArraySeq, ListMap}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.language.postfixOps
import scala.scalajs.js

object JSON {

  private val EOI = '\uE000'
  private val ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  private var idx: Int = 0
  private var json: String = ""

  def next: Char =
    if (idx > json.length) sys.error("past end of JSON string")
    else if (idx == json.length) EOI
    else json.charAt(idx)

  def ch: Char = {
    val c = next

    advance()
    c
  }

  def advance(): Unit = idx += 1

  def prev: Char = json.charAt(idx - 1)

  def space(): Unit = while (next.isWhitespace) advance()

  def chmatch(c: Char): Unit =
    if (ch != c)
      error(
        if (c == EOI) "expected end of input" else s"expected '$c', but found '$prev':\n$json\n${(" " * idx) :+ '^'}"
      )

  def delim(c: Char): Unit = {
    chmatch(c)
    space()
  }

  def readArray: IndexedSeq[Any] = {
    val buf = new ArrayBuffer[Any]

    delim('[')

    @tailrec
    def elem(): Unit = {
      buf += readValue

      if (next == ',') {
        advance()
        space()
        elem()
      }
    }

    if (next != ']')
      elem()

    delim(']')
    buf to ArraySeq
  }

  def readObject: ListMap[String, Any] = {
    val buf = new ListBuffer[(String, Any)]

    delim('{')

    @tailrec
    def elem(): Unit = {
      val key = readString

      delim(':')

      buf += ((key, readValue))

      if (next == ',') {
        advance()
        space()
        elem()
      }
    }

    if (next != '}')
      elem()

    delim('}')
    buf to ListMap
  }

  def error(str: String) = sys.error(str)

  def readValue: Any =
    next match {
      case `EOI`                      => error("unexpected end of JSON string")
      case '['                        => readArray
      case '{'                        => readObject
      case '"'                        => readString
      case d if d.isDigit || d == '-' => readNumber
      case 'n'                        => literal("null", null)
      case 't'                        => literal("true", true)
      case 'f'                        => literal("false", false)
    }

  def readString: String = {
    val buf = new StringBuilder

    chmatch('"')

    @tailrec
    def content(): Unit =
      ch match {
        case '\\' =>
          buf +=
            (ch match {
              case '\\' => '\\'
              case '"'  => '"'
              case '/'  => '/'
              case 'b'  => '\b'
              case 'f'  => '\f'
              case 'n'  => '\n'
              case 'r'  => '\r'
              case 't'  => '\t'
              case 'u'  => (hex(ch) << 12 | hex(ch) << 8 | hex(ch) << 4 | hex(ch)).toChar
            })
          content()
        case '"' =>
        case c =>
          buf += c
          content()
      }

    content()
    space()

    buf.toString
  }

  def readNumber: String = {
    val buf = new StringBuilder
    var c: Char = next

    while (c.isDigit || c == '.' || c == '-' || c == 'e' || c == 'E') {
      buf += c
      advance()
      c = next
    }

    space()
    buf.toString
  }

  def literal(s: String, v: Any): Any = {
    for (i <- 0 until s.length) {
      if (next == EOI) error(s"unexpected end of JSON string: trying to match '$s'")
      else if (ch != s.charAt(i)) error(s"mismatch")
    }

    space()
    v
  }

  def readArray(s: String): IndexedSeq[Any] = {
    idx = 0
    json = s
    space()

    val v = readArray

    chmatch(EOI)
    v
  }

  def readValue(s: String): Any = {
    idx = 0
    json = s
    space()

    val v = readValue

    chmatch(EOI)
    v
  }

  def apply(
      value: Any,
      platformSpecific: PartialFunction[Any, String],
      tab: Int = 2,
      format: Boolean = false
  ): String = {
    val buf = new StringBuilder
    var level = 0

    def ln(): Unit =
      if (format)
        buf += '\n'

    def indent(): Unit = {
      ln()
      level += tab
      margin()
    }

    def dedent(): Unit = {
      ln()
      level -= tab
      margin()
    }

    def margin(): Unit =
      if (format)
        buf ++= " " * level

    def aggregate[T](open: Char, seq: collection.Seq[T], close: Char)(render: T => Unit): Unit = {
      buf += open
      indent()

      val it = seq.iterator

      if (it.nonEmpty)
        render(it.next())

      while (it.hasNext) {
        buf += ','
        ln()
        margin()
        render(it.next())
      }

      dedent()
      buf += close
    }

    def jsonValue(value: Any): Unit =
      value match {
        case p if platformSpecific isDefinedAt p                     => buf ++= platformSpecific(p)
        case _: Number | _: java.math.BigDecimal | _: Boolean | null => buf ++= String.valueOf(value)
        case m: collection.Map[_, _]           => jsonObject(m.toSeq.asInstanceOf[Seq[(String, Any)]])
        case s: collection.Seq[_] if s.isEmpty => buf ++= "[]"
        case s: collection.Seq[_]              => aggregate('[', s, ']')(jsonValue)
        case a: Array[_]                       => jsonValue(a.toList)
        case a: js.Array[_]                    => jsonValue(a.toList)
        case p: Product                        => jsonObject(p.productElementNames zip p.productIterator toList)
        case t: Instant                        => buf ++= '"' +: ISO.format(t.atOffset(ZoneOffset.UTC)) :+ '"'
//        case t: Timestamp                      => jsonValue(t.toInstant.toString) //  | _: Instant  _: java.util.UUID
        case _: String =>
          buf += '"'
          buf ++=
            List("\\" -> "\\\\", "\"" -> "\\\"", "\t" -> "\\t", "\n" -> "\\n", "\r" -> "\\r").foldLeft(value.toString) {
              case (acc, (c, r)) => acc.replace(c, r)
            }
          buf += '"'
        case _: js.Object => jsonObject(value.asInstanceOf[js.Dictionary[Any]].toList)
        case _ =>
          js.typeOf(value) match {
            case "bigint" => buf ++= String.valueOf(value)
            case _        => buf ++= '"' +: String.valueOf(value) :+ '"'
          }
      }

    def jsonObject(pairs: Seq[(String, Any)]): Unit =
      if (pairs.isEmpty)
        buf ++= "{}"
      else
        aggregate('{', pairs, '}') { case (k, v) =>
          jsonValue(k)
          buf ++= (if (format) ": " else ":")
          jsonValue(v)
        }

    jsonValue(value)
    ln()
    buf.toString
  }

}

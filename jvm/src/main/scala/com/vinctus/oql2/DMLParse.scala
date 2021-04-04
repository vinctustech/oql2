package com.vinctus.oql2

import com.vinctus.oql2.DMLParser.{AliasContext, AttributeNameContext}
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import org.antlr.v4.runtime.{
  ANTLRErrorListener,
  CharStreams,
  CommonTokenStream,
  ConsoleErrorListener,
  Parser,
  RecognitionException,
  Recognizer
}

import java.util

object DMLParse {

  def apply(input: String): Option[DMLModel] = {
    val charStream = CharStreams.fromString(input)
    val lexer = new DMLLexer(charStream)
    val tokens = new CommonTokenStream(lexer)
    val parser = new DMLParser(tokens)
    val errors = new ErrorListener(input)

    lexer.removeErrorListener(ConsoleErrorListener.INSTANCE)
    parser.removeErrorListener(ConsoleErrorListener.INSTANCE)
    lexer.addErrorListener(errors)
    parser.addErrorListener(errors)

    val res = parser.model.m

    if (errors.error) None
    else Some(res)
  }

  def alias(ctx: AliasContext): Option[Ident] = if (ctx eq null) None else Some(ctx.id)

  def attributeName(ctx: AttributeNameContext): Option[Ident] = if (ctx eq null) None else Some(ctx.id)

}

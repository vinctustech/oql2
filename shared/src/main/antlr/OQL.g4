grammar OQL;

@header {
  package com.vinctus.oql2;

  import scala.collection.mutable.ListBuffer;
  import scala.collection.mutable.Buffer;
  import scala.collection.immutable.Seq;
  import scala.Some;
  import scala.None$;
}

command returns [OQLAST c]
  : query EOF
    { $c = $query.q; }
  | insert EOF
    { $c = $insert.i; }
  ;

// todo: use /= to represent 'DISTINCT'
query returns [OQLQuery q]
  : entityName project ('[' logicalExpression ']')? group? order? restrict
    { $q = new OQLQuery($entityName.id, null, null, OQLParse.project($project.ps), OQLParse.select($logicalExpression.ctx),
      OQLParse.group($group.ctx), OQLParse.order($order.ctx), $restrict.r.limit(), $restrict.r.offset()); }
  ;

project returns [Buffer<OQLProject> ps]
  : '{' '*' subtracts attributeProjects '}'
    { $ps = $subtracts.ps.prepend(StarOQLProject$.MODULE$).appendAll($attributeProjects.ps); }
  | '{' '*' subtracts '}'
    { $ps = $subtracts.ps.prepend(StarOQLProject$.MODULE$); }
  | '{' attributeProjects '}'
    { $ps = $attributeProjects.ps; }
  | dotProject
    { $ps = $dotProject.ps; }
  ;

dotProject returns [Buffer<OQLProject> ps]
  : '.' attributeName d=dotProject
    { $ps = new ListBuffer<OQLProject>().addOne(OQLParse.dotQueryProject($attributeName.id, $d.ps.toList())); }
  | /* empty (equivalent to '{' '*' '}') */
    { $ps = new ListBuffer<OQLProject>().addOne(StarOQLProject$.MODULE$); }
  ;

subtracts returns [ListBuffer<OQLProject> ps]
  : l=subtracts '-' attributeName
    { $ps = $l.ps.addOne(new SubtractOQLProject($attributeName.id)); }
  | /* empty */
    { $ps = new ListBuffer<OQLProject>(); }
  ;

attributeProjects returns [ListBuffer<OQLProject> ps]
  : l=attributeProjects attributeProject
    { $ps = $l.ps.addOne($attributeProject.p); }
  | attributeProject
    { $ps = new ListBuffer<OQLProject>().addOne($attributeProject.p); }
  ;

attributeProject returns [OQLProject p]
  : label? identifier '(' argument ')'
    { $p = new ExpressionOQLProject(OQLParse.label($label.ctx, $identifier.id, $argument.e), new ApplyOQLExpression($identifier.id, new ListBuffer<OQLExpression>().addOne($argument.e).toList())); }
  | label applyExpression
    { $p = new ExpressionOQLProject($label.id, $applyExpression.e); }
  | label qualifiedAttributeName
    { $p = new ExpressionOQLProject($label.id, $qualifiedAttributeName.e); }
  | label '(' expression ')'
    { $p = new ExpressionOQLProject($label.id, $expression.e); }
  | label? attributeName
    { $p = new ExpressionOQLProject(OQLParse.label($label.ctx, $attributeName.id), new AttributeOQLExpression(new ListBuffer<Ident>().addOne($attributeName.id).toList(), null)); }
  | label? '&' attributeName
    { $p = new ExpressionOQLProject(OQLParse.label($label.ctx, $attributeName.id), new ReferenceOQLExpression(new ListBuffer<Ident>().addOne($attributeName.id).toList())); }
  | label? parameter
    { $p = new ExpressionOQLProject(OQLParse.label($label.ctx, $parameter.e), $parameter.e); }
  | label? query
    { $p = new QueryOQLProject(OQLParse.label($label.ctx, $query.q), $query.q); }
  ;

argument returns [OQLExpression e]
  : identifier
    { $e = new AttributeOQLExpression(new ListBuffer<Ident>().addOne($identifier.id).toList(), null); }
  | '*'
    { $e = StarOQLExpression$.MODULE$; }
  ;

label returns [Ident id]
  : identifier ':'
    { $id = $identifier.id; }
  ;

expression returns [OQLExpression e]
  : additive
    { $e = $additive.e; }
  ;

additive returns [OQLExpression e]
  : l=additive o=('+' | '-') multiplicative
    { $e = new InfixOQLExpression($l.e, $o.text, $multiplicative.e); }
  | multiplicative
    { $e = $multiplicative.e; }
  ;

multiplicative returns [OQLExpression e]
  : l=multiplicative o=('*' | '/') primary
    { $e = new InfixOQLExpression($l.e, $o.text, $primary.e); }
  | primary
    { $e = $primary.e; }
  ;

applyExpression returns [OQLExpression e]
  : identifier '(' expressions ')'
    { $e = new ApplyOQLExpression($identifier.id, $expressions.es.toList()); }
  ;

primary returns [OQLExpression e]
  : FLOAT
    { $e = new FloatOQLExpression(Double.parseDouble($FLOAT.text), new Position($FLOAT.line, $FLOAT.pos)); }
  | INTEGER
    { $e = new IntegerOQLExpression(Integer.parseInt($INTEGER.text), new Position($INTEGER.line, $INTEGER.pos)); }
  | STRING
    { $e = new LiteralOQLExpression($STRING.text.substring(1, $STRING.text.length() - 1), new Position($STRING.line, $STRING.pos)); }
  | '*'
    { $e = StarOQLExpression$.MODULE$; }
  | b=('TRUE' | 'FALSE')
    { $e = new BooleanOQLExpression($b.text, new Position($b.line, $b.pos)); }
  | applyExpression
    { $e = $applyExpression.e; }
  | parameter
    { $e = $parameter.e; }
  | qualifiedAttributeName
    { $e = $qualifiedAttributeName.e; }
  | '&' identifiers
    { $e = new ReferenceOQLExpression($identifiers.ids.toList()); }
  | caseExpression
    { $e = $caseExpression.e; }
  | '-' primary
    { $e = new PrefixOQLExpression("-", $primary.e); }
  | '(' query ')'
    { $e = new QueryOQLExpression($query.q); }
  | '(' expression ')'
    { $e = new GroupedOQLExpression($expression.e); }
  ;

caseExpression returns [OQLExpression e]
  : 'CASE' when+ ('ELSE' expression)? 'END'
    { $e = OQLParse.caseExpression($ctx.when(), $expression.ctx); }
  ;

when returns [OQLWhen w]
  : 'WHEN' logicalExpression 'THEN' expression
    { $w = new OQLWhen($logicalExpression.e, $expression.e); }
  ;

qualifiedAttributeName returns [AttributeOQLExpression e]
  : identifiers
    { $e = new AttributeOQLExpression($identifiers.ids.toList(), null); }
  ;

identifiers returns [ListBuffer<Ident> ids]
  : l=identifiers '.' identifier
    { $ids = $l.ids.addOne($identifier.id); }
  | identifier
    { $ids = new ListBuffer<Ident>().addOne($identifier.id); }
  ;

parameter returns [ParameterOQLExpression e]
  : ':' identifier
    { $e = new ParameterOQLExpression($identifier.id); }
  ;

expressions returns [ListBuffer<OQLExpression> es]
  : l=expressions ',' expression
    { $es = $l.es.addOne($expression.e); }
  | expression
    { $es = new ListBuffer<OQLExpression>().addOne($expression.e); }
  ;

qualifiedAttributeNames returns [ListBuffer<AttributeOQLExpression> es]
  : l=qualifiedAttributeNames ',' qualifiedAttributeName
    { $es = $l.es.addOne($qualifiedAttributeName.e); }
  | qualifiedAttributeName
    { $es = new ListBuffer<AttributeOQLExpression>().addOne($qualifiedAttributeName.e); }
  ;

logicalExpression returns [OQLExpression e]
  : orExpression
    { $e = $orExpression.e; }
  ;

orExpression returns [OQLExpression e]
  : l=orExpression o='OR' r=andExpression
    { $e = new InfixOQLExpression($l.e, $o.text, $r.e); }
  | ex=andExpression
    { $e = $ex.e; }
  ;

andExpression returns [OQLExpression e]
  : l=andExpression o='AND' r=notExpression
    { $e = new InfixOQLExpression($l.e, $o.text, $r.e); }
  | ex=notExpression
    { $e = $ex.e; }
  ;

notExpression returns [OQLExpression e]
  : o='NOT' r=comparisonExpression
    { $e = new PrefixOQLExpression($o.text, $r.e); }
  | ex=comparisonExpression
    { $e = $ex.e; }
  ;

comparisonExpression returns [OQLExpression e]
  : l=expression o=comparison r=expression
    { $e = new InfixOQLExpression($l.e, $o.text, $r.e); }
  | exp=expression between l=expression 'AND' u=expression
    { $e = new BetweenOQLExpression($exp.e, $between.text, $l.e, $u.e); }
  | expression isNull
    { $e = new PostfixOQLExpression($expression.e, $isNull.text); }
  | expression in '(' expressions ')'
    { $e = new InArrayOQLExpression($expression.e, $in.text, $expressions.es.toList()); }
  | expression in parameter
    { $e = new InParameterOQLExpression($expression.e, $in.text, $parameter.e); }
  | expression in '(' query ')'
    { $e = new InQueryOQLExpression($expression.e, $in.text, $query.q); }
  | 'EXISTS' '(' query ')'
    { $e = new ExistsOQLExpression($query.q); }
  | ex=logicalPrimary
    { $e = $ex.e; }
  ;

logicalPrimary returns [OQLExpression e]
  : b=('TRUE' | 'FALSE')
    { $e = new BooleanOQLExpression($b.text, new Position($b.line, $b.pos)); }
  | parameter
    { $e = $parameter.e; }
  | qualifiedAttributeName
    { $e = $qualifiedAttributeName.e; }
  | '(' logicalExpression ')'
    { $e = new GroupedOQLExpression($logicalExpression.e); }
  ;

in
  : 'NOT'? 'IN'
  ;

comparison
  : '<=' | '>=' | '<' | '>' | '=' | '!=' | 'NOT'? ('LIKE' | 'ILIKE')
  ;

between
  : 'NOT'? 'BETWEEN'
  ;

isNull
  : 'IS' 'NULL' | 'IS' 'NOT' 'NULL'
  ;

group returns  [ListBuffer<OQLExpression> es]
  : '/' expressions '/'
    { $es = $expressions.es; }
  ;

order returns [ListBuffer<OQLOrdering> os]
  : '<' orderings '>'
    { $os = $orderings.os; }
  ;

orderings returns [ListBuffer<OQLOrdering> os]
  : l=orderings ',' ordering
    { $os = $l.os.addOne($ordering.o); }
  | ordering
    { $os = new ListBuffer<OQLOrdering>().addOne($ordering.o); }
  ;

ordering returns [OQLOrdering o]
  : expression dir? nulls?
    { $o = new OQLOrdering($expression.e, OQLParse.ordering($dir.text, $nulls.text)); }
  ;

dir
  : 'ASC' | 'DESC'
  ;

nulls
  : 'NULLS' ('FIRST' | 'LAST')
  ;

restrict returns [OQLRestrict r]
  : '|' l=INTEGER (',' o=INTEGER)? '|'
    { $r = OQLParse.restrict($l.text, $o.text); }
  | '|' ',' INTEGER '|'
    { $r = OQLParse.restrict(null, $INTEGER.text); }
  | // empty
    { $r = OQLParse.restrict(null, null); }
  ;

entityName returns [Ident id]
  : identifier
    { $id = $identifier.id; }
  ;

attributeName returns [Ident id]
  : identifier
    { $id = $identifier.id; }
  ;

identifier returns [Ident id]
  : IDENTIFIER
    { $id = new Ident($IDENTIFIER.text, $IDENTIFIER.line, $IDENTIFIER.pos); }
  ;

insert returns [OQLInsert i]
  : entityName '<-' objects
    { $i = new OQLInsert($entityName.id, $objects.os.toList()); }
  ;

objects returns [ListBuffer<Seq<OQLKeyValuePair>> os]
  : l=objects object
    { $os = $l.os.addOne($object.o); }
  | object
    { $os = new ListBuffer<Seq<OQLKeyValuePair>>().addOne($object.o); }
  ;

object returns [Seq<OQLKeyValuePair> o]
  : '{' pairs '}'
    { $o = $pairs.ps.toList(); }
  ;

pairs returns [ListBuffer<OQLKeyValuePair> ps]
  : l=pairs ',' pair
    { $ps = $l.ps.addOne($pair.p); }
  | pair
    { $ps = new ListBuffer<OQLKeyValuePair>().addOne($pair.p); }
  ;

pair returns [OQLKeyValuePair p]
  : label expression
    { $p = new OQLKeyValuePair($label.id, $expression.e); }
  ;

FLOAT
  : [0-9]* '.' [0-9]+ ([eE] [+-]? [0-9]+)?
  ;

INTEGER
  : [0-9]+
  ;

STRING
  : '"' CONTENT '"'
  | '\'' CONTENT '\''
  ;

fragment ESC
  : '\\' .
  ;

fragment CONTENT
  : (ESC|.)*?
  ;

IDENTIFIER
  : [A-Za-z_] [A-Za-z0-9_]*
  ;

WS
  : [ \t\r\n]+ -> skip
  ;

COMMENT
  : '/*' (COMMENT | .)*? '*/' -> skip
  ;

LINE_COMMENT
  : '//' (~[\r\n])* -> skip
  ;

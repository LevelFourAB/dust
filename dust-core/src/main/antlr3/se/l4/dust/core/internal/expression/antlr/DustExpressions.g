grammar DustExpressions;

options {
	output=AST;
	backtrack=true;
	memoize=true;
}

tokens {
	INVOKE;
	ID;
	NAMESPACE;
	NOT;
	GROUP;
	TERNARY;
	INDEXED;
}

@parser::header {
	package se.l4.dust.core.internal.expression.antlr;
}

@lexer::header {
	package se.l4.dust.core.internal.expression.antlr;
}

root	:	logicalExpression EOF!;

expression
	:	LPAREN expr=logicalExpression RPAREN -> $expr
	|	constant
	|	keyword
	|	chain
	;
	
keyword	:	NULL | TRUE | FALSE | THIS;

id	:	var=NAMESPACED_IDENTIFIER -> ^(NAMESPACE $var)
	|	var=IDENTIFIER -> ^(ID $var);
	
term	:	(id | method) ('[' expression ']')+ -> ^(INDEXED id? method? expression+)
	|	(id | method) 
	;

constant:	LONG | DOUBLE | STRING;

chain	:	term CHAIN chain -> ^(CHAIN term chain)
	|	term CHAIN_NULL chain -> ^(CHAIN_NULL term chain)
	|	term;
	
method	:	id LPAREN RPAREN -> ^(INVOKE id)
	|	id LPAREN methodExpressions RPAREN -> ^(INVOKE id methodExpressions)
	;
	
methodExpressions
	:	logicalExpression (COMMA! logicalExpression)*;
	
not	:	'!' logicalExpression -> ^(NOT logicalExpression);

// Logical operations
logicalExpression
	:	test=booleanAndExpression '?' left=booleanAndExpression (':' right=booleanAndExpression)? -> ^(TERNARY $test $left $right?)
	|	booleanAndExpression ( OR^ booleanAndExpression )*
	;
	
booleanAndExpression
	:	equalityExpression ( AND^ equalityExpression )*
	;
	
equalityExpression
	:	relationalExpression ( (EQUAL|NOT_EQUAL)^ relationalExpression)*
	;
	
relationalExpression
	:	additiveExpression ( (LESS | LESS_OR_EQUAL | MORE | MORE_OR_EQUAL)^ additiveExpression)?
	;

additiveExpression
	:	multiplicativeExpression ( (PLUS | MINUS)^ multiplicativeExpression )*
	;

multiplicativeExpression
	:	unaryExpression (( MULTIPLY | DIVIDE | MODULO )^ unaryExpression)*
	;
	
unaryExpression
	:	PLUS^ unaryExpression
	|	MINUS^ unaryExpression
	|	NOT expression -> ^(NOT expression)
	|	expression;

NULL	:	'null';
THIS	:	'this';
TRUE	:	'true';
FALSE	:	'false';

COMMA	:	',';
LPAREN	:	'(';
RPAREN	:	')';
LBRACKET:	'[';
RBRACKER:	']';

CHAIN	:	'.';
CHAIN_NULL	:	'?.';

AND	:	'&&'
	|	'and';

OR	:	'||'
	|	'or';

LESS	:	'<'
	|	'lt';

LESS_OR_EQUAL
	:	'<='
	|	'lt=';
	
MORE	:	'>'
	|	'gt';

MORE_OR_EQUAL
	:	'>='
	|	'gt=';
	
EQUAL	:	'=='
	|	'is';
	
NOT_EQUAL
	:	'!='
	|	'not';

NOT	:	'!';

PLUS	:	'+';
MINUS	:	'-';

fragment DIGIT
	:	'0'..'9';
	
fragment SIGN
	:	('+'|'-');
	
MULTIPLY:	'*';
DIVIDE	:	'/';
MODULO	:	'%';

QMARK	:	'?';

// Numbers	
LONG	: 	'0'
	| 	'1'..'9' ('0'..'9')*
	;

fragment NonIntegerNumber
	: 	('0'..'9')+ '.' ('0'..'9')* Exponent?  
	| 	'.' ('0'..'9')+ Exponent?  
	| 	('0'..'9')+ Exponent
        ;
        
fragment Exponent
	: 	( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+;

fragment HEX
	: 	('0'..'9') | ('A'..'Z') | ('a'..'z')
	;

DOUBLE	:	NonIntegerNumber;
	
// Strings
STRING	:
	'\''
	(   EscapeSequence
		|   ~( '\\' | '\'' | '\r' | '\n' )        
	)* 
        '\'' 
    ;

fragment EscapeSequence 
	:   '\\' (
		'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\'' | '\\' 
		| 'u' HEX HEX HEX HEX
	);

NAMESPACED_IDENTIFIER
	:	IDENTIFIER ':' IDENTIFIER;
		
IDENTIFIER
	:	ID_START (ID_PART)*;
	
fragment ID_START
	:  '\u0024'
	|  '\u0041'..'\u005a'
	|  '\u005f'
	|  '\u0061'..'\u007a'
	|  '\u00c0'..'\u00d6'
	|  '\u00d8'..'\u00f6'
	|  '\u00f8'..'\u00ff'
	|  '\u0100'..'\u1fff'
	|  '\u3040'..'\u318f'
	|  '\u3300'..'\u337f'
	|  '\u3400'..'\u3d2d'
	|  '\u4e00'..'\u9fff'
	|  '\uf900'..'\ufaff'
	;

fragment ID_PART
	: ID_START
	|  '\u0030'..'\u0039'
	;
	
WS	:
	(' '|'\t'|'\n'|'\r')+ { skip(); };
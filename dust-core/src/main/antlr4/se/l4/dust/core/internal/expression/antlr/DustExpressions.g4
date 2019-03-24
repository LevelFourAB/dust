grammar DustExpressions;

root
	: logicalExpression EOF
	;

// Logical operations
logicalExpression
	: unaryExpression #unary
	| logicalExpression op=( MULTIPLY | DIVIDE | MODULO ) logicalExpression #multiplicativeExpression
	| logicalExpression op=( PLUS | MINUS ) logicalExpression #additiveExpression
	| logicalExpression op=( LESS | LESS_OR_EQUAL | MORE_CMP | MORE_OR_EQUAL ) logicalExpression #comparingExpression
	| logicalExpression op=( EQUAL | NOT_EQUAL ) logicalExpression #equalCompareExpression
	| test=logicalExpression '?' left=logicalExpression (':' right=logicalExpression)? #ternaryExpression
	| logicalExpression AND logicalExpression #booleanAndExpression
	| logicalExpression OR logicalExpression #booleanOrExpression
	;

unaryExpression
	: PLUS unaryExpression #unaryPlusExpression
	| MINUS unaryExpression #unaryMinusExpression
	| NOT expression #unaryNotExpression
	| expression #unaryExpr
	;

expression
	: LPAREN expr=logicalExpression RPAREN #specificLogicalExpression
	| array #arrayExpression
	| constant #constantExpression
	| keyword #keywordExpression
	| chain #chainedExpression
	;

keyword
	: NULL #nullKeyword
	| TRUE #trueKeyword
	| FALSE #falseKeyword
	| THIS #thisKeyword
	;

id
	: var=NAMESPACED_IDENTIFIER #namespaceId
	| var=IDENTIFIER #normalId
	;

term
	: (id | method) ('[' expression ']')+ #indexedAccess
	| (id | method) #directAccess
	;

constant
	: LONG #longConstant
	| DOUBLE #doubleConstant
	| STRING #stringConstant
	;

chain
	: term CHAIN chain #chainNormal
	| term CHAIN_NULL chain #chainNullSafe
	| term #chainTerm
	;

method
	: id LPAREN RPAREN #invokeEmpty
	| id LPAREN methodExpressions RPAREN #invokeArguments
	;

methodExpressions
	: logicalExpression (COMMA logicalExpression)*
	;

not
	: '!' logicalExpression;

array
	: LBRACKET RBRACKET
	| LBRACKET args=methodExpressions RBRACKET
	;

NULL : 'null';
THIS : 'this';
TRUE : 'true';
FALSE : 'false';

COMMA : ',';
LPAREN :'(';
RPAREN : ')';
LBRACKET : '[';
RBRACKET : ']';

CHAIN : '.';
CHAIN_NULL : '?.';

AND : '&&' | 'and';
OR : '||' | 'or';

LESS : '<' | 'lt';
LESS_OR_EQUAL : '<=' | 'lt=';
MORE_CMP : '>' | 'gt';
MORE_OR_EQUAL : '>=' | 'gt=';

EQUAL : '==' | 'is';

NOT_EQUAL : '!=' | 'not';

NOT : '!';

PLUS : '+';
MINUS : '-';

fragment DIGIT : '0'..'9';

fragment SIGN : ('+'|'-');

MULTIPLY : '*';
DIVIDE : '/';
MODULO : '%';

QMARK : '?';

// Numbers
LONG
	: '0'
	| '1'..'9' ('0'..'9')*
	;

fragment NonIntegerNumber
	: ('0'..'9')+ '.' ('0'..'9')* Exponent?
	| '.' ('0'..'9')+ Exponent?
	| ('0'..'9')+ Exponent
    ;

fragment Exponent
	: ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
	;

fragment HEX
	: ('0'..'9') | ('A'..'Z') | ('a'..'z')
	;

DOUBLE : NonIntegerNumber;

// Strings
STRING :
	'\''
	(
		EscapeSequence
		| ~( '\\' | '\'' | '\r' | '\n' )
	)*
        '\''
    ;

fragment EscapeSequence
	: '\\' (
		'b' | 't' | 'n' | 'f' | 'r' | '"' | '\'' | '\\'
		| 'u' HEX HEX HEX HEX
	);

NAMESPACED_IDENTIFIER
	: IDENTIFIER ':' IDENTIFIER;

IDENTIFIER
	: ID_START (ID_PART)*;

fragment ID_START
	: '\u0024'
	| '\u0041'..'\u005a'
	| '\u005f'
	| '\u0061'..'\u007a'
	| '\u00c0'..'\u00d6'
	| '\u00d8'..'\u00f6'
	| '\u00f8'..'\u00ff'
	| '\u0100'..'\u1fff'
	| '\u3040'..'\u318f'
	| '\u3300'..'\u337f'
	| '\u3400'..'\u3d2d'
	| '\u4e00'..'\u9fff'
	| '\uf900'..'\ufaff'
	;

fragment ID_PART
	: ID_START
	| '\u0030'..'\u0039'
	;

WS : (' '|'\t'|'\n'|'\r')+ -> skip;

ERROR_CHAR : . ;

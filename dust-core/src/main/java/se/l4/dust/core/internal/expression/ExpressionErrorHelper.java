package se.l4.dust.core.internal.expression;

import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.AND;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.CHAIN;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.CHAIN_NULL;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.COMMA;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.DIVIDE;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.DOUBLE;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.IDENTIFIER;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.LONG;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.LPAREN;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MINUS;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MODULO;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MULTIPLY;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.NAMESPACED_IDENTIFIER;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.NOT;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.OR;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.PLUS;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.QMARK;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.RPAREN;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.STRING;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.VOCABULARY;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.IntegerList;
import org.antlr.v4.runtime.misc.IntervalSet;

import se.l4.dust.api.expression.ExpressionParseException;

public class ExpressionErrorHelper
	extends BaseErrorListener
{
	private final String source;
	private final TokenStream tokens;

	public ExpressionErrorHelper(String source, TokenStream tokens)
	{
		this.source = source;
		this.tokens = tokens;
	}

	@Override
	public void syntaxError(
		Recognizer<?, ?> recognizer,
		Object offendingSymbol,
		int line,
		int charPositionInLine,
		String msg,
		RecognitionException e
	)
	{
		StringBuilder builder = new StringBuilder();
		if(e instanceof NoViableAltException)
		{
			NoViableAltException v = (NoViableAltException) e;

			builder.append("Invalid continuation of ");

			Token token = e.getOffendingToken();
			token = findLastKnown(tokens, token);
			builder.append(getReadableName(token));

			outputAlternatives(builder, token, v.getExpectedTokens());
		}
		else if(e instanceof InputMismatchException)
		{
			Token token = e.getOffendingToken();
			IntervalSet expectedTokens = e.getExpectedTokens();
			if(token == null)
			{
				builder.append("Mismatched token");
			}
			else
			{
				builder.append("Mismatched ");
				builder.append(getReadableName(token.getType()));
			}

			builder.append(". Parser expected one of ");
			ouputExpectedTokens(builder, expectedTokens);
		}
		else if(e == null && offendingSymbol instanceof CommonToken)
		{
			CommonToken token = (CommonToken) offendingSymbol;
			builder.append("Extra input at '");
			builder.append(token.getText());
			builder.append("', end of expression expected");
		}
		else
		{
			builder.append(msg);
		}

		throw new ExpressionParseException(source, line, charPositionInLine, builder.toString());
	}

	private static Token findLastKnown(TokenStream stream, Token start)
	{
		if(start.getTokenIndex() > 0 && stream != null)
		{
			for(int i=start.getTokenIndex(); i>=0; i--)
			{
				Token t = stream.get(i);
				if(t.getType() >= 0)
				{
					return t;
				}
			}
		}

		return start;
	}

	private static void ouputExpectedTokens(StringBuilder builder, IntervalSet expectedTokens)
	{
		IntegerList list = expectedTokens.toIntegerList();
		boolean hasOutput = false;
		for(int t=0, n=list.size(); t<n; t++)
		{
			if(hasOutput) builder.append(t == n - 1 ? " or " : ", ");

			String name = getReadableName(t);
			if(name == null) continue;

			builder.append(name);
			hasOutput = true;
		}
	}

	private static void outputAlternatives(StringBuilder builder, Token token, IntervalSet expectedTokens)
	{
		String[] alts = getAlternativesFor(token.getType());
		if(alts == null && expectedTokens.isNil()) return;

		builder.append(". Did you mean to follow with ");
		if(alts == null)
		{
			ouputExpectedTokens(builder, expectedTokens);
		}
		else
		{
			for(int i=0, n=alts.length; i<n; i++)
			{
				if(i > 0) builder.append(i == n - 1 ? " or " : ", ");

				builder.append(alts[i]);
			}
		}
		builder.append("?");
	}

	private static String getReadableName(Token t)
	{
		if(t.getType() == -1)
		{
			return t.getText();
		}

		return getReadableName(t.getType());
	}

	private static String getReadableName( int tokenType)
	{
		if(tokenType < 0)
		{
			return "token";
		}

		switch(tokenType)
		{
			case Token.EOF:
				return "<EOF>";
			case AND:
				return "and (&&) expression";
			case OR:
				return "or (||) expression";
			case CHAIN:
				return "property or method call chain (.)";
			case CHAIN_NULL:
				return "nullable property or method call chain (.?)";
			case IDENTIFIER:
				return "property or method name";
			case NAMESPACED_IDENTIFIER:
				return "namespaced property or method";
			case LPAREN:
				return "'('";
			case RPAREN:
				return "')'";
			case COMMA:
				return "','";
			case MINUS:
				return "'-'";
			case PLUS:
				return "'+'";
			case QMARK:
				return "'?'";
			case LONG:
				return "long";
			case DOUBLE:
				return "double";
			case STRING:
				return "string";
			case NOT:
				return "not expression";
			case DIVIDE:
				return "division";
			case MULTIPLY:
				return "multiplication";
			case MODULO:
				return "modulo";
		}

		String name = VOCABULARY.getLiteralName(tokenType);
		if(name == null)
		{
			name = VOCABULARY.getSymbolicName(tokenType);
		}
		return name;
	}

	private static String[] getAlternativesFor(int tokenType)
	{
		switch(tokenType)
		{
			case IDENTIFIER:
			case CHAIN:
				return new String[] {
					"method call",
					"property name"
				};
			case COMMA:
				return new String[] {
					"parameter",
					"')'"
				};
			case MINUS:
			case PLUS:
				return new String[] {
					"number",
					"property",
					"method call"
				};
			case QMARK:
				return new String[] {
					"expression to run when test is true"
				};
		}

		return null;
	}
}

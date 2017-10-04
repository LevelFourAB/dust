package se.l4.dust.core.internal.expression;

import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.AND;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.ARRAY;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.CHAIN;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.CHAIN_NULL;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.COMMA;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.DIVIDE;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.DOUBLE;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.EQUAL;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.FALSE;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.ID;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.IDENTIFIER;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.INDEXED;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.INVOKE;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.LESS;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.LESS_OR_EQUAL;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.LONG;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.LPAREN;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MINUS;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MODULO;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MORE;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MORE_OR_EQUAL;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MULTIPLY;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.NAMESPACE;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.NOT;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.NOT_EQUAL;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.NULL;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.OR;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.PLUS;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.QMARK;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.RPAREN;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.STRING;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.TERNARY;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.THIS;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.TRUE;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.tokenNames;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.MismatchedNotSetException;
import org.antlr.runtime.MismatchedRangeException;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.MissingTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.UnwantedTokenException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import se.l4.dust.api.expression.ExpressionParseException;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsLexer;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser;
import se.l4.dust.core.internal.expression.ast.AddNode;
import se.l4.dust.core.internal.expression.ast.AndNode;
import se.l4.dust.core.internal.expression.ast.ArrayNode;
import se.l4.dust.core.internal.expression.ast.ChainNode;
import se.l4.dust.core.internal.expression.ast.DivideNode;
import se.l4.dust.core.internal.expression.ast.DoubleNode;
import se.l4.dust.core.internal.expression.ast.EqualsNode;
import se.l4.dust.core.internal.expression.ast.GreaterNode;
import se.l4.dust.core.internal.expression.ast.GreaterOrEqualNode;
import se.l4.dust.core.internal.expression.ast.IdentifierNode;
import se.l4.dust.core.internal.expression.ast.IndexNode;
import se.l4.dust.core.internal.expression.ast.InvokeNode;
import se.l4.dust.core.internal.expression.ast.KeywordNode;
import se.l4.dust.core.internal.expression.ast.LessNode;
import se.l4.dust.core.internal.expression.ast.LessOrEqualNode;
import se.l4.dust.core.internal.expression.ast.LongNode;
import se.l4.dust.core.internal.expression.ast.ModuloNode;
import se.l4.dust.core.internal.expression.ast.MultiplyNode;
import se.l4.dust.core.internal.expression.ast.NegateNode;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.ast.NotEqualsNode;
import se.l4.dust.core.internal.expression.ast.OrNode;
import se.l4.dust.core.internal.expression.ast.SignNode;
import se.l4.dust.core.internal.expression.ast.StringNode;
import se.l4.dust.core.internal.expression.ast.SubtractNode;
import se.l4.dust.core.internal.expression.ast.TernaryNode;

/**
 * Parser for the internal Dust expressions.
 *
 * @author Andreas Holstenson
 *
 */
public class ExpressionParser
{
	private ExpressionParser()
	{
	}

	/**
	 * Parse the specified string.
	 *
	 * @param in
	 * @return
	 * @throws RecognitionException
	 */
	public static Node parse(final String in)
	{
		CharStream stream = new ANTLRStringStream(in);
		DustExpressionsLexer lexer = new DustExpressionsLexer(stream)
		{
			@Override
			public void reportError(RecognitionException e)
			{
				ExpressionParser.reportLexerError(in, e);
			}
		};
		TokenRewriteStream tokens = new TokenRewriteStream(lexer);
		DustExpressionsParser parser = new DustExpressionsParser(tokens)
		{
			@Override
			public void reportError(RecognitionException e)
			{
				ExpressionParser.reportParserError(in, e, getTokenStream());
			};
		};

		try
		{
			DustExpressionsParser.root_return ret = parser.root();
			CommonTree tree = (CommonTree) ret.getTree();

			return createNode(tree);
		}
		catch(RecognitionException e)
		{
			reportParserError(in, e, tokens);
			return null;
		}
	}

	private static Node createNode(Tree tree)
	{
		int line = tree.getLine();
		int position = tree.getCharPositionInLine();

		switch(tree.getType())
		{
			case TRUE:
				return new KeywordNode(line, position, KeywordNode.Type.TRUE);
			case FALSE:
				return new KeywordNode(line, position, KeywordNode.Type.FALSE);
			case NULL:
				return new KeywordNode(line, position, KeywordNode.Type.NULL);
			case THIS:
				return new KeywordNode(line, position, KeywordNode.Type.THIS);

			case LONG:
			{
				long v = Long.parseLong(tree.getText());
				return new LongNode(line, position, v);
			}

			case DOUBLE:
			{
				double v = Double.parseDouble(tree.getText());
				return new DoubleNode(line, position, v);
			}

			case STRING:
			{
				String v = tree.getText();
				return new StringNode(line, position, StringNode.decode(v.substring(1, v.length()-1)));
			}

			case NAMESPACE:
			{
				// Id with a namespace.
				String text = tree.getChild(0).getText();

				int idx = text.indexOf(':');
				return new IdentifierNode(line, position, text.substring(0, idx), text.substring(idx+1));
			}

			case ID:
			{
				return new IdentifierNode(line, position, null, tree.getChild(0).getText());
			}

			case INVOKE:
			{
				// First child will be the identifier
				IdentifierNode id = (IdentifierNode) createNode(tree.getChild(0));

				// All the other create the list of parameters
				List<Node> params = new ArrayList<Node>();
				for(int i=1, n=tree.getChildCount(); i<n; i++)
				{
					params.add(createNode(tree.getChild(i)));
				}

				return new InvokeNode(line, position, id, params);
			}

			case TERNARY:
			{
				Node test = createNode(tree.getChild(0));
				Node left = createNode(tree.getChild(1));

				Node right = tree.getChildCount() >= 3
					? createNode(tree.getChild(2))
					: null;

				return new TernaryNode(line, position, test, left, right);
			}

			case NOT:
			{
				Node child = createNode(tree.getChild(0));
				return new NegateNode(line, position, child);
			}

			case CHAIN:
			{
				Node left = createNode(tree.getChild(0));
				Node right = createNode(tree.getChild(1));
				return new ChainNode(line, position, left, right);
			}

			case EQUAL:
			{
				Node left = createNode(tree.getChild(0));
				Node right = createNode(tree.getChild(1));
				return new EqualsNode(line, position, left, right);
			}

			case NOT_EQUAL:
			{
				Node left = createNode(tree.getChild(0));
				Node right = createNode(tree.getChild(1));
				return new NotEqualsNode(line, position, left, right);
			}

			case LESS:
			{
				Node left = createNode(tree.getChild(0));
				Node right = createNode(tree.getChild(1));
				return new LessNode(line, position, left, right);
			}

			case LESS_OR_EQUAL:
			{
				Node left = createNode(tree.getChild(0));
				Node right = createNode(tree.getChild(1));
				return new LessOrEqualNode(line, position, left, right);
			}

			case MORE:
			{
				Node left = createNode(tree.getChild(0));
				Node right = createNode(tree.getChild(1));
				return new GreaterNode(line, position, left, right);
			}

			case MORE_OR_EQUAL:
			{
				Node left = createNode(tree.getChild(0));
				Node right = createNode(tree.getChild(1));
				return new GreaterOrEqualNode(line, position, left, right);
			}

			case OR:
			{
				Node left = createNode(tree.getChild(0));
				Node right = createNode(tree.getChild(1));
				return new OrNode(line, position, left, right);
			}

			case AND:
			{
				Node left = createNode(tree.getChild(0));
				Node right = createNode(tree.getChild(1));
				return new AndNode(line, position, left, right);
			}

			case PLUS:
			{
				if(tree.getChildCount() == 1)
				{
					/*
					 * This is an expression to turn a result negative such
					 * as: -2 or -id()
					 */
					Node child = createNode(tree.getChild(0));

					// Optimization for static numbers
					if(child instanceof LongNode || child instanceof DoubleNode)
					{
						return child;
					}

					return new SignNode(line, position, false, child);
				}
				else
				{
					Node left = createNode(tree.getChild(0));
					Node right = createNode(tree.getChild(1));
					return new AddNode(line, position, left, right);
				}
			}

			case MINUS:
			{
				if(tree.getChildCount() == 1)
				{
					/*
					 * This is an expression to turn a result negative such
					 * as: -2 or -id()
					 */
					Node child = createNode(tree.getChild(0));

					// Optimization for static numbers
					if(child instanceof LongNode)
					{
						return new LongNode(line, position, - ((LongNode) child).getValue());
					}
					else if(child instanceof DoubleNode)
					{
						return new DoubleNode(line, position, - ((DoubleNode) child).getValue());
					}

					return new SignNode(line, position, true, child);
				}
				else
				{
					Node left = createNode(tree.getChild(0));
					Node right = createNode(tree.getChild(1));
					return new SubtractNode(line, position, left, right);
				}
			}

			case MULTIPLY:
			{
				Node left = createNode(tree.getChild(0));
				Node right = createNode(tree.getChild(1));
				return new MultiplyNode(line, position, left, right);
			}

			case DIVIDE:
			{
				Node left = createNode(tree.getChild(0));
				Node right = createNode(tree.getChild(1));
				return new DivideNode(line, position, left, right);
			}

			case MODULO:
			{
				Node left = createNode(tree.getChild(0));
				Node right = createNode(tree.getChild(1));
				return new ModuloNode(line, position, left, right);
			}

			case INDEXED:
			{
				Node left = createNode(tree.getChild(0));
				Node[] indexes = new Node[tree.getChildCount() - 1];
				for(int i=0, n=indexes.length; i<n; i++)
				{
					indexes[i] = createNode(tree.getChild(i+1));
				}
				return new IndexNode(line, position, left, indexes);
			}

			case ARRAY:
			{
				Node[] values = new Node[tree.getChildCount()];
				for(int i=0, n=values.length; i<n; i++)
				{
					values[i] = createNode(tree.getChild(i));
				}

				return new ArrayNode(line, position, values);
			}
		}

		throw new Error("Unknown node with type " + tree.getType());
	}

	private static void reportLexerError(String source, RecognitionException e)
	{
		String msg;
		if(e instanceof MismatchedTokenException)
		{
			MismatchedTokenException mte = (MismatchedTokenException) e;
			msg = "Mismatched character, got " + toDisplay(e.c) + " but expected " + toDisplay(mte.expecting);
		}
		else if(e instanceof NoViableAltException)
		{
			msg = "No viable continuation at character " + toDisplay(e.c);
		}
		else if(e instanceof EarlyExitException)
		{
			msg = "No viable continuation at character " + toDisplay(e.c);
		}
		else if(e instanceof MismatchedNotSetException)
		{
			MismatchedNotSetException mse = (MismatchedNotSetException) e;
			msg = "Mismatched character, got " + toDisplay(e.c) + " but expected " + mse.expecting;
		}
		else if(e instanceof MismatchedSetException)
		{
			MismatchedSetException mse = (MismatchedSetException) e;
			msg = "Mismatched character, got " + toDisplay(e.c) + " but expected " + mse.expecting;
		}
		else if(e instanceof MismatchedRangeException)
		{
			MismatchedRangeException mre = (MismatchedRangeException) e;
			msg = "Mismatched character, got " + toDisplay(e.c) + " but expected something in range " + toDisplay(mre.a) + ".." + toDisplay(mre.b);
		}
		else
		{
			msg = "Unknwon error at " + toDisplay(e.c);
		}

		throw new ExpressionParseException(source, e.line, e.charPositionInLine, msg);
	}

	private static String toDisplay(int c)
	{
		return "'" + (char) c + "'";
	}

	private static void reportParserError(String source, RecognitionException e, TokenStream tokens)
	{
		StringBuilder builder = new StringBuilder();
		Token token = e.token;

		if(e instanceof NoViableAltException)
		{
			NoViableAltException v = (NoViableAltException) e;

			builder.append("Invalid continuation of ");
			token = findLastKnown(tokens, token);
			builder.append(getReadableName(token));

			outputAlternatives(builder, token);
		}
		else if(e instanceof MissingTokenException)
		{
			MissingTokenException mte = (MissingTokenException)e;

			builder.append("Invalid continuation of ");
			builder.append(getReadableName(token));

			builder.append(". Parser expected ");
			if(mte.expecting == Token.EOF)
			{
				builder.append("EOF");
			}
			else
			{
				builder.append(getReadableName(mte.expecting));
			}

			outputAlternatives(builder, token);
		}
		else if(e instanceof UnwantedTokenException)
		{
			UnwantedTokenException uw = (UnwantedTokenException) e;
			builder.append("Dangling ");
			builder.append(getReadableName(token.getType()));

			builder.append(". Parser expected ");
			if(uw.expecting == Token.EOF)
			{
				builder.append("EOF");
			}
			else
			{
				builder.append(getReadableName(uw.expecting));
			}

			outputAlternatives(builder, findLastKnown(tokens, token));
		}
		else if(e instanceof MismatchedTokenException)
		{
			MismatchedTokenException me = (MismatchedTokenException) e;

			if(token == null)
			{
				builder.append("Mismatched token");
			}
			else
			{
				builder.append("Mismatched ");
				builder.append(getReadableName(token.getType()));
			}

			builder.append(". Parser expected ");
			if(me.expecting == Token.EOF)
			{
				builder.append("EOF");
			}
			else
			{
				builder.append(getReadableName(me.expecting));
			}
		}

		String msg = builder.length() == 0 ? "Unknown error type" : builder.toString();
		throw new ExpressionParseException(source, e.line, e.charPositionInLine, msg);
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

	private static void outputAlternatives(StringBuilder builder, Token token)
	{
		String[] alts = getAlternativesFor(token.getType());
		if(alts != null)
		{
			builder.append(". Did you mean to follow with ");
			for(int i=0, n=alts.length; i<n; i++)
			{
				if(i > 0) builder.append(i == n - 1 ? " or " : ", ");

				builder.append(alts[i]);
			}
			builder.append("?");
		}
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
			case AND:
				return "&& expression";
			case OR:
				return "|| expression";
			case CHAIN:
			case CHAIN_NULL:
				return "property expansion";
			case IDENTIFIER:
				return "property name";
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
			case DOUBLE:
				return "number";
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

		return tokenNames[tokenType];
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

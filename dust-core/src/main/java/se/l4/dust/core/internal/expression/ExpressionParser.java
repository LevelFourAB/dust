package se.l4.dust.core.internal.expression;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import se.l4.dust.core.internal.expression.antlr.DustExpressionsLexer;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.RootContext;
import se.l4.dust.core.internal.expression.ast.Node;

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
	public static Node parse(String in)
	{
		CharStream stream = CharStreams.fromString(in);

		DustExpressionsLexer lexer = new DustExpressionsLexer(stream);
		lexer.removeErrorListeners();

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		DustExpressionsParser parser = new DustExpressionsParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(new ExpressionErrorHelper(in, tokens));

		RootContext root = parser.root();
		ExpressionVisitor visitor = new ExpressionVisitor();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(visitor, root);
		return visitor.getRoot();
	}

}

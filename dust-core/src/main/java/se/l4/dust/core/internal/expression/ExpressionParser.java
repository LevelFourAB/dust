package se.l4.dust.core.internal.expression;

import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.*;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.Tree;

import se.l4.dust.core.internal.expression.antlr.DustExpressionsLexer;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser;
import se.l4.dust.core.internal.expression.ast.AddNode;
import se.l4.dust.core.internal.expression.ast.AndNode;
import se.l4.dust.core.internal.expression.ast.ChainNode;
import se.l4.dust.core.internal.expression.ast.DivideNode;
import se.l4.dust.core.internal.expression.ast.DoubleNode;
import se.l4.dust.core.internal.expression.ast.EqualsNode;
import se.l4.dust.core.internal.expression.ast.GreaterNode;
import se.l4.dust.core.internal.expression.ast.GreaterOrEqualNode;
import se.l4.dust.core.internal.expression.ast.IdentifierNode;
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
	public static Node parse(String in)
		throws RecognitionException
	{
		CharStream stream = new ANTLRStringStream(in);
		DustExpressionsLexer lexer = new DustExpressionsLexer(stream);
		TokenRewriteStream tokens = new TokenRewriteStream(lexer);
		DustExpressionsParser parser = new DustExpressionsParser(tokens);
		
		CommonTreeAdaptor adaptor = new CommonTreeAdaptor()
		{
			@Override
			public Object create(Token payload)
			{
				return new CommonTree(payload);
			}
		};
		
		parser.setTreeAdaptor(adaptor);
		DustExpressionsParser.root_return ret = parser.root();
		CommonTree tree = (CommonTree) ret.getTree();
		
		return createNode(tree);
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
		}
		
		throw new Error("Unknown node with type " + tree.getType());
	}
}

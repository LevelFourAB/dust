package se.l4.dust.core.internal.expression;

import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.DIVIDE;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.EQUAL;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.LESS;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.LESS_OR_EQUAL;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MINUS;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MODULO;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MORE_CMP;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MORE_OR_EQUAL;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MULTIPLY;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.NOT_EQUAL;
import static se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.PLUS;

import java.util.Collections;
import java.util.LinkedList;

import se.l4.dust.api.expression.ExpressionException;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsBaseListener;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.AdditiveExpressionContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.ArrayContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.BooleanAndExpressionContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.BooleanOrExpressionContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.ChainNormalContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.ChainNullSafeContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.ComparingExpressionContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.DoubleConstantContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.EqualCompareExpressionContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.FalseKeywordContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.IndexedAccessContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.InvokeArgumentsContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.InvokeEmptyContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.LongConstantContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.MultiplicativeExpressionContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.NamespaceIdContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.NormalIdContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.NullKeywordContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.StringConstantContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.TernaryExpressionContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.ThisKeywordContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.TrueKeywordContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.UnaryMinusExpressionContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.UnaryNotExpressionContext;
import se.l4.dust.core.internal.expression.antlr.DustExpressionsParser.UnaryPlusExpressionContext;
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
 * Visitor used to build up the AST of an expression.
 */
public class ExpressionVisitor
	extends DustExpressionsBaseListener
{
	private static final Node[] EMPTY_NODE_ARRAY = new Node[0];

	private final LinkedList<Node> nodeStack;

	public ExpressionVisitor()
	{
		nodeStack = new LinkedList<>();
	}

	public Node getRoot()
	{
		return nodeStack.removeLast();
	}

	@Override
	public void enterTrueKeyword(TrueKeywordContext ctx)
	{
		nodeStack.add(new KeywordNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			KeywordNode.Type.TRUE
		));
	}

	@Override
	public void enterFalseKeyword(FalseKeywordContext ctx)
	{
		nodeStack.add(new KeywordNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			KeywordNode.Type.FALSE
		));
	}

	@Override
	public void enterNullKeyword(NullKeywordContext ctx)
	{
		nodeStack.add(new KeywordNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			KeywordNode.Type.NULL
		));
	}

	@Override
	public void enterThisKeyword(ThisKeywordContext ctx)
	{
		nodeStack.add(new KeywordNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			KeywordNode.Type.THIS
		));
	}

	@Override
	public void enterLongConstant(LongConstantContext ctx)
	{
		long v = Long.parseLong(ctx.getText());
		nodeStack.add(new LongNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			v
		));
	}

	@Override
	public void enterDoubleConstant(DoubleConstantContext ctx)
	{
		double v = Double.parseDouble(ctx.getText());
		nodeStack.add(new DoubleNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			v
		));
	}

	@Override
	public void enterStringConstant(StringConstantContext ctx)
	{
		String v = ctx.getText();
		nodeStack.add(new StringNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			StringNode.decode(v.substring(1, v.length()-1))
		));
	}

	@Override
	public void enterNormalId(NormalIdContext ctx)
	{
		String v = ctx.getText();
		nodeStack.add(new IdentifierNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			null,
			v
		));
	}

	@Override
	public void enterNamespaceId(NamespaceIdContext ctx)
	{
		String v = ctx.getText();
		int idx = v.indexOf(':');
		nodeStack.add(new IdentifierNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			v.substring(0, idx),
			v.substring(idx+1)
		));
	}

	@Override
	public void exitInvokeEmpty(InvokeEmptyContext ctx)
	{
		IdentifierNode id = (IdentifierNode) nodeStack.removeLast();

		nodeStack.add(new InvokeNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			id,
			Collections.emptyList()
		));
	}

	@Override
	public void exitInvokeArguments(InvokeArgumentsContext ctx)
	{
		LinkedList<Node> params = new LinkedList<>();
		int expressionCount = ctx.methodExpressions().logicalExpression().size();
		for(int i=0; i<expressionCount; i++)
		{
			Node node = nodeStack.removeLast();
			params.addFirst(node);
		}

		IdentifierNode id = (IdentifierNode) nodeStack.removeLast();

		nodeStack.add(new InvokeNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			id,
			params
		));
	}

	@Override
	public void exitUnaryPlusExpression(UnaryPlusExpressionContext ctx)
	{
		Node child = nodeStack.removeLast();

		Node newNode = null;
		if(child instanceof LongNode || child instanceof DoubleNode)
		{
			// If the child is a static value optimize it away
			newNode = child;
		}
		else
		{
			newNode = new SignNode(
				ctx.getStart().getLine(),
				ctx.getStart().getCharPositionInLine(),
				false,
				child
			);
		}
		nodeStack.add(newNode);
	}

	@Override
	public void exitUnaryMinusExpression(UnaryMinusExpressionContext ctx)
	{
		Node child = nodeStack.removeLast();

		Node newNode = null;
		if(child instanceof LongNode)
		{
			// If the child is a static value optimize it away
			newNode = new LongNode(
				ctx.getStart().getLine(),
				ctx.getStart().getCharPositionInLine(),
				- ((LongNode) child).getValue()
			);
		}
		else if(child instanceof DoubleNode)
		{
			// If the child is a static value optimize it away
			newNode = new DoubleNode(
				ctx.getStart().getLine(),
				ctx.getStart().getCharPositionInLine(),
				- ((DoubleNode) child).getValue()
			);
		}
		else
		{
			newNode = new SignNode(
				ctx.getStart().getLine(),
				ctx.getStart().getCharPositionInLine(),
				true,
				child
			);
		}
		nodeStack.add(newNode);
	}

	@Override
	public void exitUnaryNotExpression(UnaryNotExpressionContext ctx)
	{
		nodeStack.add(new NegateNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			nodeStack.removeLast()
		));
	}

	@Override
	public void exitTernaryExpression(TernaryExpressionContext ctx)
	{
		Node right = ctx.right == null
			? null
			: nodeStack.removeLast();

		Node left = nodeStack.removeLast();
		Node test = nodeStack.removeLast();

		nodeStack.add(new TernaryNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			test,
			left,
			right
		));
	}

	@Override
	public void exitChainNormal(ChainNormalContext ctx)
	{
		Node right = nodeStack.removeLast();
		Node left = nodeStack.removeLast();
		nodeStack.add(new ChainNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			left,
			right
		));
	}

	@Override
	public void exitChainNullSafe(ChainNullSafeContext ctx)
	{
		// TODO
		super.exitChainNullSafe(ctx);
	}

	@Override
	public void exitEqualCompareExpression(EqualCompareExpressionContext ctx)
	{
		Node right = nodeStack.removeLast();
		Node left = nodeStack.removeLast();

		Node newNode = null;
		switch(ctx.op.getType())
		{
			case EQUAL:
				newNode = new EqualsNode(
					ctx.getStart().getLine(),
					ctx.getStart().getCharPositionInLine(),
					left,
					right
				);
				break;
			case NOT_EQUAL:
				newNode = new NotEqualsNode(
					ctx.getStart().getLine(),
					ctx.getStart().getCharPositionInLine(),
					left,
					right
				);
				break;
			default:
				throw new ExpressionException("Unknown token: " + ctx.op);
		}

		nodeStack.add(newNode);
	}

	@Override
	public void exitComparingExpression(ComparingExpressionContext ctx)
	{
		Node right = nodeStack.removeLast();
		Node left = nodeStack.removeLast();

		Node newNode = null;
		switch(ctx.op.getType())
		{
			case LESS:
				newNode = new LessNode(
					ctx.getStart().getLine(),
					ctx.getStart().getCharPositionInLine(),
					left,
					right
				);
				break;
			case LESS_OR_EQUAL:
				newNode = new LessOrEqualNode(
					ctx.getStart().getLine(),
					ctx.getStart().getCharPositionInLine(),
					left,
					right
				);
				break;
			case MORE_CMP:
				newNode = new GreaterNode(
					ctx.getStart().getLine(),
					ctx.getStart().getCharPositionInLine(),
					left,
					right
				);
				break;
			case MORE_OR_EQUAL:
				newNode = new GreaterOrEqualNode(
					ctx.getStart().getLine(),
					ctx.getStart().getCharPositionInLine(),
					left,
					right
				);
				break;
			default:
				throw new ExpressionException("Unknown token: " + ctx.op);
		}

		nodeStack.add(newNode);
	}

	@Override
	public void exitBooleanAndExpression(BooleanAndExpressionContext ctx)
	{
		Node right = nodeStack.removeLast();
		Node left = nodeStack.removeLast();

		nodeStack.add(new AndNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			left,
			right
		));
	}

	@Override
	public void exitBooleanOrExpression(BooleanOrExpressionContext ctx)
	{
		Node right = nodeStack.removeLast();
		Node left = nodeStack.removeLast();

		nodeStack.add(new OrNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			left,
			right
		));
	}

	@Override
	public void exitAdditiveExpression(AdditiveExpressionContext ctx)
	{
		Node right = nodeStack.removeLast();
		Node left = nodeStack.removeLast();

		Node newNode = null;
		switch(ctx.op.getType())
		{
			case PLUS:
				newNode = new AddNode(
					ctx.getStart().getLine(),
					ctx.getStart().getCharPositionInLine(),
					left,
					right
				);
				break;
			case MINUS:
				newNode = new SubtractNode(
					ctx.getStart().getLine(),
					ctx.getStart().getCharPositionInLine(),
					left,
					right
				);
				break;
			default:
				throw new ExpressionException("Unknown token: " + ctx.op);
		}

		nodeStack.add(newNode);
	}

	@Override
	public void exitMultiplicativeExpression(MultiplicativeExpressionContext ctx)
	{
		Node right = nodeStack.removeLast();
		Node left = nodeStack.removeLast();

		Node newNode = null;
		switch(ctx.op.getType())
		{
			case MULTIPLY:
				newNode = new MultiplyNode(
					ctx.getStart().getLine(),
					ctx.getStart().getCharPositionInLine(),
					left,
					right
				);
				break;
			case DIVIDE:
				newNode = new DivideNode(
					ctx.getStart().getLine(),
					ctx.getStart().getCharPositionInLine(),
					left,
					right
				);
				break;
			case MODULO:
				newNode = new ModuloNode(
					ctx.getStart().getLine(),
					ctx.getStart().getCharPositionInLine(),
					left,
					right
				);
				break;
			default:
				throw new ExpressionException("Unknown token: " + ctx.op);
		}

		nodeStack.add(newNode);
	}

	@Override
	public void exitArray(ArrayContext ctx)
	{
		if(ctx.args == null)
		{
			nodeStack.add(new ArrayNode(
				ctx.getStart().getLine(),
				ctx.getStart().getCharPositionInLine(),
				EMPTY_NODE_ARRAY
			));
			return;
		}

		int expressionCount = ctx.args.logicalExpression().size();
		Node[] values = new Node[expressionCount];
		for(int i=0; i<expressionCount; i++)
		{
			Node node = nodeStack.removeLast();
			values[expressionCount - i - 1] = node;
		}

		nodeStack.add(new ArrayNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			values
		));
	}

	@Override
	public void exitIndexedAccess(IndexedAccessContext ctx)
	{
		int expressionCount = ctx.expression().size();
		Node[] indexes = new Node[expressionCount];
		for(int i=0; i<expressionCount; i++)
		{
			Node node = nodeStack.removeLast();
			indexes[expressionCount - i - 1] = node;
		}

		Node left = nodeStack.removeLast();

		nodeStack.add(new IndexNode(
			ctx.getStart().getLine(),
			ctx.getStart().getCharPositionInLine(),
			left,
			indexes
		));
	}
}

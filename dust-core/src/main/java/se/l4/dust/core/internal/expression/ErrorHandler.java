package se.l4.dust.core.internal.expression;

import se.l4.dust.core.internal.expression.ast.Node;

public class ErrorHandler
{
	private final String expression;

	public ErrorHandler(String expression)
	{
		this.expression = expression;
	}
	
	public ExpressionException error(Node node, String message)
	{
		return new ExpressionException(expression, node.getLine(), node.getPositionInLine(), message);
	}
	
	public ExpressionException error(Node node, String message, Throwable cause)
	{
		return new ExpressionException(expression, node.getLine(), node.getPositionInLine(), message);
	}
}

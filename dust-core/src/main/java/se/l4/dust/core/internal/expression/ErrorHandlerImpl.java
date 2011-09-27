package se.l4.dust.core.internal.expression;

import se.l4.dust.api.expression.ExpressionException;
import se.l4.dust.core.internal.expression.ast.Node;

/**
 * Error handler for expressions. Used throughout the parse process to create
 * errors that are more developer friendly.
 * 
 * @author Andreas Holstenson
 *
 */
public class ErrorHandlerImpl
	implements ErrorHandler
{
	private final String expression;

	/**
	 * Create a new error handler.
	 * 
	 * @param expression
	 */
	public ErrorHandlerImpl(String expression)
	{
		this.expression = expression;
	}
	
	@Override
	public ExpressionException error(Node node, String message)
	{
		return new ExpressionException(expression, node.getLine(), node.getPositionInLine(), message);
	}
	
	@Override
	public ExpressionException error(Node node, String message, Throwable cause)
	{
		return new ExpressionException(expression, node.getLine(), node.getPositionInLine(), message);
	}
	
	@Override
	public ExpressionException error(Node node, Throwable cause)
	{
		String message;
		if(cause instanceof ExpressionException)
		{
			if(((ExpressionException) cause).hasSource())
			{
				throw (ExpressionException) cause;
			}
			
			message = cause.getMessage();
		}
		else
		{
			message = cause.getMessage() == null
				? "Failed with " + cause.getClass().getSimpleName()
				: cause.getClass().getSimpleName() + ": " + cause.getMessage();
		}
		
		return new ExpressionException(expression, node.getLine(), node.getPositionInLine(), message);
	}
	
	@Override
	public String toString()
	{
		return "ErrorHandler[" + expression + "]";
	}
}

package se.l4.dust.core.internal.expression;

/**
 * Exception that indicates that a parse error has occurred.
 * 
 * @author Andreas Holstenson
 *
 */
public class ExpressionParseException
	extends ExpressionException
{

	public ExpressionParseException(String source, int line, int position, String message)
	{
		super(source, line, position, message);
	}

}

package se.l4.dust.core.template;

public class ExpressionException
	extends RuntimeException
{

	public ExpressionException()
	{
		super();
	}

	public ExpressionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ExpressionException(String message)
	{
		super(message);
	}

	public ExpressionException(Throwable cause)
	{
		super(cause);
	}
	
}

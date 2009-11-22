package se.l4.dust.api;

public class ComponentException
	extends RuntimeException
{

	public ComponentException()
	{
		super();
	}

	public ComponentException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ComponentException(String message)
	{
		super(message);
	}

	public ComponentException(Throwable cause)
	{
		super(cause);
	}
	
}

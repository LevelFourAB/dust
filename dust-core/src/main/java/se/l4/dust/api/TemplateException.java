package se.l4.dust.api;

/**
 * Indicate that an error occurred while parsing or processing a template.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateException
	extends RuntimeException
{

	public TemplateException()
	{
		super();
	}

	public TemplateException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public TemplateException(String message)
	{
		super(message);
	}

	public TemplateException(Throwable cause)
	{
		super(cause);
	}
	
}

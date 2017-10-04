package se.l4.dust.api.template;

/**
 * Exception related to components, thrown when components are missing or
 * misbehaving.
 *
 * @author Andreas Holstenson
 *
 */
public class ComponentException
	extends TemplateException
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

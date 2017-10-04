package se.l4.dust.api.conversion;

/**
 * Runtime exception to indicate that an error occurred during a conversion.
 *
 * @author Andreas Holstenson
 *
 */
public class ConversionException
	extends RuntimeException
{
	public ConversionException()
	{
		super();
	}

	public ConversionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ConversionException(String message)
	{
		super(message);
	}

	public ConversionException(Throwable cause)
	{
		super(cause);
	}

}

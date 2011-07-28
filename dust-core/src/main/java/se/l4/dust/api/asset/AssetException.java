package se.l4.dust.api.asset;

/**
 * Exception thrown when loading of an asset fails.
 * 
 * @author Andreas Holstenson
 *
 */
public class AssetException
	extends RuntimeException
{

	public AssetException()
	{
		super();
	}

	public AssetException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public AssetException(String message)
	{
		super(message);
	}

	public AssetException(Throwable cause)
	{
		super(cause);
	}
	
}

package se.l4.dust.api.asset;

import se.l4.dust.api.resource.NamespaceLocation;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.resource.UrlLocation;

/**
 * Exception thrown when loading of an asset fails.
 *
 * @author Andreas Holstenson
 *
 */
public class AssetException
	extends RuntimeException
{
	private ResourceLocation location;

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

	public AssetException withLocation(ResourceLocation location)
	{
		this.location = location;
		return this;
	}

	public ResourceLocation getLocation()
	{
		return location;
	}

	@Override
	public String getMessage()
	{
		if(location == null)
		{
			return super.getMessage();
		}

		if(location instanceof NamespaceLocation)
		{
			NamespaceLocation nl = (NamespaceLocation) location;
			return nl.getName() + " in " + nl.getNamespace().getUri() + ": " + super.getMessage();
		}
		else if(location instanceof UrlLocation)
		{
			return location.getName() + ": " + super.getMessage();
		}

		return location + ": " + super.getMessage();
	}

}

package se.l4.dust.api.resource;

import java.io.IOException;
import java.io.InputStream;

import se.l4.dust.api.asset.AssetProcessor;

/**
 * A resource that is named, used by {@link AssetProcessor}s if they wish to
 * rename the resource as presented to the user.
 * 
 * @author Andreas Holstenson
 *
 */
public class NamedResource
	implements Resource
{
	private final Resource wrapped;
	private final String name;

	public NamedResource(Resource wrapped, String name)
	{
		this.wrapped = wrapped;
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}

	public String getContentType()
	{
		return wrapped.getContentType();
	}

	public int getContentLength()
	{
		return wrapped.getContentLength();
	}

	public String getContentEncoding()
	{
		return wrapped.getContentEncoding();
	}

	public long getLastModified()
	{
		return wrapped.getLastModified();
	}

	public InputStream openStream()
		throws IOException
	{
		return wrapped.openStream();
	}

}

package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.io.InputStream;

import se.l4.dust.api.resource.Resource;

public class CachedResource
	implements Resource
{
	private final CacheFormat format;

	public CachedResource(CacheFormat format)
	{
		this.format = format;
	}

	@Override
	public String getContentType()
	{
		return format.getContentType();
	}

	@Override
	public int getContentLength()
	{
		return format.getLength();
	}

	@Override
	public String getContentEncoding()
	{
		return format.getContentEncoding();
	}

	@Override
	public long getLastModified()
	{
		return format.getLastModified();
	}

	@Override
	public InputStream openStream()
		throws IOException
	{
		return format.openStream();
	}

}

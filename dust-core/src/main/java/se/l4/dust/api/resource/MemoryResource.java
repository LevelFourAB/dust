package se.l4.dust.api.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreams;

/**
 * Implementation of {@link Resource} that is stored entirely in memory.
 * 
 * @author Andreas Holstenson
 *
 */
public class MemoryResource
	implements Resource
{
	private final String contentType;
	private final String contentEncoding;
	private final byte[] data;
	private final long lastModified;

	public MemoryResource(String contentType, String contentEncoding, InputStream stream)
		throws IOException
	{
		this(contentType, contentEncoding, ByteStreams.toByteArray(stream));
	}
	
	public MemoryResource(String contentType, String contentEncoding, byte[] data)
		throws IOException
	{
		this.contentType = contentType;
		this.contentEncoding = contentEncoding;
		
		this.data = data;
		lastModified = System.currentTimeMillis();
	}
	
	public int getContentLength()
	{
		return data.length;
	}

	public String getContentType()
	{
		return contentType;
	}
	
	public String getContentEncoding()
	{
		return contentEncoding;
	}
	
	public long getLastModified()
	{
		return lastModified;
	}

	public InputStream openStream()
		throws IOException
	{
		return new ByteArrayInputStream(data);
	}

	@Override
	public String toString()
	{
		return "MemoryResource[type=" + contentType + ", length=" + data.length + "]";
	}
}

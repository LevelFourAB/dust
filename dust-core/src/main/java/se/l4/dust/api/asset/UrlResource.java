package se.l4.dust.api.asset;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Implementation of {@link Resource} built on top of {@link URL}. Uses the URL
 * to determine content type, length and encoding.
 * 
 * @author Andreas Holstenson
 *
 */
public class UrlResource
	implements Resource
{
	private final URL url;
	private final String contentType;
	private final int contentLength;
	private final String contentEncoding;
	private final long lastModified;

	public UrlResource(URL url)
		throws IOException
	{
		this.url = url;
		
		URLConnection conn = url.openConnection();
		try
		{
			conn.connect();
			
			contentLength = conn.getContentLength();
			contentType = conn.getContentType();
			contentEncoding = conn.getContentEncoding();
			lastModified = conn.getLastModified();
		}
		finally
		{
			conn.getInputStream().close();
		}
	}
	
	public String getContentType()
	{
		return contentType;
	}
	
	public int getContentLength()
	{
		return contentLength;
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
		return url.openStream();
	}

	@Override
	public String toString()
	{
		return "UrlResource[" + url + "]";
	}
}

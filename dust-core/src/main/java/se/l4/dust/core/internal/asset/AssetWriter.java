package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.DateUtil;
import org.jboss.resteasy.util.HttpHeaderNames;

import se.l4.dust.api.asset.Asset;

@Provider
@Produces("*/*")
public class AssetWriter
	implements MessageBodyWriter<Asset>
{
	private final String date;
	
	public AssetWriter()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, c.get(Calendar.YEAR) + 10);
		
		date = DateUtil.formatDate(c.getTime());
	}
	
	public long getSize(Asset t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType)
	{
		return -1;
	}

	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType)
	{
		return Asset.class.isAssignableFrom(type);
	}

	public void writeTo(Asset t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream)
		throws IOException, WebApplicationException
	{
		URL url = t.getURL();
		URLConnection connection = url.openConnection();
		
		httpHeaders.putSingle(HttpHeaderNames.EXPIRES, date);
		httpHeaders.putSingle(HttpHeaderNames.CACHE_CONTROL, "public");
		
		
		InputStream stream = null;
		try
		{
			stream = connection.getInputStream();
//			long lastModified = connection.getLastModified();
//			int length = connection.getContentLength();
//			
//			if(length != -1)
//			{
//				 httpHeaders.putSingle(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(length));
//			}
			
			byte[] buffer = new byte[1024];
			int l;
			while((l = stream.read(buffer)) != -1)
			{
				entityStream.write(buffer, 0, l);
			}
		}
		finally
		{
			if(stream != null)
			{
				stream.close();
			}
		}
	}

}

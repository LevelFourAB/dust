package se.l4.dust.jaxrs.internal.asset;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.resource.Resource;

import com.google.inject.Inject;
import com.google.inject.Stage;

/**
 * {@link MessageBodyWriter} for {@link Asset}.
 *
 * @author Andreas Holstenson
 */
@Provider
@Produces("*/*")
public class AssetWriter
	implements MessageBodyWriter<Asset>
{
	private final boolean development;
	private final long maxAge;

	@Inject
	public AssetWriter(Stage stage)
	{
		development = stage == Stage.DEVELOPMENT;

		long time = System.currentTimeMillis();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.YEAR, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);

		maxAge = c.getTimeInMillis() - time;
	}

	@Override
	public long getSize(Asset t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType)
	{
		Resource resource = t.getResource();
		return resource.getContentLength();
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType)
	{
		return Asset.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(Asset t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream)
		throws IOException, WebApplicationException
	{
		Resource resource = t.getResource();

		if(false == development)
		{
			httpHeaders.putSingle("Cache-Control", "public, max-age=" + maxAge);
			httpHeaders.putSingle("Date", new Date());
			httpHeaders.putSingle("Expires", new Date(System.currentTimeMillis() + maxAge));
		}

		String contentType = getMimeType(t);
		httpHeaders.putSingle("Last-Modified", new Date(resource.getLastModified()));

		if(contentType != null)
		{
			httpHeaders.putSingle("Content-Type", contentType);
		}

		InputStream stream = null;
		try
		{
			stream = resource.openStream();

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

	public static String getMimeType(Asset asset)
	{
		String contentType = asset.getResource().getContentType();
		if(contentType == null || "".equals(contentType) || "content/unknown".equals(contentType) || "unknown".equals(contentType))
		{
			contentType = getLazyMimeType(asset);
		}

		return contentType;
	}

	private static String getLazyMimeType(Asset asset)
	{
		String name = asset.getName();
		if(name.endsWith(".css"))
		{
			return "text/css";
		}
		else if(name.endsWith(".js"))
		{
			return "text/javascript";
		}
		else if(name.endsWith(".woff"))
		{
			return "application/x-font-woff";
		}
		else
		{
			return "application/octet-stream";
		}
	}
}

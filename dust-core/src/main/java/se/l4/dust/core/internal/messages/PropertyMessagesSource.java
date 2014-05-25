package se.l4.dust.core.internal.messages;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import se.l4.dust.api.Context;
import se.l4.dust.api.messages.MessageSource;
import se.l4.dust.api.messages.Messages;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.core.internal.Caches;

import com.google.common.base.Optional;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.inject.Inject;

/**
 * Source of property based messages.
 * 
 * @author Andreas Holstenson
 *
 */
public class PropertyMessagesSource
	implements MessageSource
{
	private static final Charset ISO88591 = Charset.forName("ISO-8859-1");
	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private final ResourceVariantManager variants;
	private final LoadingCache<String, Optional<Messages>> cache;

	@Inject
	public PropertyMessagesSource(Caches caches, ResourceVariantManager variants)
	{
		this.variants = variants;
		this.cache = caches.newLoadingCache(new CacheLoader<String, Optional<Messages>>() {
			@Override
			public Optional<Messages> load(String key)
				throws Exception
			{
				return Optional.of(loadUrl(key));
			}
		});
	}
	
	private boolean checkIfUrlExists(String url)
	{
		InputStream stream = null;
		try
		{
			stream = new URL(url).openStream();
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
		finally
		{
			Closeables.closeQuietly(stream);
		}
	}
	
	@Override
	public Messages load(Context context, Class<?> resource) throws IOException
	{
		URL url = resource.getResource(resource.getSimpleName() + ".properties");
		if(url == null)
		{
			return null;
		}
		
		return load(context, url.toString());
	}

	@Override
	public Messages load(Context context, String url)
		throws IOException
	{
		int idx = url.lastIndexOf('.');
		String firstPart = idx > 0 ? url.substring(0, idx) : url;
		
		url = firstPart + ".properties";
		ResourceVariantManager.Result result = variants.resolve(context, new ResourceVariantManager.ResourceCallback()
		{
			public boolean exists(ResourceVariant variant, String url)
				throws IOException
			{
				return checkIfUrlExists(url);
			}
		}, url);
		
		try
		{
			return cache.get(result.getUrl()).orNull();
		}
		catch(ExecutionException e)
		{
			throw new RuntimeException("Unable to load messages; " + e.getCause().getMessage(), e.getCause());
		}
	}
	
	private Messages loadUrl(String url)
		throws IOException
	{
		if(! checkIfUrlExists(url))
		{
			return null;
		}
		
		// Load the properties
		InputStream stream = new URL(url).openStream();
		try
		{
			Properties props = new Properties();
			props.load(stream);
			
			PropertyMessages messages = new PropertyMessages(props);
			return messages;
		}
		finally
		{
			Closeables.closeQuietly(stream);
		}
	}

	private static class PropertyMessages
		implements Messages
	{
		private final Map<String, String> messages;

		public PropertyMessages(Properties props)
		{
			Map<String, String> messages = Maps.newHashMap();
			for(Entry<Object, Object> o : props.entrySet())
			{
				messages.put((String) o.getKey(), new String(((String) o.getValue()).getBytes(ISO88591), UTF8));
			}
			this.messages = messages;
		}
		
		@Override
		public String get(String property)
		{
			return messages.get(property);
		}
	}
}

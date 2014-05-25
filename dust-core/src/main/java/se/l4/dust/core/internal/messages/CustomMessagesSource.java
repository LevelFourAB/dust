package se.l4.dust.core.internal.messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import se.l4.dust.api.Context;
import se.l4.dust.api.messages.MessageCollection;
import se.l4.dust.api.messages.MessageSource;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.core.internal.Caches;
import se.l4.dust.core.internal.messages.MessageInput.Token;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.inject.Inject;

public class CustomMessagesSource
	implements MessageSource
{
	private final ResourceVariantManager variants;
	private final LoadingCache<String, Optional<MessageCollection>> cache;

	@Inject
	public CustomMessagesSource(Caches caches, ResourceVariantManager variants)
	{
		this.variants = variants;
		this.cache = caches.newLoadingCache(new CacheLoader<String, Optional<MessageCollection>>() {
			@Override
			public Optional<MessageCollection> load(String key)
				throws Exception
			{
				return Optional.fromNullable(loadUrl(key));
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
	public MessageCollection load(Context context, Class<?> resource) throws IOException
	{
		URL url = resource.getResource(resource.getSimpleName() + ".messages");
		if(url == null)
		{
			return null;
		}
		
		return load(context, url.toString());
	}

	@Override
	public MessageCollection load(Context context, String url)
		throws IOException
	{
		int idx = url.lastIndexOf('.');
		String firstPart = idx > 0 ? url.substring(0, idx) : url;
		
		url = firstPart + ".messages";
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
	
	private MessageCollection loadUrl(String url)
		throws IOException
	{
		if(! checkIfUrlExists(url))
		{
			return null;
		}
		
		// Load the properties
		InputStream stream = new URL(url).openStream();
		InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);
		try
		{
			Map<String, String> result = Maps.newHashMap();
			MessageInput in = new MessageInput(reader);
			while(in.peek() != null)
			{
				Token token = in.next(Token.KEY);
				String key = in.getString();
				
				token = in.next();
				switch(token)
				{
					case OBJECT_START:
						readObject(in, result, key);
						break;
					case VALUE:
						result.put(key, in.getString());
						break;
				}
			}
			
			return new Collection(result);
		}
		finally
		{
			Closeables.closeQuietly(reader);;
			Closeables.closeQuietly(stream);
		}
	}

	private void readObject(MessageInput in, Map<String, String> result, String parent)
		throws IOException
	{
		while(in.peek() != Token.OBJECT_END)
		{
			in.next(Token.KEY);
			String key = parent + "." + in.getString();
			
			switch(in.next())
			{
				case OBJECT_START:
					readObject(in, result, key);
					break;
				case VALUE:
					result.put(key, in.getString());
					break;
			}
		}
		
		in.next(Token.OBJECT_END);
	}
	
	private static class Collection
		implements MessageCollection
	{
		private final Map<String, String> messages;

		public Collection(Map<String, String> messages)
		{
			this.messages = messages;
		}
		
		@Override
		public String get(String property)
		{
			return messages.get(property);
		}
	}
}

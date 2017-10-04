package se.l4.dust.core.internal.messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.inject.Inject;

import se.l4.dust.api.Context;
import se.l4.dust.api.messages.MessageCollection;
import se.l4.dust.api.messages.MessageSource;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.resource.variant.ResourceVariantResolution;
import se.l4.dust.core.internal.Caches;
import se.l4.dust.core.internal.messages.MessageInput.Token;

public class CustomMessagesSource
	implements MessageSource
{
	private final ResourceVariantManager variants;
	private final Cache<ResourceLocation, MessageCollection> cache;

	@Inject
	public CustomMessagesSource(Caches caches, ResourceVariantManager variants)
	{
		this.variants = variants;
		this.cache = caches.newCache();
	}

	@Override
	public MessageCollection load(Context context, ResourceLocation resource)
		throws IOException
	{
		ResourceLocation location = resource.withExtension("messages");
		ResourceVariantResolution variant = variants.resolve(context, location);
		if(variant.getResource() == null) return null;

		Resource toLoad = variant.getResource();
		MessageCollection collection = cache.getIfPresent(toLoad.getLocation());
		if(collection != null) return collection;

		MessageCollection result = load(toLoad);
		cache.put(toLoad.getLocation(), result);
		return result;
	}

	private MessageCollection load(Resource resource)
		throws IOException
	{
		// Load the properties
		InputStream stream = resource.openStream();
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

		@Override
		public Set<String> keys()
		{
			return messages.keySet();
		}
	}
}

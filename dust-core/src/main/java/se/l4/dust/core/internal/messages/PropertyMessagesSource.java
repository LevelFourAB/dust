package se.l4.dust.core.internal.messages;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

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
	private final Cache<ResourceLocation, MessageCollection> cache;

	@Inject
	public PropertyMessagesSource(Caches caches, ResourceVariantManager variants)
	{
		this.variants = variants;
		this.cache = caches.newCache();
	}

	@Override
	public MessageCollection load(Context context, ResourceLocation resource)
		throws IOException
	{
		ResourceLocation location = resource.withExtension("properties");
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
		implements MessageCollection
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

		@Override
		public Set<String> keys()
		{
			return messages.keySet();
		}
	}
}

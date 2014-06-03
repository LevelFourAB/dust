package se.l4.dust.core.internal.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.l4.dust.api.Context;
import se.l4.dust.api.Namespace;
import se.l4.dust.api.resource.MergedResource;
import se.l4.dust.api.resource.NamespaceLocation;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.resource.Resources;
import se.l4.dust.api.resource.UrlLocation;
import se.l4.dust.api.resource.UrlResource;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.resource.variant.ResourceVariantResolution;
import se.l4.dust.api.resource.variant.ResourceVariantSource;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Implementation of {@link ResourceVariantManager}.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ResourceVariantManagerImpl
	implements ResourceVariantManager
{
	private volatile ResourceVariantSource[] sources;
	private final Lock sourceLock;
	private final Map<Key, Value> cache;
	private ResourceCallback urlCallback;
	
	@Inject
	public ResourceVariantManagerImpl(final Resources resources)
	{
		sources = new ResourceVariantSource[0];
		sourceLock = new ReentrantLock();
		
		cache = new ConcurrentHashMap<Key, Value>();
		
		urlCallback = new ResourceCallback()
		{
			@Override
			public boolean exists(ResourceVariant variant, String url)
				throws IOException
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
			public Resource create(String name)
				throws IOException
			{
				URL url = new URL(name);
				try
				{
					return new UrlResource(new UrlLocation(url), url);
				}
				catch(IOException e)
				{
					return null;
				}
			}
		};
	}
	
	@Override
	public void addSource(ResourceVariantSource source)
	{
		sourceLock.lock();
		try
		{
			ResourceVariantSource[] result = new ResourceVariantSource[sources.length + 1];
			System.arraycopy(sources, 0, result, 0, sources.length);
			result[sources.length] = source;
			
			sources = result;
		}
		finally
		{
			sourceLock.unlock();
		}
	}

	@Override
	public List<ResourceVariant> getVariants(Context context)
	{
		ResourceVariantSource[] sources = this.sources;
		
		@SuppressWarnings("unchecked")
		List<ResourceVariant>[] perSource = new List[sources.length];
		
		for(int i=0, n=sources.length; i<n; i++)
		{
			ResourceVariantSource source = sources[i];
			perSource[i] = source.getVariants(context);
		}
		
		ResourceVariant[] empty = new ResourceVariant[0];
		
		List<ResourceVariant> result = new ArrayList<ResourceVariant>(sources.length * sources.length); 

		for(int i=0, n=sources.length; i<n; i++)
		{
			// Go through each initial source creating its chained variants
			combine(result, perSource, i, empty);
		}
		
		return result;
	}

	private void combine(List<ResourceVariant> result, List<ResourceVariant>[] perSource,
			int index, ResourceVariant[] path)
	{
		for(ResourceVariant v : perSource[index])
		{
			ResourceVariant[] combined = Arrays.copyOf(path, path.length + 1);
			combined[path.length] = v;
			
			result.add(new MergedResourceVariant(combined));
			
			if(index < perSource.length - 1)
			{
				combine(result, perSource, index + 1, combined);
			}
		}
	}

	@Override
	public List<Context> getInitialContexts()
	{
		List<Context> result = new ArrayList<Context>();
		result.add(new CacheContext());
		result.add(new CacheContext().withValue(ResourceVariant.LOCALE, Locale.getDefault()));
		
		return result;
	}
	
	@Override
	public Object[] getCacheObject(Context context)
	{
		ResourceVariantSource[] sources = this.sources;
		Object[] values = new Object[sources.length];
		for(int i=0, n=sources.length; i<n; i++)
		{
			values[i] = sources[i].getCacheValue(context);
		}
		
		return values;
	}
	
	@Override
	public ResourceVariantResolution resolveNoCache(Context context, ResourceLocation location)
		throws IOException
	{
		String original = getInitialName(location);
		ResourceCallback callback = getCallback(location);
		
		// Try different variants for the URL
		int idx = original.lastIndexOf('.');
		String extension = idx > 0 ? original.substring(idx) : "";
		String firstPart = idx > 0 ? original.substring(0, idx) : original;
		
		for(ResourceVariant v : getVariants(context))
		{
			String variant = firstPart + "." + v.getIdentifier() + extension;
			
			if(callback.exists(v, variant))
			{
				// This URL exists, use it
				return new Value(callback.create(variant), variant, v);
			}
		}
		
		return new Value(callback.create(original), original, null);
	}
	
	@Override
	public ResourceVariantResolution resolve(Context context, ResourceLocation location)
		throws IOException
	{
		ResourceVariantSource[] sources = this.sources;
		Object[] values = new Object[sources.length];
		for(int i=0, n=sources.length; i<n; i++)
		{
			values[i] = sources[i].getCacheValue(context);
		}
		
		Key key = new Key(location, values);
		
		// Check if this is cached
		Value cached = cache.get(key);
		if(cached != null) return cached;
		
		String original = getInitialName(location);
		ResourceCallback callback = getCallback(location);
		
		// Try different variants for the URL
		int idx = original.lastIndexOf('.');
		String extension = idx > 0 ? original.substring(idx) : "";
		String firstPart = idx > 0 ? original.substring(0, idx) : original;
		
		for(ResourceVariant v : getVariants(context))
		{
			String variant = firstPart + "." + v.getIdentifier() + extension;
			
			if(callback.exists(v, variant))
			{
				// This URL exists, use it
				Value result = new Value(callback.create(variant), variant, v);
				cache.put(key, result);
				return result;
			}
		}
		
		// No variant found, use original
		Value result = new Value(callback.create(original), original, null);
		cache.put(key, result);
		return result;
	}
	
	@Override
	public ResourceVariantResolution createCombined(List<ResourceVariantResolution> result, ResourceLocation location)
	{
		Map<Class<? extends ResourceVariant>, ResourceVariant> variants = Maps.newHashMap();
		for(ResourceVariantResolution r : result)
		{
			ResourceVariant v = r.getVariant();
			if(v == null) continue;
			
			ResourceVariant current = variants.get(v.getClass());
			if(current == null || v.isMoreSpecific(current))
			{
				variants.put(v.getClass(), v);
			}
		}

		ResourceVariant variant = null;
		if(! variants.isEmpty())
		{
			if(variants.size() == 1)
			{
				variant = variants.values().iterator().next();
			}
			else
			{
				ResourceVariant[] variantArray = new ResourceVariant[variants.size()];
				int idx = 0;
				for(ResourceVariantSource source : sources)
				{
					ResourceVariant rv = variants.get(source.getVariantClass());
					if(rv != null)
					{
						variantArray[idx++] = rv;
					}
				}
				
				variant = new MergedResourceVariant(variantArray);
			}
			
			String name = getInitialName(location);
			int idx = name.lastIndexOf('.');
			String extension = idx > 0 ? name.substring(idx) : "";
			
			location = location.withExtension(variant.getIdentifier() + extension);
		}
		
		MergedResource resource = new MergedResource(location, Lists.transform(result, new Function<ResourceVariantResolution, Resource>()
		{
			@Override
			public Resource apply(ResourceVariantResolution input)
			{
				return input.getResource();
			}
		}));
		return new Value(resource, location.getName(), variant);
	}
	
	@Override
	public ResourceVariantResolution createCombined(Context context, ResourceLocation location, Supplier<List<ResourceVariantResolution>> resultSupplier)
	{
		Key key = new Key(location, getCacheObject(context));
		Value result = cache.get(key);
		if(result != null) return result;

		result = (Value) createCombined(resultSupplier.get(), location);
		cache.put(key, result);
		return result;
	}
	
	private ResourceCallback getCallback(ResourceLocation location)
	{
		if(location instanceof UrlLocation)
		{
			return urlCallback;
		}
		else if(location instanceof NamespaceLocation)
		{
			return new NamespaceCallback(((NamespaceLocation) location).getNamespace());
		}
		else
		{
			throw new AssertionError("Unsupported location: " + location);
		}
	}
	
	private String getInitialName(ResourceLocation location)
	{
		if(location instanceof UrlLocation)
		{
			return ((UrlLocation) location).getUrl().toString();
		}
		else if(location instanceof NamespaceLocation)
		{
			return ((NamespaceLocation) location).getName();
		}
		else
		{
			throw new AssertionError("Unsupported location: " + location);
		}
	}
	
	private static class Value
		implements ResourceVariantResolution
	{
		private final Resource resource;
		private final String path;
		private final ResourceVariant finalVariant;

		public Value(Resource resource, String path, ResourceVariant finalVariant)
		{
			this.resource = resource;
			this.path = path;
			this.finalVariant = finalVariant;
		}
		
		@Override
		public Resource getResource()
		{
			return resource;
		}
		
		@Override
		public String getName()
		{
			return path;
		}

		@Override
		public ResourceVariant getVariant()
		{
			return finalVariant;
		}
		
		@Override
		public String toString()
		{
			return getClass().getSimpleName() + "{name=" + path + ", variant=" + finalVariant + ", resource=" + resource + "}";
		}
	}
	
	private static class Key
	{
		private final ResourceLocation location;
		private final Object[] values;
		
		public Key(ResourceLocation location, Object[] values)
		{
			this.location = location;
			this.values = values;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((location == null) ? 0 : location.hashCode());
			result = prime * result + Arrays.hashCode(values);
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if(location == null)
			{
				if(other.location != null)
					return false;
			}
			else if(!location.equals(other.location))
				return false;
			if(!Arrays.equals(values, other.values))
				return false;
			return true;
		}
	}
	
	private static class CacheContext
		implements Context
	{
		private HashMap<Object, Object> values;

		public CacheContext()
		{
			values = new HashMap<Object, Object>();
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <T> T getValue(Object key)
		{
			return (T) values.get(key);
		}
		
		@Override
		public void putValue(Object key, Object value)
		{
			values.put(key, value);
		}
		
		public CacheContext withValue(Object key, Object value)
		{
			values.put(key, value);
			
			return this;
		}
	}
	
	interface ResourceCallback
	{
		boolean exists(ResourceVariant variant, String name)
			throws IOException;
		
		Resource create(String name)
			throws IOException;
	}
	
	private static class NamespaceCallback
		implements ResourceCallback
	{
		private final Namespace namespace;

		public NamespaceCallback(Namespace namespace)
		{
			this.namespace = namespace;
		}
		
		@Override
		public Resource create(String name)
			throws IOException
		{
			return namespace.getResource(name);
		}
		
		@Override
		public boolean exists(ResourceVariant variant, String name)
			throws IOException
		{
			return namespace.getResource(name) != null;
		}
	}
}

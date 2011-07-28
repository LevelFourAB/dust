package se.l4.dust.core.internal.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.inject.Singleton;

import se.l4.dust.api.Context;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.resource.variant.ResourceVariantSource;

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
	
	public ResourceVariantManagerImpl()
	{
		sources = new ResourceVariantSource[0];
		sourceLock = new ReentrantLock();
		
		cache = new ConcurrentHashMap<Key, Value>();
	}
	
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

	public List<Context> getInitialContexts()
	{
		List<Context> result = new ArrayList<Context>();
		result.add(new CacheContext());
		result.add(new CacheContext().withValue(ResourceVariant.LOCALE, Locale.getDefault()));
		
		return result;
	}
	
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
	
	public String resolveNoCache(Context context, ResourceCallback callback,
			String original)
		throws IOException
	{
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
				return variant;
			}
		}
		
		return original;
	}

	public String resolve(Context context, ResourceCallback callback, String original)
		throws IOException
	{
		ResourceVariantSource[] sources = this.sources;
		Object[] values = new Object[sources.length];
		for(int i=0, n=sources.length; i<n; i++)
		{
			values[i] = sources[i].getCacheValue(context);
		}
		
		Key key = new Key(original, values);
		
		// Check if this is cached
		Value cached = cache.get(key);
		if(cached != null) return cached.getPath();
		
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
				cache.put(key, new Value(variant, v));
				return variant;
			}
		}
		
		// No variant found, use original
		cache.put(key, new Value(original, null));
		return original;
	}
	
	public String resolve(ResourceVariant v, ResourceCallback callback, String original)
		throws IOException
	{
		return resolve0(v, callback, original).path;
	}
	
	public ResourceVariant resolveRealVariant(ResourceVariant v,
			ResourceCallback callback, String original)
		throws IOException
	{
		return resolve0(v, callback, original).finalVariant;
	}
	
	public Value resolve0(ResourceVariant v, ResourceCallback callback, String original)
		throws IOException
	{
		Object object = v.getCacheValue();
		
		Key key = new Key(original, 
			object instanceof Object[] 
				? (Object[]) object 
				: new Object[] { object }
			);
		
		// Check if this is cached
		Value cached = cache.get(key);
		if(cached != null) return cached;
		
		// Try different variants for the URL
		int idx = original.lastIndexOf('.');
		String extension = idx > 0 ? original.substring(idx) : "";
		String firstPart = idx > 0 ? original.substring(0, idx) : original;
		
		String variant = firstPart + "." + v.getIdentifier() + extension;
		
		if(callback.exists(v, variant))
		{
			// This URL exists, use it
			Value value = new Value(variant, v);
			cache.put(key, value);
			return value;
		}
		
		// No variant found, use original
		Value value = new Value(original, null);
		cache.put(key, value);
		return value;
	}
	
	private static class Value
	{
		private final String path;
		private final ResourceVariant finalVariant;

		public Value(String path, ResourceVariant finalVariant)
		{
			this.path = path;
			this.finalVariant = finalVariant;
		}
		
		public String getPath()
		{
			return path;
		}

		public ResourceVariant getFinalVariant()
		{
			return finalVariant;
		}
	}
	
	private static class Key
	{
		private final String url;
		private final Object[] values;
		
		public Key(String url, Object[] values)
		{
			this.url = url;
			this.values = values;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((url == null)
				? 0
				: url.hashCode());
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
			if(url == null)
			{
				if(other.url != null)
					return false;
			}
			else if(!url.equals(other.url))
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
		
		public <T> T getValue(Object key)
		{
			return (T) values.get(key);
		}
		
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
}

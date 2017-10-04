package se.l4.dust.core.internal.asset;

import java.io.IOException;

import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.ResourceLocator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BuiltAssetLocator
	implements ResourceLocator
{
	private final Cache<Key, Resource> registered;

	@Inject
	public BuiltAssetLocator()
	{
		registered = CacheBuilder.newBuilder().build();
	}

	@Override
	public Resource locate(String ns, String pathToFile)
		throws IOException
	{
		return registered.getIfPresent(new Key(ns, pathToFile));
	}

	public void add(String ns, String path, Resource resource)
	{
		registered.put(new Key(ns, path), resource);
	}

	public boolean contains(String ns, String path)
	{
		return registered.getIfPresent(new Key(ns, path)) != null;
	}

	private static class Key
	{
		private final String namespace;
		private final String path;

		public Key(String ns, String path)
		{
			this.namespace = ns;
			this.path = path;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((namespace == null) ? 0 : namespace.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
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
			if(namespace == null)
			{
				if(other.namespace != null)
					return false;
			}
			else if(!namespace.equals(other.namespace))
				return false;
			if(path == null)
			{
				if(other.path != null)
					return false;
			}
			else if(!path.equals(other.path))
				return false;
			return true;
		}
	}
}

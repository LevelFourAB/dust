package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.io.InputStream;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.NamespaceManager.Namespace;
import se.l4.dust.api.asset.AssetCache;
import se.l4.dust.api.asset.AssetEncounter;
import se.l4.dust.api.asset.AssetException;
import se.l4.dust.api.resource.Resource;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.Closeables;

/**
 * Implementation of {@link AssetEncounter}.
 * 
 * @author Andreas Holstenson
 *
 */
public class AssetEncounterImpl
	implements AssetEncounter
{
	private final NamespaceManager namespaces;
	private final boolean production;
	private final Resource in;
	private final String namespace;
	private final String path;
	
	private final AssetCache cache;
	private String cacheKey;
	
	private Resource replacedWith;
	private String renamedTo;

	public AssetEncounterImpl(
			NamespaceManager namespaces, 
			boolean production,
			Resource in, 
			String namespace, 
			String path,
			AssetCache cache)
	{
		this.namespaces = namespaces;
		this.production = production;
		this.in = in;
		this.namespace = namespace;
		this.path = path;
		this.cache = cache;
	}

	@Override
	public Resource getResource()
	{
		return in;
	}

	@Override
	public String getNamepace()
	{
		return namespace;
	}

	@Override
	public Namespace getNamespaceObject()
	{
		return namespaces.getNamespaceByURI(namespace);
	}

	@Override
	public String getPath()
	{
		return path;
	}
	
	@Override
	public boolean isProduction()
	{
		return production;
	}

	@Override
	public AssetEncounter replaceWith(Resource resource)
	{
		replacedWith = resource;
		
		return this;
	}

	@Override
	public AssetEncounter rename(String name)
	{
		renamedTo = name;
		
		return this;
	}
	
	@Override
	public AssetEncounter cache(String id, Resource resource)
	{
		if(cache == null) return this;
		
		cacheKey = id + "/" + hashInput();
		try
		{
			CacheFormat.store(resource, cache, cacheKey);
		}
		catch(IOException e)
		{
			throw new AssetException("Unable to cache " + in + "; " + e.getMessage(), e);
		}
		
		return this;
	}
	
	@Override
	public Resource getCached(String id)
	{
		if(cache == null) return null;
		
		String cacheKey = id + "/" + hashInput();
		try
		{
			CacheFormat format = CacheFormat.fromCachedStream(cache, cacheKey);
			return format == null ? null : new CachedResource(format);
		}
		catch(IOException e)
		{
			throw new AssetException("Unable to use cache " + in + "; " + e.getMessage(), e);
		}
	}
	
	private String hashInput()
	{
		Hasher hasher = Hashing.murmur3_128().newHasher();
		InputStream input = null;
		try
		{
			input = in.openStream();
			byte[] buf = new byte[4096];
			int len;
			while((len = input.read(buf)) != -1)
			{
				hasher.putBytes(buf, 0, len);
			}
			
			return hasher.hash().toString();
		}
		catch(IOException e)
		{
			throw new AssetException("Unable to cache " + in + "; " + e.getMessage(), e);
		}
		finally
		{
			Closeables.closeQuietly(input);
		}
	}

	public Resource getReplacedWith()
	{
		return replacedWith;
	}
	
	public String getRenamedTo()
	{
		return renamedTo;
	}

	public boolean isRenamed()
	{
		return renamedTo != null;
	}

	public boolean isReplaced()
	{
		return replacedWith != null;
	}
}

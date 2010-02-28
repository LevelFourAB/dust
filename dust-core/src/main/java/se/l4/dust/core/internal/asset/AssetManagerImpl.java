package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

import org.jdom.Namespace;

import se.l4.crayon.Environment;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.asset.AssetSource;
import se.l4.dust.api.asset.Resource;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.internal.ComputationException;

@Singleton
public class AssetManagerImpl
	implements AssetManager
{
	private static final Asset NULL_ASSET = new AssetImpl(null, false, null, null, null);
	
	private final ConcurrentMap<Namespace, AssetNamespace> cache;
	private final NamespaceManager manager;
	private final List<Object> sources;
	private final Set<String> protectedExtensions;
	private final Injector injector;
	
	@Inject
	public AssetManagerImpl(NamespaceManager manager, Injector injector,
			Environment env)
	{
		this.manager = manager;
		this.injector = injector;
		sources = new CopyOnWriteArrayList<Object>();
		
		Function<Namespace, AssetNamespace> f;
		if(env == Environment.DEVELOPMENT)
		{
			f = new Function<Namespace, AssetNamespace>()
				{
					public AssetNamespace apply(Namespace from)
					{
						return new DevAssetNamespace(from);
					}
				};
		}
		else
		{
			f = new Function<Namespace, AssetNamespace>()
				{
					public AssetNamespace apply(Namespace from)
					{
						return new AssetNamespace(from);
					}
				};
		}
		
		cache = new MapMaker().makeComputingMap(f);
		
		protectedExtensions = new CopyOnWriteArraySet<String>();
	}
	
	public void addSource(AssetSource source)
	{
		sources.add(source);
	}
	
	public void addSource(Class<? extends AssetSource> source)
	{
		sources.add(source);
	}
	
	public Asset locate(Namespace ns, String file)
	{
		AssetNamespace ans = cache.get(ns);
		Asset a = ans.get(file);
		
		return a == NULL_ASSET ? null: a;
	}

	public void addProtectedExtension(String extension)
	{
		protectedExtensions.add(extension);
	}
	
	public boolean isProtectedExtension(String extension)
	{
		return protectedExtensions.contains(extension);
	}
	
	public void addTemporaryAsset(Namespace ns, String path, Resource resource)
	{
//		Asset asset = locate(ns, path);
//		if(asset != null)
//		{
//			throw new IllegalArgumentException("Asset " + path 
//				+ " already exists in " + ns + ", can't add temporary resource");
//		}
		
		AssetNamespace ans = cache.get(ns);
		ans.set(path, resource);
	}
	
	public void processAssets(Namespace namespace, String filter, Class<? extends AssetProcessor>... processors)
	{
		AssetNamespace ans = cache.get(namespace);
		ans.addProcessors(filter, processors);
	}
	
	/**
	 * Default encapsulation of a namespace associated with assets. This class
	 * handles retrieval and creation of assets.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	private class AssetNamespace
	{
		private final ConcurrentMap<String, Asset> cache;
		private final List<ProcessorDef> processors;
		protected final Namespace namespace;
		
		public AssetNamespace(Namespace namespace)
		{
			this.namespace = namespace;
			
			cache = new MapMaker().makeComputingMap(new AssetLocator(this));
			processors = new CopyOnWriteArrayList<ProcessorDef>();
		}
		
		public void addProcessors(String filter, Class<? extends AssetProcessor>... classes)
		{
			processors.add(new ProcessorDef(filter, classes));
		}
		
		public Asset get(String path)
		{
			if(path == null)
			{
				throw new IllegalArgumentException("Path can not be null");
			}
			
			Asset asset = cache.get(path);
			return asset == NULL_ASSET ? null : asset;
		}
		
		public void set(String path, Resource resource)
		{
			Asset asset = new AssetImpl(manager, false, namespace, path, resource);
			cache.put(path, asset);
		}
		
		public void set(String path, Asset asset)
		{
			cache.put(path, asset);
		}
		
		public Asset createAsset(String path)
			throws IOException
		{
			int idx = path.lastIndexOf('.');
			String extension = idx > 0 ? path.substring(idx+1) : "";
			boolean protect = isProtectedExtension(extension);
			
			Resource resource = locate(path);
			if(resource != null)
			{
				boolean processed = false;
				Resource current = resource;
				
				// Check if we have any filters that need to be applied
				for(ProcessorDef def : processors)
				{
					if(def.matches(path))
					{
						if(false == processed)
						{
							processed = true;
						}
						
						current = def.filter(namespace, path, current); 
					}
				}
				
				// Return the asset
				return new AssetImpl(manager, protect, namespace, path, current);
			}
			
			return null;
		}
		
		public Resource locate(String path)
			throws IOException
		{
			for(Object source : sources)
			{
				AssetSource s = source instanceof AssetSource
					? (AssetSource) source
					: (AssetSource) injector.getInstance((Class<?>) source);
				
				Resource resource = s.locate(namespace, path);
				if(resource != null)
				{
					return resource;
				}
			}
			
			return null;
		}
	}
	
	/**
	 * Asset namespace that checks if the asset has been modified and if so
	 * reloads it.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	private class DevAssetNamespace
		extends AssetNamespace
	{
		public DevAssetNamespace(Namespace namespace)
		{
			super(namespace);
		}
		
		@Override
		public Asset get(String path)
		{
			Asset asset = super.get(path);
			if(asset == null)
			{
				return null;
			}
			
			// We check the last modified times and recreate the asset if necessary
			Resource resource = asset.getResource();
			long lastModified = resource.getLastModified();
			
			try
			{
				Resource original = locate(path);
				if(original != null && original.getLastModified() > lastModified)
				{
					asset = createAsset(path);
					set(path, asset);
				}
			}
			catch(IOException e)
			{
				throw new ComputationException(e);
			}
			
			return asset;
		}
	}
	
	/**
	 * Definition of a processor as seen by an asset namespace.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	private class ProcessorDef
	{
		private Class<? extends AssetProcessor>[] classes;
		private Pattern filter;
		
		public ProcessorDef(String filter, Class<? extends AssetProcessor>[] classes)
		{
			this.filter = Pattern.compile(filter);
			this.classes = classes;
		}
		
		public boolean matches(String path)
		{
			return filter.matcher(path).matches();
		}
		
		public Resource filter(Namespace ns, String path, Resource in)
			throws IOException
		{
			for(Class<? extends AssetProcessor> type : classes)
			{
				AssetProcessor instance = injector.getInstance(type);
				
				Resource out = instance.process(ns, path, in);
				
				in = out;
			}
			
			return in;
		}
	}
	
	/** 
	 * Locator function used with the computation map to create assets on
	 * the fly.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	private class AssetLocator
		implements Function<String, Asset>
	{
		private final AssetNamespace namespace;

		public AssetLocator(AssetNamespace namespace)
		{
			this.namespace = namespace;
		}
		
		public Asset apply(String from)
		{
			Asset asset;
			try
			{
				asset = namespace.createAsset(from);
			}
			catch(IOException e)
			{
				throw new ComputationException(e);
			}
			
			return asset == null ? NULL_ASSET : asset;
		}
	}
}

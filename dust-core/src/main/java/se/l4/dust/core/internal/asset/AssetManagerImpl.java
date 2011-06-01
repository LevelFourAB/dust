package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

import org.jdom.Namespace;

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import se.l4.crayon.Environment;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.asset.AssetSource;
import se.l4.dust.api.resource.MergedResource;
import se.l4.dust.api.resource.NamedResource;
import se.l4.dust.api.resource.Resource;

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
	private final Map<String, AssetProcessor> extensionProcessors;
	
	@Inject
	public AssetManagerImpl(NamespaceManager manager, Injector injector,
			Environment env)
	{
		this.manager = manager;
		this.injector = injector;
		sources = new CopyOnWriteArrayList<Object>();
		extensionProcessors = new ConcurrentHashMap<String, AssetProcessor>();
		
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
	
	public void processAssets(Namespace namespace, String filter,
			Class<? extends AssetProcessor> processor, 
			Object... arguments)
	{
		AssetNamespace ans = cache.get(namespace);
		ans.addProcessor(filter, processor, arguments);
	}
	
	public AssetBuilder addAsset(Namespace namespace, String pathToFile)
	{
		AssetNamespace parent = cache.get(namespace);
		return new AssetBuilderImpl(parent, namespace, pathToFile);
	}
	
	/**
	 * Builder implementation for defining combined assets.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	private class AssetBuilderImpl
		implements AssetBuilder
	{
		private final AssetNamespace parent;
		
		private final Namespace namespace;
		private final String pathToFile;
		
		private final List<Asset> assets;
		private final List<ProcessorDef> processors;

		public AssetBuilderImpl(AssetNamespace parent, Namespace namespace, String pathToFile)
		{
			this.parent = parent;
			this.namespace = namespace;
			this.pathToFile = pathToFile;
			
			assets = new ArrayList<Asset>();
			processors = new ArrayList<ProcessorDef>();
		}

		public AssetBuilder add(String pathToFile)
		{
			return add(namespace, pathToFile);
		}

		public AssetBuilder add(Namespace ns, String pathToFile)
		{
			Asset asset = locate(ns, pathToFile);
			if(asset == null)
			{
				throw new IllegalArgumentException("No asset named " + pathToFile + " found in " + ns);
			}
			
			assets.add(asset);
			
			return this;
		}

		public AssetBuilder process(Class<? extends AssetProcessor> processor, Object... args)
		{
			processors.add(new ProcessorDef(Pattern.quote(pathToFile), new Class[] { processor }, args));
			
			return this;
		}

		public void create()
		{
			parent.defineResource(pathToFile, assets, processors);
		}
		
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
		protected final List<ProcessorDef> processors;
		protected final ConcurrentMap<String, Resource> definedResources;
		
		protected final Namespace namespace;
		
		public AssetNamespace(Namespace namespace)
		{
			this.namespace = namespace;
			
			cache = new MapMaker().makeComputingMap(new AssetLocator(this));
			processors = new CopyOnWriteArrayList<ProcessorDef>();
			definedResources = new ConcurrentHashMap<String, Resource>();
		}
		
		/**
		 * Define a new resource for this namespace.
		 * 
		 * @param pathToFile
		 * @param assets
		 * @param processors
		 */
		public void defineResource(String pathToFile, List<Asset> assets, List<ProcessorDef> processors)
		{
			this.processors.addAll(processors);
				
			Resource[] resources = new Resource[assets.size()];
			for(int i=0, n=assets.size(); i<n; i++)
			{
				resources[i] = assets.get(i).getResource();
			}
			
			definedResources.put(pathToFile, new MergedResource(resources));
		}

		public void addProcessors(String filter, Class<? extends AssetProcessor>... classes)
		{
			processors.add(new ProcessorDef(filter, classes, new Object[0]));
		}
		
		public void addProcessor(String filter, Class<? extends AssetProcessor> processor, Object[] arguments)
		{
			processors.add(new ProcessorDef(filter, new Class[] { processor }, arguments));
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
			Resource resource = locate(path);
			if(resource != null)
			{
				return createAsset(path, resource);
			}
			
			return null;
		}
		
		public Asset createAsset(String path, Resource resource)
			throws IOException
		{
			int idx = path.lastIndexOf('.');
			String extension = idx > 0 ? path.substring(idx+1) : "";
			boolean protect = isProtectedExtension(extension);
			
			boolean processed = false;
			Resource current = resource;
			NamedResource lastNamed = null;
			
			Set<ProcessorDef> applied = new HashSet<ProcessorDef>();
			
			/*
			 * Check if we have any filters that need to be applied.
			 * 
			 * Filters are applied in order and if a filter opts to rename
			 * a resource all filters but the ones already applied must
			 * be tested again.
			 */
			_outer:
			while(true)
			{
				for(ProcessorDef def : processors)
				{
					if(applied.contains(def)) continue;
					
					if(def.matches(path))
					{
						if(false == processed)
						{
							processed = true;
						}
						
						current = def.filter(namespace, path, current);
						applied.add(def);
						
						if(current instanceof NamedResource)
						{
							// Rename the resource
							lastNamed = (NamedResource) current;
							
							// Reapply filters in order
							continue _outer;
						}
					}
				}
				
				break;
			}
			
			
			if(lastNamed != null)
			{
				// The resource has been renamed
				int index = path.lastIndexOf('/');
				if(index == -1)
				{
					path = lastNamed.getName();
				}
				else
				{
					path = path.substring(0, index + 1) + lastNamed.getName(); 
				}
			}
			
			// Create the actual asset (to make sure the new path is applied)
			AssetImpl asset = new AssetImpl(manager, protect, namespace, path, current);
			
			if(lastNamed != null)
			{
				// If renamed, also apply it to its new path
				set(path, asset);
			}
			
			// Return the asset
			return asset;
		}
		
		public Resource locate(String path)
			throws IOException
		{
			// Check if the resource has been defined
			if(definedResources.containsKey(path))
			{
				return definedResources.get(path);
			}
			
			// Check with all asset sources
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
		
		/**
		 * Define a new resource for this namespace.
		 * 
		 * @param pathToFile
		 * @param assets
		 * @param processors
		 */
		@Override
		public void defineResource(String pathToFile, List<Asset> assets, List<ProcessorDef> processors)
		{
			this.processors.addAll(processors);
			
			definedResources.put(pathToFile, new MergedAssetResource(
				AssetManagerImpl.this, 
				assets.toArray(new Asset[assets.size()]))
			);
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
		private final Pattern filter;
		private final Class<? extends AssetProcessor>[] classes;
		private final Object[] arguments;
		
		public ProcessorDef(String filter, Class<? extends AssetProcessor>[] classes, Object[] arguments)
		{
			this.arguments = arguments;
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
				
				Resource out = instance.process(ns, path, in, arguments);
				
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

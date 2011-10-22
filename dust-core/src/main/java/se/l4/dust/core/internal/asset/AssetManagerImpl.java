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

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.Stage;

import se.l4.dust.api.Context;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetException;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.asset.AssetSource;
import se.l4.dust.api.resource.MergedResource;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.resource.variant.ResourceVariantManager.ResourceCallback;
import se.l4.dust.core.internal.resource.MergedResourceVariant;

@Singleton
public class AssetManagerImpl
	implements AssetManager
{
	private static final Asset NULL_ASSET = new AssetImpl(null, false, null, null, null);
	
	private final ConcurrentMap<String, AssetNamespace> cache;
	private final NamespaceManager manager;
	private final ResourceVariantManager variants;
	
	private final boolean production;
	
	private final List<Object> sources;
	private final Set<String> protectedExtensions;
	private final Map<String, AssetProcessor> extensionProcessors;
	
	private final Injector injector;
	
	@Inject
	public AssetManagerImpl(NamespaceManager manager,
			ResourceVariantManager variants,
			Injector injector,
			Stage stage)
	{
		this.manager = manager;
		this.variants = variants;
		this.injector = injector;
		sources = new CopyOnWriteArrayList<Object>();
		extensionProcessors = new ConcurrentHashMap<String, AssetProcessor>();
		
		Function<String, AssetNamespace> f;
		if(stage == Stage.DEVELOPMENT)
		{
			f = new Function<String, AssetNamespace>()
				{
					public AssetNamespace apply(String from)
					{
						return new DevAssetNamespace(from);
					}
				};
				
			production = false;
		}
		else
		{
			f = new Function<String, AssetNamespace>()
				{
					public AssetNamespace apply(String from)
					{
						return new AssetNamespace(from);
					}
				};
				
			production = true;
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
	
	public Asset locate(Context context, String ns, String file)
	{
		AssetNamespace ans = cache.get(ns);
		Asset a = ans.get(context, file);
		
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
	
	public void addTemporaryAsset(String ns, String path, Resource resource)
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
	
	public void processAssets(String namespace, String filter, Class<? extends AssetProcessor> processor)
	{
		AssetNamespace ans = cache.get(namespace);
		ans.addProcessor(filter, processor);
	}
	
	public void processAssets(String namespace, String filter, AssetProcessor processor)
	{
		AssetNamespace ans = cache.get(namespace);
		ans.addProcessor(filter, processor);
	}
	
	public AssetBuilder addAsset(String namespace, String pathToFile)
	{
		AssetNamespace parent = cache.get(namespace);
		return new AssetBuilderImpl(parent, namespace, pathToFile);
	}
	
	public void addExtensionProcessor(String extension, AssetProcessor processor)
	{
		extensionProcessors.put(extension, processor);
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
		
		private final String namespace;
		private final String pathToFile;
		
		private final List<AssetDef> assets;
		private final List<ProcessorDef> processors;

		public AssetBuilderImpl(AssetNamespace parent, String namespace, String pathToFile)
		{
			this.parent = parent;
			this.namespace = namespace;
			this.pathToFile = pathToFile;
			
			assets = new ArrayList<AssetDef>();
			processors = new ArrayList<ProcessorDef>();
		}

		public AssetBuilder add(String pathToFile)
		{
			return add(namespace, pathToFile);
		}

		public AssetBuilder add(String ns, String pathToFile)
		{
			AssetNamespace assetNs = cache.get(ns);
			assets.add(new AssetDef(assetNs, pathToFile));
			
			return this;
		}

		public AssetBuilder process(Class<? extends AssetProcessor> processor)
		{
			processors.add(new ProcessorDef(Pattern.quote(pathToFile), injector.getProvider(processor)));
			
			return this;
		}
		
		@Override
		public AssetBuilder process(final AssetProcessor processor)
		{
			processors.add(new ProcessorDef(Pattern.quote(pathToFile), new InstanceProvider<AssetProcessor>(processor)));
			
			return this;
		}

		public void create()
		{
			parent.defineResource(pathToFile, assets, processors);
		}
		
	}
	
	/**
	 * Definition of an asset in a specific namespace.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	private static class AssetDef
	{
		private final AssetNamespace ns;
		private final String path;

		public AssetDef(AssetNamespace ns, String path)
		{
			this.ns = ns;
			this.path = path;
		}
		
		public Asset get(Context context)
		{
			return ns.get(context, path);
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
		protected final ConcurrentMap<String, List<AssetDef>> builtAssets;
		protected final ConcurrentMap<String, Resource> definedResources;
		
		protected final String namespace;
		
		private final ResourceCallback resourceCallback;
		
		public AssetNamespace(String namespace)
		{
			this.namespace = namespace;
			
			cache = new MapMaker().makeComputingMap(new AssetLocator(this));
			processors = new CopyOnWriteArrayList<ProcessorDef>();
			builtAssets = new ConcurrentHashMap<String, List<AssetDef>>();
			definedResources = new ConcurrentHashMap<String, Resource>();
			
			resourceCallback = new ResourceVariantManager.ResourceCallback()
			{
				public boolean exists(ResourceVariant variant, String url)
					throws IOException
				{
					return locate(url) != null;
				}
			};
		}
		
		/**
		 * Define a new resource for this namespace.
		 * 
		 * @param pathToFile
		 * @param assets
		 * @param processors
		 */
		public void defineResource(String pathToFile, List<AssetDef> assets, List<ProcessorDef> processors)
		{
			this.processors.addAll(processors);
			builtAssets.put(pathToFile, assets);
		}
		
		public Resource getResource(Context context, List<Asset> assets)
		{
			Resource[] resources = new Resource[assets.size()];
			for(int i=0, n=assets.size(); i<n; i++)
			{
				resources[i] = assets.get(i).getResource();
			}
			return new MergedResource(resources);
		}

		public void addProcessor(String filter, Class<? extends AssetProcessor> processor)
		{
			processors.add(new ProcessorDef(filter, injector.getProvider(processor)));
		}
		
		public void addProcessor(String filter, AssetProcessor processor)
		{
			processors.add(new ProcessorDef(filter, new InstanceProvider<AssetProcessor>(processor)));
		}
		
		public Asset get(Context context, String path)
		{
			if(path == null)
			{
				throw new IllegalArgumentException("Path can not be null");
			}
			
			try
			{
				// Check if the resource has been defined
				if(builtAssets.containsKey(path))
				{
					return handleBuiltAsset(context, path);
				}

				// Attempt to resolve the correct variant of the asset
				path = variants.resolve(context, resourceCallback, path);
				
				Asset asset = cache.get(path);
				return asset == NULL_ASSET ? null : asset;
			}
			catch(IOException e)
			{
				throw new AssetException("Unable to locate suitable variant of " + path + " in " + namespace);
			}
			catch(ComputationException e)
			{
				throw new AssetException("Unable to load " + path + " in " + namespace + "; " + e.getCause().getMessage(), e.getCause());
			}
		}

		private Asset handleBuiltAsset(Context context, String path)
			throws IOException
		{
			/*
			 * Built assets need special treatment. First try to resolve
			 * a new name for the merged asset.
			 */
			final List<AssetDef> defs = builtAssets.get(path);
			String name = variants.resolve(context, new ResourceCallback()
			{
				public boolean exists(ResourceVariant variant, String url)
					throws IOException
				{
					for(AssetDef d : defs)
					{
						ResourceVariant v = variants.resolveRealVariant(variant, resourceCallback, d.path);
						if(v != null && ((MergedResourceVariant) variant).hasSpecific(v))
						{
							return true;
						}
					}
					
					return false;
				}
			}, path);
			
			if(false == cache.containsKey(name))
			{
				// If it has not been cached create a suitable resource for it
				List<Asset> assets = new ArrayList<Asset>();
				for(AssetDef d : defs)
				{
					Asset asset = d.get(context);
					assets.add(asset);
				}
				
				Resource r = getResource(context, assets);
				definedResources.put(name, r);
			}
			
			return cache.get(name);
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
			String lastName = path;
			
			if(! extensionProcessors.isEmpty())
			{
				AssetProcessor ext = extensionProcessors.get(extension);
				if(ext != null)
				{
					AssetEncounterImpl encounter = new AssetEncounterImpl(manager, production, current, namespace, lastName);
					
					ext.process(encounter);
					
					if(encounter.isReplaced())
					{
						current = encounter.getReplacedWith();
					}
					
					if(encounter.isRenamed())
					{
						lastName = encounter.getRenamedTo();
					}
				}
			}
			
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
						
						AssetEncounterImpl encounter = new AssetEncounterImpl(manager, production, current, namespace, lastName);
						
						def.getProcessor().process(encounter);
						applied.add(def);
						
						if(encounter.isReplaced())
						{
							current = encounter.getReplacedWith();
						}
						
						if(encounter.isRenamed())
						{
							lastName = encounter.getRenamedTo();
							
							// Reapply filters in order
							continue _outer;
						}
					}
				}
				
				break;
			}
			
			String originalPath = path;
			boolean renamed = ! lastName.equals(path);
			if(renamed)
			{
				// The resource has been renamed, update path
				path = lastName;
			}
			
			// Create the actual asset (to make sure the new path is applied)
			Asset asset = createAsset(protect, path, current, resource, originalPath);
			
			if(renamed)
			{
				// If renamed, also apply it to its new path
				set(path, asset);
			}
			
			// Return the asset
			return asset;
		}
		
		protected Asset createAsset(boolean protect,
				String path, 
				Resource resource, 
				Resource original,
				String originalPath)
		{
			return new AssetImpl(manager, protect, namespace, path, resource);
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
		public DevAssetNamespace(String namespace)
		{
			super(namespace);
		}
		
		@Override
		public Resource getResource(Context context, List<Asset> assets)
		{
			return new MergedAssetResource(AssetManagerImpl.this, context, assets.toArray(new Asset[assets.size()]));
		}
		
		@Override
		protected Asset createAsset(
				boolean protect,
				String path,
				Resource resource, 
				final Resource original,
				String originalPath)
		{
			final Asset asset = super.createAsset(protect, path, resource, original, originalPath);
			return new DevAsset(this, asset, originalPath);
		}
	}
	
	/**
	 * Implementation of asset that takes care of data reloading when
	 * needed.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	private static class DevAsset
		implements Asset
	{
		private final DevAssetNamespace ns;
		
		private final String orignalPath;
		private Asset asset;

		public DevAsset(DevAssetNamespace ns, Asset asset, String orignalPath)
		{
			this.ns = ns;
			this.asset = asset;
			this.orignalPath = orignalPath;
		}
		
		public String getChecksum()
		{
			return asset.getChecksum();
		}
		
		public String getName()
		{
			return asset.getName();
		}
		
		public String getNamespace()
		{
			return asset.getNamespace();
		}
		
		public Resource getResource()
		{
			try
			{
				Resource resource = ns.locate(orignalPath);
				if(resource.getLastModified() > asset.getResource().getLastModified())
				{
					// Recreation needed so we replace the old asset
					asset = ns.createAsset(orignalPath, resource);
					if(asset instanceof DevAsset)
					{
						asset = ((DevAsset) asset).asset;
					}
				}
			}
			catch(IOException e)
			{
				throw new RuntimeException("Unable to recreate the asset; " + e.getMessage(), e);
			}
			
			return asset.getResource();
		}
		
		public boolean isProtected()
		{
			return asset.isProtected();
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
		private final Provider<? extends AssetProcessor> processor;
		
		public ProcessorDef(String filter, Provider<? extends AssetProcessor> processor)
		{
			this.filter = Pattern.compile(filter);
			this.processor = processor;
		}
		
		public boolean matches(String path)
		{
			return filter.matcher(path).matches();
		}
		
		public AssetProcessor getProcessor()
		{
			return processor.get();
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
	
	private static class InstanceProvider<T>
		implements Provider<T>
	{
		private final T instance;
		
		public InstanceProvider(T instance)
		{
			this.instance = instance;
		}
		
		@Override
		public T get()
		{
			return instance;
		}
	}
}

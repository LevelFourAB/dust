package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import se.l4.dust.api.Context;
import se.l4.dust.api.Namespace;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetCache;
import se.l4.dust.api.asset.AssetException;
import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.asset.Assets;
import se.l4.dust.api.resource.AbstractResource;
import se.l4.dust.api.resource.MergedResource;
import se.l4.dust.api.resource.NamespaceLocation;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.resource.Resources;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.resource.variant.ResourceVariantResolution;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.Stage;

@Singleton
public class AssetsImpl
	implements Assets
{
	private static final Asset NULL_ASSET = new AssetImpl(null, null, null, false);
	
	private final LoadingCache<String, AssetNamespace> cache;
	private final Namespaces manager;
	private final Resources resources;
	private final ResourceVariantManager variants;
	
	private final boolean production;
	
	private final Set<String> protectedExtensions;
	
	private final Injector injector;
	private final BuiltAssetLocator builtAssetLocator;

	private volatile AssetCache assetCache;
	
	@Inject
	public AssetsImpl(Namespaces namespaces0,
			Resources resources,
			ResourceVariantManager variants,
			BuiltAssetLocator builtAssetLocator,
			Injector injector,
			Stage stage)
	{
		this.manager = namespaces0;
		this.resources = resources;
		this.variants = variants;
		this.builtAssetLocator = builtAssetLocator;
		this.injector = injector;
		
		protectedExtensions = Sets.newHashSet();
		
		CacheLoader<String, AssetNamespace> f;
		if(stage == Stage.DEVELOPMENT)
		{
			f = new CacheLoader<String, AssetNamespace>()
			{
				@Override
				public AssetNamespace load(String from)
				{
					return new DevAssetNamespace(manager.getNamespaceByURI(from));
				}
			};
			
			production = false;
		}
		else
		{
			f = new CacheLoader<String, AssetNamespace>()
			{
				@Override
				public AssetNamespace load(String from)
				{
					return new AssetNamespace(manager.getNamespaceByURI(from));
				}
			};
				
			production = true;
		}
		
		cache = CacheBuilder
			.newBuilder()
			.build(f);
	}
	
	@Inject(optional=true)
	public void setAssetCache(AssetCache cache)
	{
		this.assetCache = cache;
	}
	
	private AssetNamespace get(String ns)
	{
		try
		{
			return cache.get(ns);
		}
		catch(ExecutionException e)
		{
			throw Throwables.propagate(e.getCause());
		}
	}
	
	@Override
	public Asset locate(Context context, String ns, String file)
	{
		AssetNamespace ans = get(ns);
		Asset a = ans.get(context, file);
		
		return a == NULL_ASSET ? null: a;
	}

	@Override
	public void addProtectedExtension(String extension)
	{
		protectedExtensions.add(extension);
	}
	
	@Override
	public boolean isProtectedExtension(String extension)
	{
		return protectedExtensions.contains(extension);
	}
	
	@Override
	public void addTemporaryAsset(String ns, String path, Resource resource)
	{
		builtAssetLocator.add(ns, path, resource);
	}
	
	@Override
	public AssetBuilder define(String namespace, String pathToFile)
	{
		AssetNamespace parent = get(namespace);
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
		
		private final String namespace;
		private final String pathToFile;
		
		private final List<ResourceLocation> resources;
		private final List<Provider<? extends AssetProcessor>> processors;

		public AssetBuilderImpl(AssetNamespace parent, String namespace, String pathToFile)
		{
			this.parent = parent;
			this.namespace = namespace;
			this.pathToFile = pathToFile;
			
			resources = Lists.newArrayList();
			processors = Lists.newArrayList();
		}

		@Override
		public AssetBuilder add(String pathToFile)
		{
			return add(namespace, pathToFile);
		}

		@Override
		public AssetBuilder add(String ns, String pathToFile)
		{
			resources.add(new NamespaceLocation(manager.getNamespaceByURI(ns), pathToFile));
			
			return this;
		}

		@Override
		public AssetBuilder process(Class<? extends AssetProcessor> processor)
		{
			processors.add(injector.getProvider(processor));
			
			return this;
		}
		
		@Override
		public AssetBuilder process(final AssetProcessor processor)
		{
			processors.add(new InstanceProvider<AssetProcessor>(processor));
			
			return this;
		}

		@Override
		public void create()
		{
			parent.defineResource(pathToFile, resources, processors);
		}
	}
	
	private static class AssetDef
	{
		private final List<ResourceLocation> resources;
		private final List<Provider<? extends AssetProcessor>> processors;
		
		public AssetDef(List<ResourceLocation> resources,
				List<Provider<? extends AssetProcessor>> processors)
		{
			this.resources = resources;
			this.processors = processors;
		}
	}
	
	/**
	 * Default encapsulation of a namespace associated with assets. This class
	 * handles retrieval and creation of assets.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	public class AssetNamespace
	{
		protected final Namespace namespace;
		protected final Map<String, AssetDef> builtAssets;
		
		public AssetNamespace(Namespace namespace)
		{
			this.namespace = namespace;
			
			builtAssets = Maps.newHashMap();
		}
		
		/**
		 * Define a new resource for this namespace.
		 * 
		 * @param pathToFile
		 * @param assets
		 * @param processors
		 * @throws IOException 
		 */
		public void defineResource(String pathToFile,
				List<ResourceLocation> resourceLocations,
				List<Provider<? extends AssetProcessor>> processors)
		{
			// Register a default one with the resources
			Resource[] resolvedResources = new Resource[resourceLocations.size()];
			int idx = 0;
			for(ResourceLocation location : resourceLocations)
			{
				try
				{
					Resource resource = resources.locate(location);
					if(resource == null)
					{
						throw new AssetException("Unable to define " + pathToFile + " in " + namespace.getUri() + "; Resource " + location + " does not exist");
					}
					resolvedResources[idx++] = wrapResource(resource);
				}
				catch(IOException e)
				{
					throw new AssetException("Unable to define " + pathToFile + " in " + namespace.getUri() + "; Resource " + location + " could not be read; " + e.getMessage(), e);
				}
			}
			
			AssetDef def = new AssetDef(resourceLocations, processors);
			builtAssets.put(pathToFile, def);
			
			try
			{
				MergedResource merged = new MergedResource(
					new NamespaceLocation(namespace, pathToFile),
					resolvedResources
				);
				processAndRegister(pathToFile, merged, processors);
			}
			catch(IOException e)
			{
				throw new AssetException("Unable to define " + pathToFile + " in " + namespace.getUri() + "; " + e.getMessage(), e);
			}
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
				ResourceVariantResolution result = variants.resolve(context, new NamespaceLocation(namespace, path));
				if(result == null) return null;
				
				return createAsset(result.getName(), result.getResource());
			}
			catch(IOException e)
			{
				throw new AssetException("Unable to locate suitable variant of " + path + " in " + namespace);
			}
		}
		
		private Asset handleBuiltAsset(final Context context, final String path)
			throws IOException
		{
			final NamespaceLocation location = new NamespaceLocation(namespace, path);
			ResourceVariantResolution result = variants.createCombined(context, location, new Supplier<List<ResourceVariantResolution>>()
			{
				@Override
				public List<ResourceVariantResolution> get()
				{
					try
					{
						List<ResourceLocation> defs = builtAssets.get(path).resources;
						
						List<ResourceVariantResolution> actualResources = Lists.newArrayListWithCapacity(defs.size());
						for(ResourceLocation def : defs)
						{
							actualResources.add(variants.resolve(context, def));
						}
						
						return actualResources;
					}
					catch(IOException e)
					{
						throw Throwables.propagate(e);
					}
				}
			});
			
			Resource resource = builtAssetLocator.locate(namespace.getUri(), result.getName());
			if(resource == null)
			{
				processAndRegister(result.getName(), result.getResource(), builtAssets.get(path).processors);
			}
			
			return createAsset(result.getName(), resource);
		}
		
		protected Resource wrapResource(Resource resource)
		{
			return resource;
		}
		
		protected void processAndRegister(String pathToFile, Resource resource, List<Provider<? extends AssetProcessor>> processors)
			throws IOException
		{
			resource = process(resource, processors);
			builtAssetLocator.add(namespace.getUri(), pathToFile, resource);
		}
		
		protected Resource process(Resource in, List<Provider<? extends AssetProcessor>> processors)
			throws IOException
		{
			String name = in.getLocation().getName();
			Resource current = in;
			
			for(Provider<? extends AssetProcessor> provider : processors)
			{
				AssetEncounterImpl encounter = new AssetEncounterImpl(production, current, namespace, name, assetCache);
				
				AssetProcessor processor = provider.get();
				processor.process(encounter);
				
				if(encounter.isReplaced())
				{
					current = encounter.getReplacedWith();
				}
			}
			
			return current;
		}
		
		public Asset createAsset(String name, Resource resource)
			throws IOException
		{
			int idx = name.lastIndexOf('.');
			boolean protect = idx == -1 ? false
				: isProtectedExtension(name.substring(idx+1));
			
			return createAsset(name, resource, protect);
		}
		
		protected Asset createAsset(String name, Resource resource, boolean protect)
		{
			return new AssetImpl(namespace, name, resource, protect);
		}

		public Resource locate(ResourceLocation location)
			throws IOException
		{
			return resources.locate(location);
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
		
		protected Resource process0(Resource in, List<Provider<? extends AssetProcessor>> processors)
			throws IOException
		{
			return super.process(in, processors);
		}
		
		@Override
		protected Resource wrapResource(final Resource resource)
		{
			if(resource instanceof ReloadingResource)
			{
				return resource;
			}
			
			Supplier<Resource> original = new Supplier<Resource>()
			{
				@Override
				public Resource get()
				{
					try
					{
						return locate(resource.getLocation());
					}
					catch(IOException e)
					{
						throw Throwables.propagate(e);
					}
				}
			};
			
			return new ReloadingResource(resource, original, Functions.<Resource>identity());
		}
		
		@Override
		protected Asset createAsset(final String name, Resource resource, boolean protect)
		{
			resource = wrapResource(resource);
			
			return new AssetImpl(namespace, name, resource, protect)
			{
				@Override
				public Resource getResource()
				{
					Resource resource = super.getResource();
					try
					{
						((ReloadingResource) resource).maybeReload();
					}
					catch(IOException e)
					{
						throw new AssetException();
					}
					return resource;
				}
			};
		}
		
		@Override
		protected Resource process(Resource in, final List<Provider<? extends AssetProcessor>> processors)
			throws IOException
		{
			return new ReloadingResource(in, Suppliers.ofInstance(in), new Function<Resource, Resource>()
			{
				@Override
				public Resource apply(Resource input)
				{
					System.out.println("Processing");
					try
					{
						return process0(input, processors);
					}
					catch(IOException e)
					{
						throw Throwables.propagate(e);
					}
				}
			});
		}
	}
	
	private class ReloadingResource
		extends AbstractResource
	{
		private final Function<Resource, Resource> reloader;
		private final Supplier<Resource> original;
		
		private Resource current;
		private boolean first;

		public ReloadingResource(Resource resource,
				Supplier<Resource> original,
				Function<Resource, Resource> reloader)
		{
			super(null);
			
			first = true;
			
			this.original = original;
			this.current = resource;
			this.reloader = reloader;
		}
		
		@Override
		public String getContentEncoding()
		{
			return current.getContentEncoding();
		}
		
		@Override
		public int getContentLength()
		{
			return current.getContentLength();
		}
		
		@Override
		public String getContentType()
		{
			return current.getContentType();
		}
		
		@Override
		public long getLastModified()
		{
			return current.getLastModified();
		}
		
		@Override
		public InputStream openStream()
			throws IOException
		{
			return current.openStream();
		}
		
		public synchronized void maybeReload()
			throws IOException
		{
			Resource resource = original.get();
			maybeReload(resource);
			
			System.out.println(resource.getLocation() + " " + resource.getLastModified() + " < " + current.getLastModified());
			
			if(first || resource.getLastModified() > current.getLastModified())
			{
				first = false;
				this.current = reloader.apply(resource);
			}
		}
		
		private void maybeReload(Resource resource)
			throws IOException
		{
			if(resource instanceof MergedResource)
			{
				MergedResource merged = (MergedResource) resource;
				for(Resource r : merged.getResources())
				{
					if(r instanceof ReloadingResource)
					{
						((ReloadingResource) r).maybeReload();
					}
					else if (r instanceof MergedResource)
					{
						maybeReload(resource);
					}
				}
			}
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

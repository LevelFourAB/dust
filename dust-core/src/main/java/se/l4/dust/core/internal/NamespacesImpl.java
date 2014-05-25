package se.l4.dust.core.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.NamespacePlugin;
import se.l4.dust.api.Namespaces;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;

@Singleton
public class NamespacesImpl
	implements Namespaces
{
	private final static SecureRandom random = new SecureRandom();
	
	private final Injector injector;
	
	private final Map<String, Namespace> packages;
	private final Map<String, Namespace> prefixes;
	private final Map<String, Namespace> uris;
	
	private final List<Namespace> namespaces;
	
	@Inject
	public NamespacesImpl(Injector injector)
	{
		this.injector = injector;
		packages = new ConcurrentHashMap<String, Namespace>();
		prefixes = new ConcurrentHashMap<String, Namespace>();
		uris = new ConcurrentHashMap<String, Namespace>();
		
		namespaces = Lists.newArrayList();
		
		bind("dust:common").add();
	}
	
	public NamespaceBinder bind(String nsUri)
	{
		return new NamespaceBinderImpl(nsUri);
	}
	
	protected static final String generateVersion(String ns)
	{
		return Long.toHexString(random.nextLong());
	}
	
	private void addNamespace(String uri,
			String prefix,
			String pkg,
			String version,
			String resourceReference,
			ClassLoader loader,
			List<NamespacePlugin> plugins)
	{
		Namespace ns = new NamespaceImpl(uri, prefix, pkg, version, resourceReference, loader);
		namespaces.add(ns);
		uris.put(uri, ns);
		
		if(pkg != null)
		{
			packages.put(pkg, ns);
		}
		
		if(prefix != null)
		{
			prefixes.put(prefix, ns);
		}
		
		for(NamespacePlugin plugin : plugins)
		{
			plugin.register(injector, ns);
		}
	}
	
	public boolean isBound(String ns)
	{
		return uris.containsKey(ns);
	}
	
	public Namespace getBinding(String pkg)
	{
		return packages.get(pkg);
	}
	
	public Namespace getNamespaceByPrefix(String prefix)
	{
		return prefixes.get(prefix);
	}
	
	@Override
	public Namespace getNamespaceByURI(String uri)
	{
		return uris.get(uri);
	}
	
	@Override
	public Iterable<Namespace> list()
	{
		return namespaces;
	}
	
	private class NamespaceBinderImpl
		implements NamespaceBinder
	{
		private final String uri;
		private String pkg;
		private String version;
		private String prefix;
		private ClassLoader loader;
		private String resourceReference;
		private List<NamespacePlugin> plugins;
		private boolean manual;
		
		public NamespaceBinderImpl(String uri)
		{
			this.uri = uri;
			plugins = Lists.newArrayList();
		}

		public NamespaceBinder setPackage(String pkg)
		{
			if(loader != null)
			{
				// If no loader provides use the context loader
				loader = Thread.currentThread().getContextClassLoader();
			}
			
			this.pkg = pkg;
			return this;
		}

		public NamespaceBinder setPackage(Package pkg)
		{
			return setPackage(pkg.getName());
		}

		public NamespaceBinder setPackageFromClass(Class<?> type)
		{
			loader = type.getClassLoader();
			resourceReference = type.getSimpleName() + ".class";
			return setPackage(type.getPackage());
		}

		public NamespaceBinder setVersion(String version)
		{
			this.version = version;
			
			return this;
		}

		public NamespaceBinder setPrefix(String prefix)
		{
			this.prefix = prefix;
			
			return this;
		}
		
		@Override
		public NamespaceBinder manual()
		{
			this.manual = true;
			
			return this;
		}
		
		@Override
		public NamespaceBinder with(NamespacePlugin plugin)
		{
			plugins.add(plugin);
			
			return this;
		}

		public void add()
		{
			if(version == null)
			{
				version = generateVersion(uri);
			}
			
			if(pkg == null && ! manual)
			{
				// No package set, try to autodetect
				StackTraceElement[] trace = new Exception().getStackTrace();
				
				try
				{
					ClassLoader loader = Thread.currentThread().getContextClassLoader();
					Class<?> type = loader.loadClass(trace[1].getClassName());
					if(Module.class.isAssignableFrom(type))
					{
						// Set the package if this a module
						setPackageFromClass(type);
					}
				}
				catch(ClassNotFoundException e)
				{
				}
			}
			
			addNamespace(uri, prefix, pkg, version, resourceReference, loader, plugins);
		}
		
	}
	
	private static class NamespaceImpl
		implements Namespace
	{
		private final String uri;
		private final String prefix;
		private final String pkg;
		private final String version;
		private final Locator locator;

		public NamespaceImpl(String uri, String prefix, String pkg, String version, String resourceReference, ClassLoader loader)
		{
			this.uri = uri;
			this.prefix = prefix;
			this.pkg = pkg;
			this.version = version;
			
			locator = loader != null 
				? new ClassLoaderLocator(loader, pkg, resourceReference)
				: new FailingLocator(uri);
		}

		public String getPrefix()
		{
			return prefix;
		}

		public String getUri()
		{
			return uri;
		}

		public String getVersion()
		{
			return version;
		}

		public URL getResource(String resource)
		{
			return locator.locateResource(resource);
		}
		
		public URI resolveResource(String resource)
		{
			return locator.resolveResource(resource);
		}
		
		public String getPackage()
		{
			return pkg;
		}
	}
	
	private static interface Locator
	{
		URL locateResource(String path);
		
		URI resolveResource(String path);
	}
	
	private static class ClassLoaderLocator
		implements Locator
	{
		private final String base;
		private final ClassLoader loader;
		private final URI reference;

		public ClassLoaderLocator(ClassLoader loader, String base, String resourceReference)
		{
			this.loader = loader;
			this.base = base.replace('.', '/') + "/";
			
			if(resourceReference != null)
			{
				URL resource = loader.getResource(this.base + resourceReference);
				if(resource != null)
				{
					try
					{
						this.reference = resource.toURI().resolve(".").normalize();
					}
					catch(URISyntaxException e)
					{
						throw Throwables.propagate(e);
					}
				}
				else
				{
					this.reference = null;
				}
			}
			else
			{
				this.reference = null;
			}
		}
		
		@Override
		public URI resolveResource(String path)
		{
			return reference.resolve(path);
		}
		
		public URL locateResource(String path)
		{
			return loader.getResource(base + path);
		}
	}
	
	private static class FailingLocator
		implements Locator
	{
		private final String uri;

		public FailingLocator(String uri)
		{
			this.uri = uri;
		}
		
		@Override
		public URI resolveResource(String path)
		{
			return null;
		}
		
		@Override
		public URL locateResource(String path)
		{
			return null;
//			throw new AssetException("The namespace " + uri + " does not have any assets. Did you tie it to a package or class?");
		}
	}
}

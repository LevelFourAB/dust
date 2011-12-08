package se.l4.dust.core.internal;

import java.net.URL;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.asset.AssetException;

import com.google.inject.Singleton;

@Singleton
public class NamespaceManagerImpl
	implements NamespaceManager
{
	private final static SecureRandom random = new SecureRandom();
	
	private final Map<String, Namespace> packages;
	private final Map<String, Namespace> prefixes;
	private final Map<String, Namespace> uris;
	
	public NamespaceManagerImpl()
	{
		packages = new ConcurrentHashMap<String, Namespace>();
		prefixes = new ConcurrentHashMap<String, Namespace>();
		uris = new ConcurrentHashMap<String, Namespace>();
		
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
	
	private void addNamespace(String uri, String prefix, String pkg, String version, ClassLoader loader)
	{
		Namespace ns = new NamespaceImpl(uri, prefix, pkg, version, loader);
		uris.put(uri, ns);
		
		if(pkg != null)
		{
			packages.put(pkg, ns);
		}
		
		if(prefix != null)
		{
			prefixes.put(prefix, ns);
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
	
	public Namespace getNamespaceByURI(String uri)
	{
		return uris.get(uri);
	}
	
	public Iterator<Namespace> iterator()
	{
		return uris.values().iterator();
	}
	
	private class NamespaceBinderImpl
		implements NamespaceBinder
	{
		private final String uri;
		private String pkg;
		private String version;
		private String prefix;
		private ClassLoader loader;
		
		public NamespaceBinderImpl(String uri)
		{
			this.uri = uri;
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

		public void add()
		{
			if(version == null)
			{
				version = generateVersion(uri);
			}
			
			addNamespace(uri, prefix, pkg, version, loader);
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

		public NamespaceImpl(String uri, String prefix, String pkg, String version, ClassLoader loader)
		{
			this.uri = uri;
			this.prefix = prefix;
			this.pkg = pkg;
			this.version = version;
			
			locator = loader != null 
				? new ClassLoaderLocator(loader, pkg)
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
		
		public String getPackage()
		{
			return pkg;
		}
	}
	
	private static interface Locator
	{
		URL locateResource(String path);
	}
	
	private static class ClassLoaderLocator
		implements Locator
	{
		private final String base;
		private final ClassLoader loader;

		public ClassLoaderLocator(ClassLoader loader, String base)
		{
			this.loader = loader;
			this.base = base.replace('.', '/') + "/";
		
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
		public URL locateResource(String path)
		{
			throw new AssetException("The namespace " + uri + " does not have any assets. Did you tie it to a package or class?");
		}
	}
}

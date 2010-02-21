package se.l4.dust.core.internal;

import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jdom.Namespace;

import se.l4.dust.api.NamespaceManager;

import com.google.inject.Singleton;

@Singleton
public class NamespaceManagerImpl
	implements NamespaceManager
{
	private final Map<String, Namespace> packages;
	private final Map<Namespace, Locator> locators;
	private final Map<String, Namespace> prefixes;
	private final Map<String, Namespace> urls;
	private final Map<String, String> versions;
	private final SecureRandom random;
	
	public NamespaceManagerImpl()
	{
		packages = new ConcurrentHashMap<String, Namespace>();
		locators = new ConcurrentHashMap<Namespace, Locator>();
		prefixes = new ConcurrentHashMap<String, Namespace>();
		urls = new ConcurrentHashMap<String, Namespace>();
		versions = new ConcurrentHashMap<String, String>();
		
		random = new SecureRandom();
	}

	private void prefix(Namespace ns)
	{
		prefixes.put(ns.getPrefix(), ns);
		urls.put(ns.getURI(), ns);
	}
	
	private String generateVersion(Namespace ns)
	{
		return Long.toHexString(random.nextLong());
	}
	
	public void bind(Namespace ns, Class<?> pkgBase)
	{
		bind(ns, pkgBase, generateVersion(ns));
	}
	
	public void bind(Namespace ns, Class<?> pkgBase, String version)
	{
		String pkg = pkgBase.getPackage().getName();
		packages.put(pkg, ns);
		locators.put(ns, 
			new ClassLoaderLocator(pkgBase.getClassLoader(), pkg)
		);
		versions.put(ns.getURI(), version);
		prefix(ns);
	}
	
	public void bind(Namespace ns, Package pkg)
	{
		bind(ns, pkg, generateVersion(ns));
	}
	
	public void bind(Namespace ns, Package pkg, String version)
	{
		packages.put(pkg.getName(), ns);
		locators.put(ns, 
			new ClassLoaderLocator(getClass().getClassLoader(), pkg.getName())
		);
		versions.put(ns.getURI(), version);
		prefix(ns);
	}
	
	public void bind(Namespace ns, String pkg)
	{
		bind(ns, pkg, generateVersion(ns));
	}
	
	public void bind(Namespace ns, String pkg, String version)
	{
		packages.put(pkg, ns);
		locators.put(ns, 
			new ClassLoaderLocator(getClass().getClassLoader(), pkg)
		);
		versions.put(ns.getURI(), version);
		prefix(ns);
	}
	
	public void bindSimple(Namespace ns)
	{
		bindSimple(ns, generateVersion(ns));
	}
	
	public void bindSimple(Namespace ns, String version)
	{
		urls.put(ns.getURI(), ns);
		versions.put(ns.getURI(), version);
		prefix(ns);
	}
	
	public boolean isBound(Namespace ns)
	{
		return urls.containsKey(ns.getURI());
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
		return urls.get(uri);
	}
	
	public URL getResource(Namespace ns, String resource)
	{
		Locator locator = locators.get(ns);
		if(locator == null)
		{
			return null;
		}
		
		return locator.locateResource(resource);
	}
	
	public String getVersion(Namespace ns)
	{
		return versions.get(ns.getURI());
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
}

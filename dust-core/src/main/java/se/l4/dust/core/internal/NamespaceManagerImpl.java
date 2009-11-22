package se.l4.dust.core.internal;

import java.net.URL;
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
	
	public NamespaceManagerImpl()
	{
		packages = new ConcurrentHashMap<String, Namespace>();
		locators = new ConcurrentHashMap<Namespace, Locator>();
		prefixes = new ConcurrentHashMap<String, Namespace>();
	}

	private void prefix(Namespace ns)
	{
		prefixes.put(ns.getPrefix(), ns);
	}
	
	public void bind(Namespace ns, Class<?> pkgBase)
	{
		String pkg = pkgBase.getPackage().getName();
		packages.put(pkg, ns);
		locators.put(ns, 
			new ClassLoaderLocator(pkgBase.getClassLoader(), pkg)
		);
		prefix(ns);
	}
	
	public void bind(Namespace ns, Package pkg)
	{
		packages.put(pkg.getName(), ns);
		locators.put(ns, 
			new ClassLoaderLocator(getClass().getClassLoader(), pkg.getName())
		);
		prefix(ns);
	}
	
	public void bind(Namespace ns, String pkg)
	{
		packages.put(pkg, ns);
		locators.put(ns, 
			new ClassLoaderLocator(getClass().getClassLoader(), pkg)
		);
		prefix(ns);
	}
	
	public Namespace getBinding(String pkg)
	{
		return packages.get(pkg);
	}
	
	public Namespace getNamespaceByPrefix(String prefix)
	{
		return prefixes.get(prefix);
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

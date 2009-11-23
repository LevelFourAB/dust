package se.l4.dust.api;

import java.net.URL;

import org.jdom.Namespace;

/**
 * The namespace manager is used to bind namespaces to packages to enable
 * automatic page and component discovery.
 * 
 * @author Andreas Holstenson
 *
 */
public interface NamespaceManager
{
	/**
	 * Bind the given namespace to a package name. This allows for automatic
	 * page and component discovery.
	 * 
	 * @param ns
	 * @param pkg
	 */
	void bind(Namespace ns, String pkg);
	
	/**
	 * Bind the given namespace to a package name.
	 * 
	 * @param ns
	 * @param pkg
	 */
	void bind(Namespace ns, Package pkg);
	
	/**
	 * Bind the given namespace to a package name.
	 * 
	 * @param ns
	 * @param pkgBase
	 */
	void bind(Namespace ns, Class<?> pkgBase);
	
	/**
	 * Get the namespace of a package if any exists.
	 * 
	 * @param pkg
	 * @return
	 */
	Namespace getBinding(String pkg);
	
	/**
	 * Locate a namespace based on it's registered prefix.
	 * 
	 * @param prefix
	 * @return
	 */
	Namespace getNamespaceByPrefix(String prefix);

	Namespace getNamespaceByURI(String uri);
	
	URL getResource(Namespace ns, String resource);

}

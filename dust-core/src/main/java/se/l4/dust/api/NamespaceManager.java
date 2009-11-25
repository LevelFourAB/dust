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
	 * Bind the given namespace to a package name generating a version for it. 
	 * This allows for automatic page and component discovery.
	 * 
	 * @param ns
	 * @param pkg
	 */
	void bind(Namespace ns, String pkg);
	
	/**
	 * Bind the given namespace to a package name. This allows for automatic
	 * page and component discovery.
	 * 
	 * @param ns
	 * @param pkg
	 * @param version
	 */
	void bind(Namespace ns, String pkg, String version);
	
	/**
	 * Bind the given namespace to a package name.
	 * 
	 * @param ns
	 * @param pkg
	 */
	void bind(Namespace ns, Package pkg);
	
	void bind(Namespace ns, Package pkg, String version);
	
	/**
	 * Bind the given namespace to a package name.
	 * 
	 * @param ns
	 * @param pkgBase
	 */
	void bind(Namespace ns, Class<?> pkgBase);
	
	void bind(Namespace ns, Class<?> pkgBase, String version);
	
	/**
	 * Bind the given namespace without tying it to a package. This allows
	 * it to be accssed via other method in the class but will not enable
	 * page and component detection for the namespace.
	 * 
	 * @param ns
	 */
	void bindSimple(Namespace ns);
	
	/**
	 * Bind the given namespace without tying it to a package. This allows
	 * it to be accssed via other method in the class but will not enable
	 * page and component detection for the namespace.
	 * 
	 * @param ns
	 */
	void bindSimple(Namespace ns, String version);
	
	/**
	 * Check if the namespace has been bound.
	 * 
	 * @param ns
	 * @return
	 */
	boolean isBound(Namespace ns);
	
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

	String getVersion(Namespace ns);
}

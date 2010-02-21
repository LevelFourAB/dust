package se.l4.dust.api;

import java.net.URL;

import org.jdom.Namespace;

import se.l4.dust.api.asset.AssetManager;

/**
 * The namespace manager is used to bind namespaces to packages to enable
 * automatic page and component discovery.
 * 
 * <p>
 * Namespaces can have prefixes (see {@link Namespace#getPrefix()}) which are
 * used in certain places. Namespaces can also have versions specified when
 * bound, if no version is specified one will be auto-generated. Both prefixes
 * and versions are used when serving assets, see {@link AssetManager}).
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
	 * 		namespace to bind
	 * @param pkg
	 * 		full package name
	 */
	void bind(Namespace ns, String pkg);
	
	/**
	 * Bind the given namespace to a package name. This allows for automatic
	 * page and component discovery.
	 * 
	 * @param ns
	 * 		namespace to bind
	 * @param pkg
	 * 		full package name
	 * @param version
	 * 		version of package
	 */
	void bind(Namespace ns, String pkg, String version);
	
	/**
	 * Bind the given namespace to a package name.
	 * 
	 * @param ns
	 * 		namespace to bind
	 * @param pkg
	 * 		package instance
	 */
	void bind(Namespace ns, Package pkg);
	
	/**
	 * Bind the given namespace to a package including a version of the
	 * namespace.
	 * 
	 * @param ns
	 * 		namespace to bind
	 * @param pkg
	 * 		package instance
	 * @param version
	 * 		version of package
	 */
	void bind(Namespace ns, Package pkg, String version);
	
	/**
	 * Bind the given namespace to a package name.
	 * 
	 * @param ns
	 * 		namespace to bind
	 * @param pkgBase
	 * 		class from which the package name will be extracted 
	 */
	void bind(Namespace ns, Class<?> pkgBase);
	
	/**
	 * Bind the given namespace to a package name with a specific version.
	 * 
	 * @param ns
	 * 		namespace to bind
	 * @param pkgBase
	 * 		class from which the package name will be extracted
	 * @param version
	 * 		version of package
	 */
	void bind(Namespace ns, Class<?> pkgBase, String version);
	
	/**
	 * Bind the given namespace without tying it to a package. This allows
	 * it to be accessed via other method in the class but will not enable
	 * page and component detection for the namespace.
	 * 
	 * @param ns
	 * 		namespace to bind
	 */
	void bindSimple(Namespace ns);
	
	/**
	 * Bind the given namespace without tying it to a package. This allows
	 * it to be accessed via other method in the class but will not enable
	 * page and component detection for the namespace.
	 * 
	 * @param ns
	 * 		namespace to bind
	 */
	void bindSimple(Namespace ns, String version);
	
	/**
	 * Check if the namespace has been bound.
	 * 
	 * @param ns
	 * 		namespace to check
	 * @return
	 * 		{@code true} if bound, otherwise {@code false}
	 */
	boolean isBound(Namespace ns);
	
	/**
	 * Get the namespace of a package if any exists.
	 * 
	 * @param pkg
	 * 		package to get namespace for
	 * @return
	 * 		namespace if found, otherwise {@code null}
	 */
	Namespace getBinding(String pkg);
	
	/**
	 * Locate a namespace based on it's registered prefix.
	 * 
	 * @param prefix
	 * 		prefix to locate
	 * @return
	 */
	Namespace getNamespaceByPrefix(String prefix);

	Namespace getNamespaceByURI(String uri);
	
	URL getResource(Namespace ns, String resource);

	String getVersion(Namespace ns);
}

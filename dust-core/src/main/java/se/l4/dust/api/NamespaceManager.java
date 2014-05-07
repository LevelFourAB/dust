package se.l4.dust.api;

import java.net.URI;
import java.net.URL;

/**
 * The namespace manager is used to bind namespaces to packages to enable
 * automatic page and component discovery.
 * 
 * @author Andreas Holstenson
 *
 */
public interface NamespaceManager
	extends Iterable<NamespaceManager.Namespace>
{
	/**
	 * Binder for namespaces.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface NamespaceBinder
	{
		/**
		 * Set the package of the bound namespace.
		 * 
		 * @param pkg
		 * @return
		 */
		NamespaceBinder setPackage(String pkg);
		
		/**
		 * Set the package of the bound namespace.
		 * 
		 * @param pkg
		 * @return
		 */
		NamespaceBinder setPackage(Package pkg);
		
		/**
		 * Set the package of the namespace via the name of a class.
		 * 
		 * @param pkg
		 * @return
		 */
		NamespaceBinder setPackageFromClass(Class<?> type);
		
		/**
		 * Set the version of the namespace.
		 * 
		 * @param version
		 * @return
		 */
		NamespaceBinder setVersion(String version);
		
		/**
		 * Set the short prefix of the namespace. Prefixes are required for
		 * certain operations, such as serving assets.
		 * 
		 * @param prefix
		 * @return
		 */
		NamespaceBinder setPrefix(String prefix);
		
		/**
		 * Add a plugin that can add things to this namespace after it has
		 * been created.
		 * 
		 * @param plugin
		 * @return
		 */
		NamespaceBinder with(NamespacePlugin plugin);
		
		/**
		 * Bind the given namespace.
		 */
		void add();
	}
	
	/**
	 * Information about a namespace.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface Namespace
	{
		/**
		 * Get the prefix of the namespace.
		 * 
		 * @return
		 */
		String getPrefix();
		
		/**
		 * Get the URI of the namespace.
		 * 
		 * @return
		 */
		String getUri();
		
		/**
		 * Get the version of the namespace.
		 * 
		 * @return
		 */
		String getVersion();
		
		/**
		 * Attempt to locate a resource within the given namespace.
		 * 
		 * @param resource
		 * @return
		 */
		URL getResource(String resource);

		/**
		 * Resolve the URI of a certain resource. This will not check if
		 * the resource actually exists.
		 * 
		 * @param resource
		 * @return
		 */
		URI resolveResource(String resource);
		
		/**
		 * Get the Java package of this namespace.
		 * 
		 * @return
		 */
		String getPackage();

	}
	
	/**
	 * Bind the given namespace without tying it to a package. This allows
	 * it to be accessed via other method in the class but will not enable
	 * page and component detection for the namespace.
	 * 
	 * @param ns
	 * 		URI of namespace to bind
	 */
	NamespaceBinder bind(String nsUri);
	
	/**
	 * Check if the namespace has been bound.
	 * 
	 * @param ns
	 * 		namespace to check
	 * @return
	 * 		{@code true} if bound, otherwise {@code false}
	 */
	boolean isBound(String ns);
	
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
	 * Locate a namespace based on its registered prefix.
	 * 
	 * @param prefix
	 * 		prefix to locate
	 * @return
	 */
	Namespace getNamespaceByPrefix(String prefix);

	/**
	 * Locate a namespace based on its registered URI.
	 * 
	 * @param uri
	 * @return
	 */
	Namespace getNamespaceByURI(String uri);
}

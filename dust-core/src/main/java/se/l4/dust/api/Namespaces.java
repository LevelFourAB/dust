package se.l4.dust.api;


/**
 * The namespace manager is used to bind namespaces to packages to enable
 * automatic page and component discovery.
 *
 * @author Andreas Holstenson
 *
 */
public interface Namespaces
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
		 * Indicate that the package of this namespace needs to be
		 * manually set.
		 *
		 * @return
		 */
		NamespaceBinder manual();

		/**
		 * Bind the given namespace.
		 */
		void add();
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

	/**
	 * Find the namespace (if any) that the given type belongs to.
	 *
	 * @param c
	 * @return
	 */
	Namespace findNamespaceFor(Class<?> c);

	/**
	 * List all of the registered namespaces.
	 *
	 * @return
	 */
	Iterable<Namespace> list();

}

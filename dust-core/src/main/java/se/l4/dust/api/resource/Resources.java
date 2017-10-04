package se.l4.dust.api.resource;

import java.io.IOException;
import java.net.URL;

import se.l4.dust.api.Namespaces;

/**
 * Resources are always bound to a namespace, given a namespace and a file name the
 * manager looks through registered {@link ResourceLocator}s for the given resource.
 *
 * <h2>Classpath source</h2>
 * By default an {@link ResourceLocator} based on the classpath is registered, this
 * source will lookup the package name for a given namespace (via
 * {@link Namespaces}) and will then perform a lookup of the file
 * equivalent to calling {@link Package#getResource(String)} on a class located
 * in the package.
 *
 * @author Andreas Holstenson
 *
 */
public interface Resources
{
	/**
	 * Locate a resource within the given namespace.
	 *
	 * @param namespace
	 * @param file
	 * @return
	 * @throws IOException
	 */
	Resource locate(String namespace, String file)
		throws IOException;

	/**
	 * Locate a resource at the given URL.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	Resource locate(URL url)
		throws IOException;

	/**
	 * Locate the given resource as defined by the location.
	 *
	 * @param location
	 * @return
	 * @throws IOException
	 */
	Resource locate(ResourceLocation location)
		throws IOException;

	/**
	 * Add a source of assets to the manager.
	 *
	 * @param source
	 */
	void addLocator(ResourceLocator source);

}

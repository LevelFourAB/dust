package se.l4.dust.api;

import java.io.IOException;
import java.net.URL;

import se.l4.dust.api.resource.Resource;

/**
 * Information about a namespace.
 *
 * @author Andreas Holstenson
 *
 */
public interface Namespace
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
	 * @throws IOException
	 */
	Resource getResource(String resource)
		throws IOException;

	/**
	 * Attempt to locate a resource within the classpath of the namespace.
	 *
	 * @param resource
	 * @return
	 */
	URL getClasspathResource(String resource);

	/**
	 * Get the Java package of this namespace.
	 *
	 * @return
	 */
	String getPackage();

}

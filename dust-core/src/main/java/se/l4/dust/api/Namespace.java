package se.l4.dust.api;

import java.net.URI;
import java.net.URL;

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
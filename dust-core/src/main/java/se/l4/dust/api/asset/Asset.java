package se.l4.dust.api.asset;

import java.net.URL;

import org.jdom.Namespace;

/**
 * Usable asset on a page, assets can be used in templates to link to resources
 * such a CSS-files, Javascript and images. Assets are always tied to
 * namespaces.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Asset
{
	/**
	 * Get the namespace of the asset.
	 * 
	 * @return
	 */
	Namespace getNamespace();
	
	/**
	 * Get the name of the asset, should be unique within the namespace.
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Get the checksum of the asset, used for linking to the asset.
	 * 
	 * @return
	 */
	String getChecksum();
	
	/**
	 * Get a URL where the contents of the asset can be found.
	 * 
	 * @return
	 */
	URL getURL();
}

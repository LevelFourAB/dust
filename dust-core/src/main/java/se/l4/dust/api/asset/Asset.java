package se.l4.dust.api.asset;

import org.jdom.Namespace;

import se.l4.dust.api.resource.Resource;

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
	 * Get the resource with the contents of the asset.
	 * 
	 * @return
	 */
	Resource getResource();

	/**
	 * Get if access to the resource is protected.
	 * 
	 * @return
	 */
	boolean isProtected();
}

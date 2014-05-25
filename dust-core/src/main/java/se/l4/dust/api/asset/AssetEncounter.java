package se.l4.dust.api.asset;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.resource.Resource;

/**
 * Encounter with a {@link Resource} for creating an asset. Used by
 * {@link AssetProcessor asset processing}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface AssetEncounter
{
	/**
	 * Get the resource of the asset.
	 * 
	 * @return
	 */
	Resource getResource();
	
	/**
	 * Get the namespace URI of the asset.
	 * 
	 * @return
	 */
	String getNamepace();
	
	/**
	 * Get the namespace as an object.
	 * 
	 * @return
	 */
	Namespace getNamespaceObject();
	
	/**
	 * Get the path of the asset.
	 * 
	 * @return
	 */
	String getPath();
	
	/**
	 * Get if the system is in production mode.
	 * 
	 * @return
	 */
	boolean isProduction();
	
	/**
	 * Get the original resource as a cached instance. The identifier should
	 * be an identifier unique to the processor.
	 * 
	 * @param id
	 * @return
	 */
	Resource getCached(String id);
	
	/**
	 * Cache the given resource for usage later. The identifier should be
	 * an identifier unique to the processor and not the resource.
	 * 
	 * @param resource
	 * @return
	 */
	AssetEncounter cache(String id, Resource resource);
	
	/**
	 * Replace the resource with a new one.
	 * 
	 * @param resource
	 */
	AssetEncounter replaceWith(Resource resource);
	
	/**
	 * Rename the asset.
	 * 
	 * @param name
	 * @return
	 */
	AssetEncounter rename(String name);
}

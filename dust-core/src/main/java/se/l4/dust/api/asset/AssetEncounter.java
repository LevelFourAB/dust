package se.l4.dust.api.asset;

import java.util.List;

import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.ResourceLocation;

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
	 * Get the original location.
	 * 
	 * @return
	 */
	ResourceLocation getLocation();
	
	/**
	 * Get the combined resource of the asset.
	 * 
	 * @return
	 */
	Resource getResource();
	
	/**
	 * Get the resources of this asset.
	 * 
	 * @return
	 */
	List<Resource> getResources();
	
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
}

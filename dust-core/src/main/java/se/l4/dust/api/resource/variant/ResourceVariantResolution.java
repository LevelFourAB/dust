package se.l4.dust.api.resource.variant;

import se.l4.dust.api.resource.Resource;

public interface ResourceVariantResolution
{
	/**
	 * Get the resource of the variant.
	 * 
	 * @return
	 */
	Resource getResource();
	
	/**
	 * Get the name of the variant.
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Get the {@link ResourceVariant} that is active.
	 * 
	 * @return
	 */
	ResourceVariant getVariant();
}
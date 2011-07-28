package se.l4.dust.api.resource.variant;

import java.util.List;

import se.l4.dust.api.Context;

/**
 * Source of resource variants.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ResourceVariantSource
{
	/**
	 * Get variants this context matches.
	 * 
	 * @param ctx
	 * @return
	 */
	List<ResourceVariant> getVariants(Context ctx);
	
	/**
	 * Get a value to use for caching purposes from the context.
	 * 
	 * @param ctx
	 * @return
	 */
	Object getCacheValue(Context ctx);
}

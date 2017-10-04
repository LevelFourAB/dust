package se.l4.dust.api.resource.variant;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Supplier;

import se.l4.dust.api.Context;
import se.l4.dust.api.resource.ResourceLocation;

/**
 * Manager for working with resource variants.
 *
 * @author Andreas Holstenson
 *
 */
public interface ResourceVariantManager
{
	/**
	 * Add a new source of variants.
	 *
	 * @param source
	 */
	void addSource(ResourceVariantSource source);

	/**
	 * Get the variants for the given context.
	 *
	 * @param context
	 * @return
	 */
	List<ResourceVariant> getVariants(Context context);

	/**
	 * Get the initial contexts to use for caching.
	 *
	 * @return
	 */
	List<Context> getInitialContexts();

	/**
	 * Get a cache object suitable for storing data related to the specified
	 * context.
	 *
	 * @param context
	 * @return
	 */
	Object[] getCacheObject(Context context);

	/**
	 * Try creating a more specific URL based on the variants active in the
	 * given context.
	 *
	 * @param context
	 * @param location
	 * @return
	 * @throws IOException
	 */
	ResourceVariantResolution resolve(Context context, ResourceLocation location)
		throws IOException;

	/**
	 * Try creating a more specific URL based on the variants active in the
	 * given context.
	 *
	 * @param context
	 * @param location
	 * @return
	 * @throws IOException
	 */
	ResourceVariantResolution resolveNoCache(Context context, ResourceLocation location)
		throws IOException;

	/**
	 * Create a new combined resource. This method will resolve a combined
	 * {@link ResourceVariant} and the returned result will reflect the
	 * actual name of the resource.
	 *
	 * @param result
	 * @param location
	 * @return
	 */
	ResourceVariantResolution createCombined(List<ResourceVariantResolution> result, ResourceLocation location);

	/**
	 * Create a new combined resource. This method will resolve a combined
	 * {@link ResourceVariant} and the returned result will reflect the
	 * actual name of the resource.
	 *
	 * @param result
	 * @param location
	 * @return
	 */
	ResourceVariantResolution createCombined(Context context, ResourceLocation location, Supplier<List<ResourceVariantResolution>> resultSupplier);
}

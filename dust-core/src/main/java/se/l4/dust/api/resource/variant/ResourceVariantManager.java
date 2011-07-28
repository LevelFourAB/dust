package se.l4.dust.api.resource.variant;

import java.io.IOException;
import java.util.List;

import se.l4.dust.api.Context;

/**
 * Manager for working with resource variants.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ResourceVariantManager
{
	interface ResourceCallback
	{
		boolean exists(ResourceVariant variant, String url)
			throws IOException;
	}
	
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
	 * @param callback
	 * @param original
	 * @return
	 * @throws IOException 
	 */
	String resolve(Context context, ResourceCallback callback, String original)
		throws IOException;
	
	/**
	 * Try creating a more specific URL based on the variants active in the
	 * given context.
	 * 
	 * @param context
	 * @param callback
	 * @param original
	 * @return
	 * @throws IOException 
	 */
	String resolveNoCache(Context context, ResourceCallback callback, String original)
		throws IOException;
	
	/**
	 * Try creating a more specific URL based on the given variant.
	 * 
	 * @param context
	 * @param callback
	 * @param original
	 * @return
	 * @throws IOException 
	 */
	String resolve(ResourceVariant variant, ResourceCallback callback, String original)
		throws IOException;
	
	/**
	 * Resolve the real variant that was used for the specified resource.
	 * 
	 * @param context
	 * @param callback
	 * @param original
	 * @return
	 * @throws IOException 
	 */
	ResourceVariant resolveRealVariant(ResourceVariant variant, ResourceCallback callback, String original)
		throws IOException;
}

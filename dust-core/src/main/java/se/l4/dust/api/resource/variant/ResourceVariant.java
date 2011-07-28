package se.l4.dust.api.resource.variant;

/**
 * Information about a possible variant of a resource.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ResourceVariant
{
	static final String LOCALE = "locale";
	
	/**
	 * Get the cache value.
	 * 
	 * @return
	 */
	Object getCacheValue();
	
	/**
	 * Get the identifier of this variant.
	 * 
	 * @return
	 */
	String getIdentifier();
}

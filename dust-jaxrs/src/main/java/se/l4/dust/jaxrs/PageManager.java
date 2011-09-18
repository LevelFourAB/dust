package se.l4.dust.jaxrs;

import se.l4.dust.api.NamespaceManager;

/**
 * Manager of registered pages. Should only be used if {@link NamespaceManager}
 * is not used or if it does not function in your environment.
 * 
 * @author Andreas Holstenson
 *
 */
public interface PageManager
{
	/**
	 * Expose a new page, constructing it via dependency injection.
	 * 
	 * @param page
	 */
	PageManager add(Class<?> page);
	
	/**
	 * Get the total number of pages registered.
	 * 
	 * @return
	 */
	int getPageCount();
}

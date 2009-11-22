package se.l4.dust.api;

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
	void add(Class<?> page);
	
}

package se.l4.dust.api;

/**
 * Actual page provider that is tied into Resteasy.
 * 
 * @author Andreas Holstenson
 *
 */
public interface PageProvider
{
	/**
	 * Get an instance of the page, can either be per request basis or a
	 * singleton.
	 * 
	 * @return
	 */
	Object get();
	
	/**
	 * Release an object back to the provider, can be used to pool objects.
	 *
	 * @param o
	 * 		object to release
	 */
	void release(Object o);
	
	/**
	 * Get the class of the page.
	 * 
	 * @return
	 */
	Class<?> getType();
}

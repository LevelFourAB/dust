package se.l4.dust.api;

/**
 * Context information.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Context
{
	/**
	 * Store a value in the context.
	 * 
	 * @param key
	 * @param value
	 */
	void putValue(Object key, Object value);
	
	/**
	 * Get a value from the context.
	 * 
	 * @param key
	 * @return
	 */
	<T> T getValue(Object key);
}

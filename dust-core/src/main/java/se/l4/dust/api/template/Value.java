package se.l4.dust.api.template;

/**
 * Abstraction for getting a value that might be bound to both a
 * {@link RenderingContext} and an object.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface Value<T>
{
	/**
	 * Get the value.
	 * 
	 * @param context
	 * @param data
	 * @return
	 */
	T get(RenderingContext context, Object data);
	
	/**
	 * Set the value.
	 * 
	 * @param context
	 * @param data
	 * @param value
	 */
	void set(RenderingContext context, Object data, Object value);
	
	/**
	 * Get the type of this value.
	 * 
	 * @return
	 */
	Class<T> getType();
}

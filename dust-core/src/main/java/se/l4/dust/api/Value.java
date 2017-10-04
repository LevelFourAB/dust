package se.l4.dust.api;

import se.l4.dust.api.template.RenderingContext;


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
	T get(Context context, Object data);

	/**
	 * Check if this value supports {@link #get(Context, Object)}.
	 *
	 * @return
	 */
	boolean supportsGet();

	/**
	 * Set the value.
	 *
	 * @param context
	 * @param data
	 * @param value
	 */
	void set(Context context, Object data, Object value);

	/**
	 * Check if this value supports {@link #set(Context, Object, Object)}.
	 *
	 * @return
	 */
	boolean supportsSet();

	/**
	 * Get the type of this value.
	 *
	 * @return
	 */
	Class<? extends T> getType();
}

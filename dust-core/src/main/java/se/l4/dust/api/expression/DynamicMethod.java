package se.l4.dust.api.expression;

import se.l4.dust.api.Context;

/**
 * Dynamic method as used within expressions.
 * 
 * @author Andreas Holstenson
 *
 */
public interface DynamicMethod
{
	/**
	 * Invoke the method.
	 * 
	 * @param context
	 * 		context of invocation
	 * @param instance
	 * 		instance to invoke on
	 * @param parameters
	 * 		parameters of method
	 * @return
	 */
	Object invoke(Context context, Object instance, Object... parameters);
	
	/**
	 * Get the type of the return value.
	 * 
	 * @return
	 */
	Class<?> getType();
	
	/**
	 * Get if this property needs any context. If this method returns
	 * {@code false} the argument {@code instance} in {@link #invoke(Context, Object, Object...)}
	 * may be null.
	 * 
	 * <p>
	 * When this is false it allows us to optimize chained calls.
	 * 
	 * @return
	 */
	boolean needsContext();
}

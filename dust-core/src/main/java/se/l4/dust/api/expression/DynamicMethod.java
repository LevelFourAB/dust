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
}

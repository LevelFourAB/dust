package se.l4.dust.api.expression;

import se.l4.dust.api.Context;

/**
 * Expression as retrieved from {@link Expressions}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Expression
{
	/**
	 * Execute the expression using the given context and instance.
	 * 
	 * @param context
	 * 		context (such as rendering, etc)
	 * @param instance
	 * 		instance to execute on
	 * @return
	 */
	Object execute(Context context, Object instance);
}

package se.l4.dust.api.expression;

import se.l4.dust.api.Context;
import se.l4.dust.api.Value;

/**
 * Expression as retrieved from {@link Expressions}.
 *
 * @author Andreas Holstenson
 *
 */
public interface Expression
	extends Value<Object>
{
	/**
	 * Get the return class of the expression.
	 *
	 * @return
	 */
	@Override
	Class<?> getType();

	/**
	 * Execute the expression using the given context and instance.
	 *
	 * @param context
	 * 		context (such as rendering, etc)
	 * @param instance
	 * 		instance to execute on
	 * @return
	 */
	Object get(Context context, Object instance);

	/**
	 * Set the value of this expression (if possible).
	 *
	 * @param context
	 * @param instance
	 * @param value
	 */
	void set(Context context, Object instance, Object value);

	/**
	 * Get the source of this expression.
	 *
	 * @return
	 */
	String getSource();
}

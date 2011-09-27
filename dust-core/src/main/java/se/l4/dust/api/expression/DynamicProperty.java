package se.l4.dust.api.expression;

import se.l4.dust.api.Context;

/**
 * Dynamic property used within an expression.
 * 
 * @author Andreas Holstenson
 *
 */
public interface DynamicProperty
{
	/**
	 * Resolve the value of this property.
	 * 
	 * @param context
	 * @param root
	 * @return
	 */
	Object getValue(Context context, Object root);
	
	/**
	 * Set the value of this property.
	 * 
	 * @param context
	 * @param root
	 * @param value
	 */
	void setValue(Context context, Object root, Object value);
	
	/**
	 * Get the type of the result.
	 * 
	 * @return
	 */
	Class<?> getType();
}

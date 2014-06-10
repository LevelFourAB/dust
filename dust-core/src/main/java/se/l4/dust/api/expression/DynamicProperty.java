package se.l4.dust.api.expression;

import se.l4.dust.api.Context;
import se.l4.dust.api.Value;

/**
 * Dynamic property used within an expression.
 * 
 * @author Andreas Holstenson
 *
 */
public interface DynamicProperty<T>
	extends Value<T>
{
	/**
	 * Get if this property needs any context. If this method returns
	 * {@code false} the argument {@code root} in {@link #getValue(Context, Object)}
	 * and {@link #setValue(Context, Object, Object)} may be null.
	 * 
	 * <p>
	 * When this is false it allows us to optimize chains of dynamic
	 * properties.
	 * 
	 * @return
	 */
	boolean needsContext();
	
	/**
	 * Retrieve a property (if available).
	 * 
	 * @param encounter
	 * 		information about this expression encounter
	 * @param name
	 * 		name of the property
	 * @return
	 */
	DynamicProperty<?> getProperty(ExpressionEncounter encounter, String name);
	
	/**
	 * Resolve a method.
	 * 
	 * @param encounter
	 * 		information about this expression encounter
	 * @param name
	 * 		name of the method to resolve
	 * @param parameters
	 * 		input parameters
	 * @return
	 */
	DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters);
}

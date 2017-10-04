package se.l4.dust.api.expression;


/**
 * Source of properties and methods that are namespaced.
 *
 * @author Andreas Holstenson
 *
 */
public interface ExpressionSource
{
	/**
	 * Retrieve a property (if available).
	 *
	 * @param encounter
	 * 		information about this expression encounter
	 * @param name
	 * 		name of the property
	 * @return
	 */
	DynamicProperty getProperty(ExpressionEncounter encounter, String name);

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

package se.l4.dust.api.expression;

import java.util.Map;

import se.l4.dust.api.resource.ResourceLocation;


/**
 * Expression support.
 *
 * @author Andreas Holstenson
 *
 */
public interface Expressions
{
	/**
	 * Add a source of dynamic content.
	 *
	 * @param namespace
	 * @param source
	 */
	void addSource(String namespace, ExpressionSource source);

	/**
	 * Compile an expression.
	 * @param url
	 *
	 * @param expression
	 * @param context
	 * @return
	 */
	Expression compile(ResourceLocation source, Map<String, String> namespaces, String expression, Class<?> localContext);

	/**
	 * Resolve a suitable type for the given object.
	 *
	 * @param context
	 * @return
	 */
	Class<?> resolveType(Object context);
}

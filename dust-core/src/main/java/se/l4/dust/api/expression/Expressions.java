package se.l4.dust.api.expression;

import java.util.Map;


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
	 * 
	 * @param expression
	 * @param context
	 * @return
	 */
	Expression compile(Map<String, String> namespaces, String expression, Class<?> localContext);
}

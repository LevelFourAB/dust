package se.l4.dust.api.template;

import java.util.Set;

import se.l4.dust.api.Namespaces;
import se.l4.dust.api.Value;
import se.l4.dust.api.template.dom.Attribute;

public interface TemplateEncounter
{
	/**
	 * Get attributes as found in the template. These attributes will not
	 * include attributes that belong to a namespace that is bound via
	 * {@link Namespaces}. 
	 * 
	 * @return
	 */
	Attribute<? extends Object>[] getAttributes();
	
	/**
	 * Get attributes excluding certain ones.
	 * 
	 * @param names
	 * @return
	 */
	Attribute<? extends Object>[] getAttributesExcluding(String... names);
	
	/**
	 * Get attributes excluding certain ones.
	 * 
	 * @param names
	 * @return
	 */
	Attribute<? extends Object>[] getAttributesExcluding(Set<String> names);
	
	/**
	 * Get attributes belonging to the given namespace.
	 * 
	 * @return
	 */
	Attribute<? extends Object>[] getAttributes(String namespace);
	
	/**
	 * Get a specific attribute from the current element.
	 * 
	 * @param namespace
	 * @param name
	 * @return
	 */
	Attribute<? extends Object> getAttribute(String namespace, String name);
	
	/**
	 * Get a specific attribute from the current element and bind it to
	 * handle a specific type.
	 * 
	 * @param namespace
	 * @param name
	 * @param type
	 * @return
	 */
	<T> Attribute<T> getAttribute(String namespace, String name, Class<T> type);
	
	/**
	 * Get a specific attribute from the current element.
	 * 
	 * @param name
	 * @return
	 */
	Attribute<? extends Object> getAttribute(String name);
	
	/**
	 * Get a specific attribute and bind it to handle a specific type.
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	<T> Attribute<T> getAttribute(String name, Class<T> type);
	
	/**
	 * Get a specific attribute from the current element.
	 * 
	 * @param name
	 * @param required
	 * @return
	 */
	Attribute<? extends Object> getAttribute(String name, boolean required);
	
	/**
	 * Get a specific attribute and bind it to handle a specific type.
	 * 
	 * @param name
	 * @param type
	 * @param required
	 * @return
	 */
	<T> Attribute<T> getAttribute(String name, Class<T> type, boolean required);
	
	/**
	 * Find a parameter with the given name.
	 * 
	 * @param name
	 * @return
	 */
	Emittable findParameter(String name);
	
	/**
	 * Find a parameter with the given name.
	 * 
	 * @param name
	 * @return
	 */
	Emittable findParameter(String name, boolean required);
	
	/**
	 * Bind a namespace to the given prefix. This is then used then parsing
	 * expressions.
	 * 
	 * @param prefix
	 * @param uri
	 */
	void bindNamespace(String prefix, String uri);
	
	/**
	 * Parse an expression.
	 * 
	 * @param expression
	 * @return
	 */
	Value<?> parseExpression(String expression);
	
	/**
	 * Parse an expression on an alternative context.
	 * 
	 * @param expression
	 * @param context
	 * @return
	 */
	Value<?> parseExpression(String expression, Object context);
	
	/**
	 * Raise an error related to the processing of the fragment.
	 * 
	 * @param message
	 */
	void raiseError(String message);

}

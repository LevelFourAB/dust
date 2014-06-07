package se.l4.dust.api.template.mixin;

import java.util.List;

import se.l4.dust.api.Value;
import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.dom.Content;

/**
 * Encounter for mixing during template parsing. The encounter is triggered
 * when a mixin can be created, the mixin can then use the encounter to
 * modify the parsed template.
 * 
 * @author Andreas Holstenson
 *
 */
public interface MixinEncounter
{
	/**
	 * Get a specific attribute from the current element.
	 * 
	 * @param namespace
	 * @param name
	 * @return
	 */
	Attribute<? extends Object> getAttribute(String namespace, String name);
	
	/**
	 * Get a specific attribute and bind it to the given type.
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
	 * Get a specific attribute and bind it to the given type.
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	<T> Attribute<T> getAttribute(String name, Class<T> type);
	
	/**
	 * Bind an extra namespace for this encounter.
	 * 
	 * @param prefix
	 * @param uri
	 * @return
	 */
	MixinEncounter bindNamespace(String prefix, String uri);
	
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
	 * Wrap the current element with the specified wrapper.
	 * 
	 * @param wrapper
	 */
	void wrap(ElementWrapper wrapper);
	
	/**
	 * Insert content at the beginning of the current element.
	 * 
	 * @param content
	 */
	void prepend(Emittable... content);
	
	/**
	 * Insert content at the beginning of the current element, see
	 * {@link #prepend(Content...)}.
	 * 
	 * @param content
	 */
	void prepend(List<? extends Emittable> content);
	
	/**
	 * Insert content at the end of the current element.
	 * 
	 * @param content
	 */
	void append(Emittable... content);
	
	/**
	 * Insert content at the end of the current element, see
	 * {@link #append(Content...)}.
	 * 
	 * @param content
	 */
	void append(List<? extends Emittable> content);
	
	/**
	 * Set the value of a given attribute.
	 * 
	 * @param attribute
	 * @param content
	 */
	void setAttribute(String attribute, Value<?> content);

	/**
	 * Report an error with handling this mixin.
	 * 
	 * @param string
	 */
	void error(String error);
}

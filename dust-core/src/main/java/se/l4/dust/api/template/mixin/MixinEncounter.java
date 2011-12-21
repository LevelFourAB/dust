package se.l4.dust.api.template.mixin;

import java.util.List;

import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Element;

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
	Element.Attribute getAttribute(String namespace, String name);
	
	/**
	 * Get a specific attribute from the current element.
	 * 
	 * @param name
	 * @return
	 */
	Element.Attribute getAttribute(String name);
	
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
	Content parseExpression(String expression);
	
	/**
	 * Parse an expression on an alternative context.
	 * 
	 * @param expression
	 * @param context
	 * @return
	 */
	Content parseExpression(String expression, Object context);
	
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
	void prepend(Content... content);
	
	/**
	 * Insert content at the beginning of the current element, see
	 * {@link #prepend(Content...)}.
	 * 
	 * @param content
	 */
	void prepend(List<Content> content);
	
	/**
	 * Insert content at the end of the current element.
	 * 
	 * @param content
	 */
	void append(Content... content);
	
	/**
	 * Insert content at the end of the current element, see
	 * {@link #append(Content...)}.
	 * 
	 * @param content
	 */
	void append(List<Content> content);
	
	/**
	 * Set the value of a given attribute.
	 * 
	 * @param attribute
	 * @param content
	 */
	void setAttribute(String attribute, Content content);
}

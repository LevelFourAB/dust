package se.l4.dust.api.template.spi;

import java.util.List;

import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Text;

/**
 * Builder for templates, this is used by template sources to create template
 * instances that can later be rendered.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TemplateBuilder
{
	/**
	 * 
	 * @param name
	 * @param publicId
	 * @param systemId
	 * @return
	 */
	TemplateBuilder setDoctype(String name, String publicId, String systemId);
	
	/**
	 * Start a new element. The attributes are treated as a repetition of
	 * key and values.
	 * 
	 * @param name
	 * @param attributes
	 * @return
	 */
	TemplateBuilder startElement(String name, String... attributes);
	
	/**
	 * End the current element.
	 * 
	 * @return
	 */
	TemplateBuilder endElement();
	
	/**
	 * Start creating a component of the given name. The name will be 
	 * dynamically expanded.
	 * 
	 * @param name
	 * @param namespace
	 * @return
	 */
	TemplateBuilder startComponent(String name, String namespace);
	
	/**
	 * Start creating a component.
	 * 
	 * @param component
	 * @return
	 */
	TemplateBuilder startComponent(Class<?> component);
	
	/**
	 * End the current component.
	 * 
	 * @return
	 */
	TemplateBuilder endComponent();
	
	/**
	 * End the current element or component.
	 * 
	 * @return
	 */
	TemplateBuilder endCurrent();
	
	/**
	 * Set an attribute on the current element or component.
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	TemplateBuilder setAttribute(String name, String value);
	
	/**
	 * Set an attribute on the current element component, optionally expanding 
	 * it via the current variable tokenizer.
	 * 
	 * @param name
	 * @param value
	 * @param expand
	 * @return
	 */
	TemplateBuilder setAttribute(String name, String value, boolean expand);
	
	/**
	 * Set an attribute to a set of expanded values. The different values
	 * are combined at runtime to form the value of the item.
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	TemplateBuilder setAttribute(String name, Content... value);
	
	/**
	 * Set an attribute to a set of expanded values. The different values
	 * are combined at runtime to form the value of the item.
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	TemplateBuilder setAttribute(String name, List<Content> value);
	
	/**
	 * Start creating a component of the given name. The name will be 
	 * dynamically expanded using default namespace bindings.
	 * 
	 * @param name
	 * @return
	 */
	TemplateBuilder startComponent(String name);
	
	/**
	 * Add a comment to the template.
	 * 
	 * @param comment
	 * @return
	 */
	TemplateBuilder comment(String comment);
	
	/**
	 * Add a comment to the template.
	 * 
	 * @param content
	 * @return
	 */
	TemplateBuilder comment(List<Content> content);
	
	/**
	 * Add content to the current element. This is usally instances of
	 * {@link Text} and results from {@link #createDynamicContent(String, String)}.
	 * 
	 * @param content
	 * @return
	 */
	TemplateBuilder addContent(List<Content> content);
	
	/**
	 * Bind a namespace to a specific prefix.
	 * 
	 * @param prefix
	 * @param uri
	 * @return
	 */
	TemplateBuilder bindNamespace(String prefix, String uri);
	
	/**
	 * Unbind a specific namespace prefix.
	 * 
	 * @param prefix
	 * @return
	 */
	TemplateBuilder unbindNamespace(String prefix);
	
	/**
	 * Create a piece of dynamic content based on the prefix and given 
	 * expression.
	 * 
	 * @param prefix
	 * @param expression
	 * @return
	 */
	Content createDynamicContent(String prefix, String expression);
	
	/**
	 * Create a peice of dynamic content based on the given expression.
	 * 
	 * @param expression
	 * @return
	 */
	Content createDynamicContent(String expression);

	/**
	 * Get if the builder has started creating an element or component.
	 * 
	 * @return
	 */
	boolean hasCurrent();
}

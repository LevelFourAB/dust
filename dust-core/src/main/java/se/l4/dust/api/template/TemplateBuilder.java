package se.l4.dust.api.template;

import java.util.List;

import se.l4.dust.api.Value;
import se.l4.dust.api.template.fragment.TemplateFragment;
import se.l4.dust.api.template.spi.ErrorCollector;

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
	 * Set the error collector to use.
	 *
	 * @param collector
	 * @return
	 */
	TemplateBuilder withErrorCollector(ErrorCollector collector);

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
	 * Start a fragment based on a qualified name. The qualified name
	 * is on the form {@code prefix:name}, where the prefix should be
	 * a {@link #bindNamespace(String, String) bound namespace}.
	 *
	 * @param name
	 * @return
	 */
	TemplateBuilder startFragment(String name);

	/**
	 * Start a fragment based on a name and a namespace.
	 *
	 * @param name
	 * @param namespace
	 * @return
	 */
	TemplateBuilder startFragment(String name, String namespace);

	/**
	 * Start a fragment.
	 *
	 * @param fragment
	 * @return
	 */
	TemplateBuilder startFragment(TemplateFragment fragment);

	/**
	 * End the current fragment.
	 *
	 * @return
	 */
	TemplateBuilder endFragment();

	/**
	 * Start a new parameter.
	 *
	 * @param name
	 * @return
	 */
	TemplateBuilder startParameter(String name);

	/**
	 * End a parameter.
	 *
	 * @return
	 */
	TemplateBuilder endParameter();

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
	 * Set an attribute to a set of expanded values. The different values
	 * are combined at runtime to form the value of the item.
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	TemplateBuilder setAttribute(String name, Value<?>... values);

	/**
	 * Set an attribute to a set of expanded values. The different values
	 * are combined at runtime to form the value of the item.
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	TemplateBuilder setAttribute(String name, List<Value<?>> value);

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
	TemplateBuilder comment(Iterable<? extends Emittable> content);

	/**
	 * Add content to the current element.
	 *
	 * @param content
	 * @return
	 */
	TemplateBuilder addContent(Iterable<? extends Emittable> content);

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
	 * Create a piece of dynamic content based on the given expression.
	 *
	 * @param expression
	 * @return
	 */
	Value<?> createDynamicContent(String expression);

	/**
	 * Get if the builder has started creating an element or component.
	 *
	 * @return
	 */
	boolean hasCurrent();

	/**
	 * Get a value that has been previously set on the builder.
	 *
	 * @param id
	 * @return
	 */
	<T> T getValue(String id);

	/**
	 * Store a temporary value in this builder.
	 *
	 * @param id
	 * @param value
	 */
	void putValue(String id, Object value);

	/**
	 * Add a debug hint to this builder.
	 *
	 * @param line
	 * @param column
	 * @return
	 */
	TemplateBuilder addDebugHint(int line, int column);
}

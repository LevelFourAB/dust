package se.l4.dust.api.template.fragment;

import java.util.Set;

import se.l4.dust.api.Namespaces;
import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateBuilder;
import se.l4.dust.api.template.dom.Attribute;

/**
 * Information passed to {@link TemplateFragment} when it is encountered.
 * 
 * @author Andreas Holstenson
 *
 */
public interface FragmentEncounter
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
	 * Get content in the body of the fragment.
	 * 
	 * @return
	 */
	Emittable[] getBody();
	
	/**
	 * Similar to {@link #getBody()} but will ensure that the content is
	 * tied to the data of the origin template. This should be used if the
	 * content is passed to another component. 
	 * 
	 * @return
	 */
	Emittable getScopedBody();
	
	/**
	 * Get the template builder used for emitting fragment contents.
	 * 
	 * @return
	 */
	TemplateBuilder builder();
	
	/**
	 * Replace this fragment with something {@link Emittable that can be emitted}.
	 * 
	 * @param component
	 */
	void replaceWith(Emittable emittable);
	
	void replaceWith(Emittable[] content);
	
	void replaceWith(Iterable<? extends Emittable> content);

	/**
	 * Add a parameter to the current element.
	 * 
	 * @param name
	 * @param scopedBody
	 */
	void addParameter(String name, Emittable scopedBody);
	
	/**
	 * Raise an error related to the processing of the fragment.
	 * 
	 * @param message
	 */
	void raiseError(String message);
}

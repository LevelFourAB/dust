package se.l4.dust.api.template.spi;

import java.util.Collection;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Element;

/**
 * Information passed to {@link TemplateFragment} when it is encountered.
 * 
 * @author Andreas Holstenson
 *
 */
public interface FragmentEncounter
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
	 * Get a specific attribute from the current element.
	 * 
	 * @param name
	 * @param required
	 * @return
	 */
	Element.Attribute getAttribute(String name, boolean required);
	
	/**
	 * Find a parameter with the given name.
	 * 
	 * @param name
	 * @return
	 */
	Element findParameter(String name);
	
	/**
	 * Get content in the body of the fragment.
	 * 
	 * @return
	 */
	Content[] getBody();
	
	/**
	 * Similar to {@link #getBody()} but will ensure that the content is
	 * tied to the data of the origin template. This should be used if the
	 * content is passed to another component. 
	 * 
	 * @return
	 */
	Element getScopedBody();
	
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
	
	void replaceWith(Content[] content);
	
	void replaceWith(Iterable<Content> content);

	/**
	 * Add a parameter to the current element.
	 * 
	 * @param name
	 * @param scopedBody
	 */
	void addParameter(String name, Element scopedBody);
	
	/**
	 * Raise an error related to the processing of the fragment.
	 * 
	 * @param message
	 */
	void raiseError(String message);
}

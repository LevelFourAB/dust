package se.l4.dust.api.template.spi;

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
	 * Get the template builder used for emitting fragment contents.
	 * 
	 * @return
	 */
	TemplateBuilder builder();
	
	/**
	 * Replace this fragment with a component.
	 * 
	 * @param component
	 */
	void replaceWith(Object component);
}

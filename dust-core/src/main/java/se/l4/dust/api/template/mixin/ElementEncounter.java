package se.l4.dust.api.template.mixin;

import se.l4.dust.api.template.RenderingContext;

/**
 * Encounter when rendering an element.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ElementEncounter
{
	/**
	 * Get the rendering context.
	 * 
	 * @return
	 */
	RenderingContext getContext();
	
	/**
	 * Get the object being rendered.
	 * 
	 * @return
	 */
	Object getObject();
	
	/**
	 * Skip the current element.
	 * 
	 * @return
	 */
	void skip();
}

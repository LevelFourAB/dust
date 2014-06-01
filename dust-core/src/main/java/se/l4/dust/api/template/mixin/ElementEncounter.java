package se.l4.dust.api.template.mixin;

import java.io.IOException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateOutputStream;

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
	
	/**
	 * Manually emit the wrapped content.
	 * 
	 * @throws IOException 
	 * 
	 */
	void emit()
		throws IOException;
	
	/**
	 * Get the output used for rendering. This should be used with care,
	 * no extra actions are taken to ensure that the output is valid HTML
	 * when this is used directly.
	 * 
	 * @return
	 */
	TemplateOutputStream getOutput();
}

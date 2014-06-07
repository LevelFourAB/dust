package se.l4.dust.api.template;

import java.io.IOException;

import se.l4.dust.api.template.dom.Attribute;
/**
 * Helper for {@link Emittable}s.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TemplateEmitter
{
	/**
	 * Get the object currently being emitted.
	 * 
	 * @return
	 */
	Object getObject();

	/**
	 * Get the {@link RenderingContext}.
	 * 
	 * @return
	 */
	RenderingContext getContext();

	/**
	 * Create an array of attributes for the given element.
	 * 
	 * @param element
	 * @return
	 */
	String[] createAttributes(Attribute[] attributes);

	/**
	 * Emit the given contents.
	 * 
	 * @param c
	 * @throws IOException
	 */
	void emit(Emittable emittable)
		throws IOException;
	
	/**
	 * Emit the given contents.
	 * 
	 * @param emittables
	 * @throws IOException
	 */
	void emit(Emittable[] emittables)
		throws IOException;
}

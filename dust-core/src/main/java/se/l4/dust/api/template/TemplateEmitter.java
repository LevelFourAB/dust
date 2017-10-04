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
	String[] createAttributes(Attribute<String>[] attributes);

	/**
	 * Create an array of attributes for the given single attribute.
	 *
	 * @param attr1
	 * @param value1
	 * @return
	 */
	String[] createAttributes(String attr1, Object value1);

	String[] createAttributes(String attr1, Object value1, String attr2, Object value2);

	String[] createAttributes(String attr1, Object value1,
			String attr2, Object value2,
			String attr3, Object value3);

	/**
	 * Get an active parameter.
	 *
	 * @param name
	 * @return
	 */
	Emittable getParameter(String name);

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

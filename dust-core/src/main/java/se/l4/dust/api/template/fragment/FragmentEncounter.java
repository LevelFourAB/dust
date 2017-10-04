package se.l4.dust.api.template.fragment;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEncounter;

/**
 * Information passed to {@link TemplateFragment} when it is encountered.
 *
 * @author Andreas Holstenson
 *
 */
public interface FragmentEncounter
	extends TemplateEncounter
{
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
	 * Replace this fragment with something {@link Emittable that can be emitted}.
	 *
	 * @param component
	 */
	void replaceWith(Emittable emittable);

	/**
	 * Replace this fragment with several {@link Emittable}s.
	 *
	 * @param content
	 */
	void replaceWith(Emittable[] content);

	/**
	 * Replace this fragment with several {@link Emittable}s.
	 *
	 * @param content
	 */
	void replaceWith(Iterable<? extends Emittable> content);

	/**
	 * Add a parameter to the current element.
	 *
	 * @param name
	 * @param scopedBody
	 */
	void addParameter(String name, Emittable scopedBody);

	/**
	 * Temporarily store a value that other fragments can access later on.
	 *
	 * @param key
	 * @param value
	 */
	void putValue(String key, Object value);

	/**
	 * Get a value previously stored with {@link #putValue(String, Object)}.
	 *
	 * @param key
	 */
	<T> T getValue(String key);
}

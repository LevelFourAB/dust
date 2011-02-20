package se.l4.dust.api.template;

import java.lang.annotation.Annotation;
import java.net.URI;

/**
 * Context of a template rendering. The context is used to resolve links,
 * assets and to store temporary variables.
 * 
 * @author Andreas Holstenson
 *
 */
public interface RenderingContext
{
	/**
	 * Store a value in the context.
	 * 
	 * @param key
	 * @param value
	 */
	void putValue(Object key, Object value);
	
	/**
	 * Get a value from the context.
	 * 
	 * @param key
	 * @return
	 */
	<T> T getValue(Object key);
	
	/**
	 * Resolve a URI for the given object.
	 * 
	 * @param object
	 * @return
	 */
	URI resolveURI(Object object);
	
	/**
	 * Resolve an object of the specified type.
	 * 
	 * @param type
	 * @param annotations
	 * @return
	 */
	Object resolveObject(Class<?> type, Annotation[] annotations);
}

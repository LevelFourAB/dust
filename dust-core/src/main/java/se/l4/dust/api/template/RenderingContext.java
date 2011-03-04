package se.l4.dust.api.template;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
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
	 * @param parameter
	 * 		the parameter the object is for (if any)
	 * @param type
	 * 		the type of the created object
	 * @param annotations
	 * 		annotations placed on the parameter
	 * @param instance
	 * 		the instance to create the object for (owner of parameter)
	 * @return
	 */
	Object resolveObject(AccessibleObject parameter, Type type,
		Annotation[] annotations, Object instance
	);
}

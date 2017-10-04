package se.l4.dust.api.template;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;

/**
 * Implementation of {@link RenderingContext} that does not handle any URI
 * resolving.
 *
 * @author Andreas Holstenson
 *
 */
public class DefaultRenderingContext
	implements RenderingContext
{
	private final Map<Object, Object> values;

	@Inject
	public DefaultRenderingContext()
	{
		values = new HashMap<>();
	}

	@Override
	public Object getValue(Object key)
	{
		return values.get(key);
	}

	@Override
	public void putValue(Object key, Object value)
	{
		values.put(key, value);
	}

	@Override
	public URI resolveURI(Object object)
	{
		return null;
	}

	@Override
	public Object resolveObject(AccessibleObject parameter, Type type,
			Annotation[] annotations, Object instance)
	{
		return null;
	}
}

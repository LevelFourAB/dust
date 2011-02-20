package se.l4.dust.api.template;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import se.l4.dust.api.NamespaceManager;

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
	private final NamespaceManager namespaceManager;
	private final Map<Object, Object> values;

	@Inject
	public DefaultRenderingContext(NamespaceManager namespaceManager)
	{
		this.namespaceManager = namespaceManager;
		values = new HashMap<Object, Object>();
	}
	
	public Object getValue(Object key)
	{
		return values.get(key);
	}
	
	public void putValue(Object key, Object value)
	{
		values.put(key, value);
	}
	
	public URI resolveURI(Object object)
	{
		return null;
	}
	
	public Object resolveObject(Class<?> type, Annotation[] annotations)
	{
		return null;
	}
}

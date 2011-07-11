package se.l4.dust.api.template;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.dom.DynamicContent;

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
	protected final NamespaceManager namespaceManager;
	protected final TypeConverter converter;
	private final Map<Object, Object> values;

	@Inject
	public DefaultRenderingContext(NamespaceManager namespaceManager, TypeConverter converter)
	{
		this.namespaceManager = namespaceManager;
		this.converter = converter;
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
	
	public Object getDynamicValue(DynamicContent content, Object root)
	{
		return content.getValue(this, root);
	}
	
	public String getStringValue(Object input)
	{
		if(converter.canConvertBetween(input, String.class))
		{
			return converter.convert(input, String.class);
		}
		else
		{
			return String.valueOf(input);
		}
	}
	
	public URI resolveURI(Object object)
	{
		return null;
	}
	
	public Object resolveObject(AccessibleObject parameter, Type type, 
			Annotation[] annotations, Object instance)
	{
		return null;
	}
}

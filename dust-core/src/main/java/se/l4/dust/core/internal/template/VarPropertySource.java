package se.l4.dust.core.internal.template;

import com.google.inject.Singleton;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.spi.PropertySource;

/**
 * Property source for dynamic variables within a template. These are 
 * useful in loops to avoid creating a getter and a setter. Instead one can
 * use ${var:name} for the value-attribute.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class VarPropertySource
	implements PropertySource
{
	public VarPropertySource()
	{
	}
	
	public DynamicContent getPropertyContent(Class<?> context, String propertyExpression, Element parent)
	{
		return new Content(propertyExpression);
	}

	public static class Content
		extends DynamicContent
	{
		private final String key;

		public Content(String key)
		{
			this.key = "var:" + key;
		}
		
		@Override
		public Object getValue(RenderingContext ctx, Object root)
		{
			return ctx.getValue(key);
		}

		@Override
		public void setValue(RenderingContext ctx, Object root, Object data)
		{
			ctx.putValue(key, data);
		}
	}
}

package se.l4.dust.core.internal.template;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.spi.PropertySource;
import se.l4.dust.api.template.spi.TemplateInfo;

import com.google.inject.Singleton;

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
	
	public DynamicContent getPropertyContent(TemplateInfo namespaces, Class<?> context, String propertyExpression)
	{
		return new Content(propertyExpression);
	}

	public static class Content
		extends DynamicContent
	{
		private final String key;
		private final String compoundKey;

		public Content(String key)
		{
			this.key = key;
			this.compoundKey = "var:" + key;
		}
		
		@Override
		public Object getValue(RenderingContext ctx, Object root)
		{
			return ctx.getValue(compoundKey);
		}

		@Override
		public void setValue(RenderingContext ctx, Object root, Object data)
		{
			ctx.putValue(compoundKey, data);
		}
		
		public se.l4.dust.api.template.dom.Content copy()
		{
			return new Content(key);
		}
	}
}

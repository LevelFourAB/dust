package se.l4.dust.core.internal.template;

import com.google.inject.Singleton;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.spi.Namespaces;
import se.l4.dust.api.template.spi.PropertySource;

@Singleton
public class CyclePropertySource
	implements PropertySource
{
	public CyclePropertySource()
	{
	}
	
	public DynamicContent getPropertyContent(Namespaces namespaces, Class<?> context, String propertyExpression)
	{
		String[] parts = propertyExpression.split("\\s*,\\s*");
		
		return new Content(parts);
	}

	public static class Content
		extends DynamicContent
	{
		private final String[] parts;

		public Content(String[] parts)
		{
			this.parts = parts;
		}
		
		@Override
		public Object getValue(RenderingContext ctx, Object root)
		{
			Integer count = ctx.getValue(this);
			if(count == null)
			{
				count = 0;
			}
			
			String value = parts[count];
			
			if(count + 1 < parts.length)
			{
				count += 1;
			}
			else
			{
				count = 0;
			}
			
			ctx.putValue(this, count);
			
			return value;
		}
		
		@Override
		public void setValue(RenderingContext ctx, Object root, Object data)
		{
			throw new UnsupportedOperationException("setValue can not be done on cycle bindings");
		}
	}
}

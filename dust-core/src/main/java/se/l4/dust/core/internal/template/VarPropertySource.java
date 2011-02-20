package se.l4.dust.core.internal.template;

import se.l4.dust.api.template.PropertyContent;
import se.l4.dust.api.template.PropertySource;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.core.internal.template.dom.ExpressionNode;
import se.l4.dust.core.internal.template.dom.ExpressionParser;
import se.l4.dust.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Property source for dynamic variables within a template. These are 
 * useful in loops to avoid creating a getter and a setter. Instead one can
 * use ${var:name} for the value-attribute. It is possible to call methods
 * on this variable by doing e.g. ${var:name.substring(4)}.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class VarPropertySource
	implements PropertySource
{
	private final ExpressionParser parser;

	@Inject
	public VarPropertySource(ExpressionParser parser)
	{
		this.parser = parser;
	}
	
	public PropertyContent getPropertyContent(String propertyExpression,
			Element parent)
	{
		int dot = propertyExpression.indexOf('.');
		if(dot > 0)
		{
			// This is a compound variable, there is a call made on the object
			Content c = new Content(propertyExpression.substring(0, dot));
			ExpressionNode node = parser.parseExpression(null, null, propertyExpression.substring(dot + 1));
			
			return new CompoundContent(c, node);
		}
		else
		{
			return new Content(propertyExpression);
		}
	}

	public static class Content
		extends PropertyContent
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
	
	public static class CompoundContent
		extends PropertyContent
	{
		private final PropertyContent c1;
		private final PropertyContent c2;

		public CompoundContent(PropertyContent c1, PropertyContent c2)
		{
			this.c1 = c1;
			this.c2 = c2;
		}
		
		@Override
		public Object getValue(RenderingContext ctx, Object root)
		{
			Object value = c1.getValue(ctx, root);
			return c2.getValue(ctx, value);
		}
		
		@Override
		public void setValue(RenderingContext ctx, Object root, Object data)
		{
			throw new UnsupportedOperationException("setValue can not be done on variables with method calls");
		}
	}
}

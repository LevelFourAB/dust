package se.l4.dust.core.internal.template;

import java.util.HashMap;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import se.l4.dust.api.annotation.RequestScoped;
import se.l4.dust.api.template.PropertyContent;
import se.l4.dust.api.template.PropertySource;
import se.l4.dust.core.internal.template.dom.ExpressionNode;
import se.l4.dust.core.internal.template.dom.ExpressionParser;
import se.l4.dust.dom.Element;

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
	private final Provider<ValueHolder> provider;
	private final ExpressionParser parser;

	@Inject
	public VarPropertySource(Provider<ValueHolder> provider, ExpressionParser parser)
	{
		this.provider = provider;
		this.parser = parser;
	}
	
	public PropertyContent getPropertyContent(String propertyExpression,
			Element parent)
	{
		int dot = propertyExpression.indexOf('.');
		if(dot > 0)
		{
			// This is a compound variable, there is a call made on the object
			Content c = new Content(provider, propertyExpression.substring(0, dot));
			ExpressionNode node = parser.parseExpression(null, null, propertyExpression.substring(dot + 1));
			
			return new CompoundContent(c, node);
		}
		else
		{
			return new Content(provider, propertyExpression);
		}
	}

	public static class Content
		extends PropertyContent
	{
		private final Provider<ValueHolder> provider;
		private final String key;

		public Content(Provider<ValueHolder> provider, String key)
		{
			this.provider = provider;
			this.key = key;
		}
		
		@Override
		public Object getValue(Object root)
		{
			return provider.get().get(key);
		}

		@Override
		public void setValue(Object root, Object data)
		{
			provider.get().put(key, data);
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
		public Object getValue(Object root)
		{
			Object value = c1.getValue(root);
			return c2.getValue(value);
		}
		
		@Override
		public void setValue(Object root, Object data)
		{
			throw new UnsupportedOperationException("setValue can not be done on variables with method calls");
		}
	}
	
	@RequestScoped
	public static class ValueHolder
		extends HashMap<String, Object>
	{
		public ValueHolder()
		{
		}
	}
}

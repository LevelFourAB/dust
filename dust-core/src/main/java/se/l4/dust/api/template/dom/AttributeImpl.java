package se.l4.dust.api.template.dom;

import se.l4.dust.api.conversion.Conversion;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateException;

public class AttributeImpl
	implements Attribute<Object>
{
	public static final String ATTR_EMIT = "##emit";
	public static final String ATTR_SKIP = "##skip";
	
	private final String name;
	private final Content[] value;

	public AttributeImpl(String name, Content... value)
	{
		this.name = name;
		this.value = value;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	public Content[] getValue()
	{
		return value;
	}
	
	@Override
	public Object get(RenderingContext ctx, Object root)
	{
		if(value.length == 1)
		{
			Content c = value[0];
			return getValueOf(ctx, root, c);
		}
		else
		{
			StringBuilder result = new StringBuilder();
			for(Content c : value)
			{
				Object value = getValueOf(ctx, root, c);
				result.append(ctx.getStringValue(value));
			}
			
			return result.toString();
		}
	}
	
	@Override
	public Class<? extends Object> getType()
	{
		if(value.length == 1 && value[0] instanceof DynamicContent)
		{
			return ((DynamicContent) value[0]).getValueType();
		}
		
		return String.class;
	}

	private Object getValueOf(RenderingContext ctx, Object root, Content c)
	{
		if(c instanceof DynamicContent)
		{
			return ctx.getDynamicValue((DynamicContent) c, root);
		}
		else if(c instanceof Text)
		{
			return ((Text) c).getText();
		}
		else
		{
			return "";
		}
	}

	public String getStringValue(RenderingContext ctx, Object root)
	{
		if(value.length == 1)
		{
			Content c = value[0];
			return ctx.getStringValue(getValueOf(ctx, root, c));
		}
		else
		{
			StringBuilder result = new StringBuilder();
			for(Content c : value)
			{
				Object value = getValueOf(ctx, root, c);
				result.append(ctx.getStringValue(value));
			}
			
			return result.toString();
		}
	}

	@Override
	public String getStringValue()
	{
		StringBuilder builder = new StringBuilder();
		for(Content c : value)
		{
			if(c instanceof Text)
			{
				builder.append(((Text) c).getText());
			}
		}
		
		return builder.toString();
	}
	
	@Override
	public void set(RenderingContext ctx, Object root, Object value)
	{
		if(this.value.length != 1)
		{
			throw new TemplateException("Unable to set value of attribute " + name + ", contains more than one expression");
		}
		
		Content c = this.value[0];
		if(c instanceof DynamicContent)
		{
			((DynamicContent) c).setValue(ctx, root, value);
		}
		else
		{
			throw new TemplateException("Unable to set value of attribute " + name + ", does not contain any dynamic content");
		}
	}
	
	/**
	 * Bind this attribute to the given conversion.
	 * 
	 * @param conversion
	 * @return
	 */
	public <T> Attribute<T> bindTo(
			final Class<T> type,
			final Conversion<Object, T> get,
			final Conversion<Object, ?> set)
	{
		return new Attribute<T>()
		{
			@Override
			public String getName()
			{
				return name;
			}
			
			@Override
			public T get(RenderingContext context, Object data)
			{
				Object value = AttributeImpl.this.get(context, data);
				return get.convert(value);
			}
			
			@Override
			public void set(RenderingContext context, Object data, Object value)
			{
				Object object = set.convert(value);
				AttributeImpl.this.set(context, data, object);
			}
			
			@Override
			public Class<T> getType()
			{
				return type;
			}
			
			@Override
			public String getStringValue()
			{
				return getStringValue();
			}
			
			@Override
			public <T> Attribute<T> bindVia(TypeConverter converter, Class<T> output)
			{
				throw new UnsupportedOperationException("Already bound");
			}
		};
	}
	
	@Override
	public <T> Attribute<T> bindVia(TypeConverter converter, Class<T> output)
	{
		// If we are binding to the same type
		if(output == getType()) return (Attribute<T>) this;
		
		return bindTo(
			output,
			converter.getDynamicConversion(getType(), output),
			converter.getDynamicConversion(output, getType())
		);
	}
}
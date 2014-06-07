package se.l4.dust.api.template.dom;

import se.l4.dust.api.Context;
import se.l4.dust.api.Value;
import se.l4.dust.api.Values;
import se.l4.dust.api.conversion.Conversion;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.TemplateException;

public class AttributeImpl
	implements Attribute<Object>
{
	public static final String ATTR_EMIT = "##emit";
	public static final String ATTR_SKIP = "##skip";
	
	private final String name;
	private final Value<?>[] value;

	public AttributeImpl(String name, Value<?>... value)
	{
		this.name = name;
		this.value = value;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	public Value<?>[] getValue()
	{
		return value;
	}
	
	@Override
	public Object get(Context ctx, Object root)
	{
		if(value.length == 1)
		{
			return value[0].get(ctx, root);
		}
		else
		{
			StringBuilder result = new StringBuilder();
			for(Value<?> c : value)
			{
				result.append(c.get(ctx, root));
			}
			
			return result.toString();
		}
	}
	
	@Override
	public Class<? extends Object> getType()
	{
		if(value.length == 1)
		{
			return value[0].getType();
		}
		
		return String.class;
	}


	@Override
	public String getStringValue()
	{
		if(value.length == 1)
		{
			Value<?> v = value[0];
			if(v instanceof Values.StaticValue)
			{
				return (String) v.get(null, null);
			}
			
			return "";
		}
		else
		{
			StringBuilder builder = new StringBuilder();
			for(Value<?> v : value)
			{
				if(v instanceof Values.StaticValue)
				{
					builder.append(v.get(null, null));
				}
			}
			
			return builder.toString();
		}
	}
	
	@Override
	public void set(Context ctx, Object root, Object value)
	{
		if(this.value.length != 1)
		{
			throw new TemplateException("Unable to set value of attribute " + name + ", contains more than one expression");
		}
		
		Value<?> c = this.value[0];
		c.set(ctx, root, value);
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
			public T get(Context context, Object data)
			{
				Object value = AttributeImpl.this.get(context, data);
				return get.convert(value);
			}
			
			@Override
			public void set(Context context, Object data, Object value)
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
	
	private Value<String> convertToString(final Value<?> value, TypeConverter converter)
	{
		if(value.getType() == String.class) return (Value) value;
		
		final Conversion<Object, String> conversion = converter.getDynamicConversion(value.getType(), String.class);
		return new Value<String>()
		{
			@Override
			public Class<? extends String> getType()
			{
				return String.class;
			}
			
			@Override
			public String get(Context context, Object data)
			{
				Object object = value.get(context, data);
				return conversion.convert(object);
			}
			
			@Override
			public void set(Context context, Object data, Object value)
			{
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public <T> Attribute<T> bindVia(TypeConverter converter, Class<T> output)
	{
		Class<?> type = getType();
		if(output == String.class && type == output)
		{
			// Special case, convert every value
			Value<?>[] newValues = new Value[this.value.length];
			for(int i=0, n=newValues.length; i<n; i++)
			{
				Value<?> v = this.value[i];
				newValues[i] = convertToString(v, converter);
			}
			
			return (Attribute<T>) new AttributeImpl(name, newValues);
		}
		
		// If we are binding to the same type
		if(output == getType()) return (Attribute<T>) this;
		
		return bindTo(
			output,
			converter.getDynamicConversion(getType(), output),
			converter.getDynamicConversion(output, getType())
		);
	}
}
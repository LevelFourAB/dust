package se.l4.dust.api.template.dom;

import java.util.Arrays;

import se.l4.dust.api.Context;
import se.l4.dust.api.Value;
import se.l4.dust.api.Values;
import se.l4.dust.api.conversion.Conversion;
import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.TemplateException;

public class AttributeImpl
	implements Attribute<Object>
{
	private final String name;
	private final Value<?>[] values;

	public AttributeImpl(String name, Value<?>... value)
	{
		this.name = name;
		this.values = value;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public Value<?>[] getValue()
	{
		return values;
	}

	@Override
	public Object get(Context ctx, Object root)
	{
		if(values.length == 1)
		{
			return values[0].get(ctx, root);
		}
		else
		{
			StringBuilder result = new StringBuilder();
			for(Value<?> c : values)
			{
				result.append(c.get(ctx, root));
			}

			return result.toString();
		}
	}

	@Override
	public boolean supportsGet()
	{
		for(Value<?> v : values)
		{
			if(! v.supportsGet()) return false;
		}

		return true;
	}

	@Override
	public Class<? extends Object> getType()
	{
		if(values.length == 1)
		{
			return values[0].getType();
		}

		return String.class;
	}


	@Override
	public String getStringValue()
	{
		if(values.length == 1)
		{
			Value<?> v = values[0];
			if(v instanceof Values.StaticValue)
			{
				return (String) v.get(null, null);
			}

			return "";
		}
		else
		{
			StringBuilder builder = new StringBuilder();
			for(Value<?> v : values)
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
		if(this.values.length != 1)
		{
			throw new TemplateException("Unable to set value of attribute " + name + ", contains more than one expression");
		}

		Value<?> c = this.values[0];
		c.set(ctx, root, value);
	}

	@Override
	public boolean supportsSet()
	{
		if(this.values.length != 1) return false;

		return values[0].supportsSet();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Value<String> convertToString(final Value<?> value, TypeConverter converter)
	{
		if(value.getType() == String.class) return (Value) value;

		final Conversion<Object, String> conversion = converter.getDynamicConversion(value.getType(), String.class);
		return new StringValue(value, conversion);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Attribute<T> bindVia(TypeConverter converter, Class<T> output)
	{
		Class<?> type = getType();
		if(output == String.class && type == output)
		{
			// Special case, convert every value
			Value<?>[] newValues = new Value[this.values.length];
			for(int i=0, n=newValues.length; i<n; i++)
			{
				Value<?> v = this.values[i];
				newValues[i] = convertToString(v, converter);
			}

			return (Attribute<T>) new AttributeImpl(name, newValues);
		}

		// If we are binding to the same type
		if(output == getType()) return (Attribute<T>) this;

		NonGenericConversion<Object, T> get = supportsGet()
			? converter.getDynamicConversion(getType(), output)
			: null;
		NonGenericConversion<Object, ? extends Object> set = supportsSet()
			? converter.getDynamicConversion(output, getType())
			: null;

		return (Attribute<T>) new BoundAttribute<>(name, values, output, get, set);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{name=" + name + ", content=" + Arrays.toString(values) + "}";
	}

	private static class StringValue
		implements Value<String>
	{
		private final Value<?> value;
		private final Conversion<Object, String> conversion;

		private StringValue(Value<?> value, Conversion<Object, String> conversion)
		{
			this.value = value;
			this.conversion = conversion;
		}

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
		public boolean supportsGet()
		{
			return true;
		}

		@Override
		public void set(Context context, Object data, Object value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean supportsSet()
		{
			return false;
		}
	}

	private static class BoundAttribute<T>
		extends AttributeImpl
	{
		private final Class<T> type;
		private final Conversion<Object, T> get;
		private final Conversion<Object, ?> set;

		private BoundAttribute(String name,
				Value<?>[] values,
				Class<T> type,
				Conversion<Object, T> get,
				Conversion<Object, ?> set)
		{
			super(name, values);

			this.type = type;
			this.get = get;
			this.set = set;
		}

		@Override
		public T get(Context context, Object data)
		{
			Object value = super.get(context, data);
			return get.convert(value);
		}

		@Override
		public void set(Context context, Object data, Object value)
		{
			Object object = set.convert(value);
			super.set(context, data, object);
		}

		@Override
		public Class<T> getType()
		{
			return type;
		}

		@Override
		public <T> Attribute<T> bindVia(TypeConverter converter, Class<T> output)
		{
			throw new UnsupportedOperationException("Already bound");
		}
	}
}

package se.l4.dust.jaxrs.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.api.conversion.TypeConverter;

import com.google.common.primitives.Primitives;
import com.google.inject.Inject;

/**
 * A {@link ParamConverterProvider} that ties into {@link TypeConverter}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ConversionParamProvider
	implements ParamConverterProvider
{
	private final TypeConverter converter;

	@Inject
	public ConversionParamProvider(TypeConverter converter)
	{
		this.converter = converter;
	}

	@Override
	public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations)
	{
		if(rawType.isPrimitive()
			|| Primitives.isWrapperType(rawType) ||
			rawType == String.class ||
			! converter.canConvertBetween(rawType, String.class) ||
			! converter.canConvertBetween(String.class, rawType))
		{
			// Primitives and String are not handled, nor are types that we can't convert both ways
			return null;
		}
		
		NonGenericConversion<T, String> toString = converter.getConversion(rawType, String.class);
		NonGenericConversion<String, T> fromString = converter.getConversion(String.class, rawType);
		return new ParamConverterImpl<T>(toString, fromString);
	}

	private static class ParamConverterImpl<T>
		implements ParamConverter<T>
	{
		private final NonGenericConversion<T, String> toString;
		private final NonGenericConversion<String, T> fromString;

		public ParamConverterImpl(NonGenericConversion<T, String> toString, NonGenericConversion<String, T> fromString)
		{
			this.toString = toString;
			this.fromString = fromString;
		}
		
		@Override
		public T fromString(String value)
		{
			return fromString.convert(value);
		}
		
		@Override
		public String toString(T value)
		{
			return toString.convert(value);
		}
	}
}

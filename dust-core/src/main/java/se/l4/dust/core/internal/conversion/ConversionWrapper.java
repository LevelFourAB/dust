package se.l4.dust.core.internal.conversion;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import se.l4.dust.api.conversion.Conversion;
import se.l4.dust.api.conversion.ConversionException;
import se.l4.dust.api.conversion.NonGenericConversion;

/**
 * Wrapper for {@link Conversion}s that turn them into non-generic dependent
 * variants for internal use.
 * 
 * @author Andreas Holstenson
 *
 * @param <I>
 * @param <O>
 */
public class ConversionWrapper<I, O>
	implements NonGenericConversion<I, O>
{
	private final Conversion<I, O> conversion;
	private final Class<I> input;
	private final Class<O> output;

	public ConversionWrapper(Conversion<I, O> conversion)
	{
		this.conversion = conversion;
		
		Type input = null;
		Type output = null;
		
		Class<? extends Conversion> c = conversion.getClass();
		Type[] genericInterfaces = c.getGenericInterfaces();
		for(Type t : genericInterfaces)
		{
			if(t instanceof ParameterizedType)
			{
				ParameterizedType pt = (ParameterizedType) t;
				if(pt.getRawType() == Conversion.class)
				{
					// This is the conversion
					Type[] arguments = pt.getActualTypeArguments();
					input = arguments[0];
					output = arguments[1];
				}
			}
		}
		
		if(input == null)
		{
			throw new ConversionException("Unable to determine types for " 
				+ conversion + " consider using " 
				+ NonGenericConversion.class.getSimpleName()
			);
		}
		
		this.input = (Class<I>) findClass(conversion, input);
		this.output = (Class<O>) findClass(conversion, output);
	}
	
	private Class<?> findClass(Conversion<I, O> conversion, Type type)
	{
		if(type instanceof Class)
		{
			return (Class) type;
		}
		else if(type instanceof ParameterizedType)
		{
			return (Class) ((ParameterizedType) type).getRawType();
		}
		else if(type instanceof WildcardType)
		{
			WildcardType wc = (WildcardType) type;
			Type[] lowerBounds = wc.getLowerBounds();
			if(lowerBounds.length == 0)
			{
				throw new ConversionException("Could not determine type for " + conversion + " (on " + type + ")");
			}
			
			return findClass(conversion, lowerBounds[0]);
		}
		
		throw new ConversionException("Could not determine type for " + conversion + " (on " + type + ")");
	}
	
	public O convert(I in)
	{
		return conversion.convert(in);
	}
	
	public Class<I> getInput()
	{
		return input;
	}
	
	public Class<O> getOutput()
	{
		return output;
	}
}

package se.l4.dust.core.internal;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Some small utilities for dealing with generic types.
 * 
 * @author Andreas Holstenson
 *
 */
public class Generics
{
	private static final Type[] EMTPY_TYPE = new Type[0];

	public Generics()
	{
	}
	
	public static Type[] findParameterTypes(Type type)
	{
		if(type instanceof ParameterizedType)
		{
			ParameterizedType pt = (ParameterizedType) type;
			return pt.getActualTypeArguments();
		}
		else
		{
			return EMTPY_TYPE;
		}
	}
	
	/**
	 * Find the class of the given type.
	 * 
	 * @param root
	 * @param type
	 * @return
	 */
	public static Class<?> findClass(Object root, Type type)
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
				return Object.class;
//				throw new ConversionException("Could not determine type for " + root + " (on " + type + ")");
			}
			
			return findClass(root, lowerBounds[0]);
		}
		else if(type instanceof GenericArrayType)
		{
			Class c = findClass(root, ((GenericArrayType) type).getGenericComponentType());
			return Array.newInstance(c, 0).getClass();
		}
		
		return Object.class;
//		throw new ConversionException("Could not determine type for " + root + " (on " + type + ")");
	}
}

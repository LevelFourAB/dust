package se.l4.dust.core.internal.conversion;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

import se.l4.dust.api.conversion.Conversion;
import se.l4.dust.api.conversion.ConversionException;
import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.api.conversion.TypeConverter;

/**
 * Implementation of {@link TypeConverter}, supports chaining of conversions
 * to reach the desired output type.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultTypeConverter
	implements TypeConverter
{
	private Map<Class<?>, List<Conversion<?, ?>>> conversions;
	private Map<CacheKey, Conversion<?, ?>> cache;
	
	private static Map<Class<?>, Class<?>> primitives;
	
	static
	{
		primitives = new HashMap<Class<?>, Class<?>>();
		primitives.put(boolean.class, Boolean.class);
		primitives.put(byte.class, Byte.class);
		primitives.put(short.class, Short.class);
		primitives.put(int.class, Integer.class);
		primitives.put(long.class, Long.class);
		primitives.put(float.class, Float.class);
		primitives.put(double.class, Double.class);
		primitives.put(void.class, Void.class);
	}
	
	public DefaultTypeConverter()
	{
		conversions = new MapMaker()
			.makeComputingMap(new Function<Class<?>, List<Conversion<?, ?>>>()
			{
				public List<Conversion<?, ?>> apply(Class<?> from)
				{
					return new CopyOnWriteArrayList<Conversion<?,?>>();
				}
			});
		
		cache = new ConcurrentHashMap<CacheKey, Conversion<?,?>>();
	}
	
	private List<Conversion<?, ?>> getListFor(Class<?> c)
	{
		synchronized(conversions)
		{
			List<Conversion<?, ?>> list = conversions.get(c);
			if(list == null)
			{
				list = new LinkedList<Conversion<?,?>>();
				conversions.put(c, list);
			}
			
			return list;
		}
	}
	
	public void add(Conversion<?, ?> conversion)
	{
		NonGenericConversion<?, ?> nonGeneric = toNonGeneric(conversion);
		List<Conversion<?, ?>> list = conversions.get(nonGeneric.getInput());
		list.add(conversion);
	}
	
	private <I, O> NonGenericConversion<I, O> toNonGeneric(Conversion<I, O> conversion)
	{
		if(conversion instanceof NonGenericConversion)
		{
			return (NonGenericConversion<I, O>) conversion;
		}
		
		return new ConversionWrapper<I, O>(conversion);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T convert(Object in, Class<T> output)
	{
		Class<?> type;
		if(in == null)
		{
			if(output.isPrimitive())
			{
				type = void.class;
			}
			else
			{
				return null;
			}
		}
		else
		{
			type = in.getClass();
		}
		
		// Check if it is assignable
		if(output.isAssignableFrom(type))
		{
			return (T) in;
		}
		
		// Check cache first
		CacheKey key = new CacheKey(type, output);
		Conversion tc = cache.get(key);
		
		if(tc == null)
		{
			// If not cached find suitable conversion
			tc = findConversion(type, output);

			if(tc == null)
			{
				// No suitable conversion found
				throw new ConversionException("Unable to find suitable conversion between "
					+ type + " and " + output);
			}
			
			cache.put(key, tc);
		}
		
		return (T) tc.convert(in);
	}
	
	public boolean canConvertBetween(Class<?> in, Class<?> out)
	{
		return findConversion(in, out) != null;
	}

	/**
	 * Find the conversion to use for converting {@code in} to {@code out}.
	 * 
	 * @param <I>
	 * 		in type
	 * @param <O>
	 * 		out type
	 * @param in
	 * 		input class
	 * @param out
	 * 		output class
	 * @return
	 * 		conversion to use, {@code null} if none was found
	 */
	@SuppressWarnings("unchecked")
	private <I, O> Conversion<I, O> findConversion(Class<I> in, Class<O> out)
	{
		in = (Class) wrap(in);
		out = (Class) wrap(out);
		
		Set<Conversion<I, O>> tested = new HashSet<Conversion<I, O>>();
		PriorityQueue<NonGenericConversion<I, O>> queue = new PriorityQueue<NonGenericConversion<I, O>>(
			10,
			new Comparator<NonGenericConversion<I, O>>()
			{
				public int compare(NonGenericConversion<I,O> o1, NonGenericConversion<I,O> o2)
				{
					int c1 = count(o1);
					int c2 = count(o2);
					
					return (c1<c2 ? -1 : (c1==c2 ? 0 : 1));
				}
				
				private int count(NonGenericConversion n)
				{
					if(n instanceof CompoundTypeConversion)
					{
						CompoundTypeConversion tc = (CompoundTypeConversion) n;
						return count(tc.getIn()) + count(tc.getOut());
					}
					else
					{
						return 1;
					}
				}
			}
		);
		
		// Add initial conversions that should be checked
		for(Class<?> c : getInheritance(in))
		{
			List<Conversion<?, ?>> list = getListFor(c);
			
			queue.addAll((Collection) list);
			tested.addAll((Collection) list);
		}
		
		while(false == queue.isEmpty())
		{
			NonGenericConversion tc = queue.poll();
		
			// check if this is ok
			if(out.isAssignableFrom(tc.getOutput()))
			{
				return tc;
			}
			
			// otherwise continue
			for(Class<?> c : getInheritance(tc.getOutput()))
			{
				List<Conversion<?, ?>> list = getListFor(c);
				
				for(Conversion<?, ?> possible : list)
				{
					if(tested.contains(possible))
					{
						continue;
					}
					
					CompoundTypeConversion ctc = new CompoundTypeConversion(
						tc, (NonGenericConversion<Object, Object>) possible
					);
					
					queue.add((NonGenericConversion<I, O>) ctc);
					tested.add((NonGenericConversion<I, O>) possible);
				};
			}
		}
		
		return null;
	}
	
	private static Set<Class<?>> getInheritance(Class<?> in)
	{
		LinkedHashSet<Class<?>> result = new LinkedHashSet<Class<?>>();
		
		result.add(in);
		getInheritance(in, result);
		
		return result;
	}
	
	/**
	 * Get inheritance of type.
	 * 
	 * @param in
	 * @param result
	 */
	private static void getInheritance(Class<?> in, Set<Class<?>> result)
	{
		Class<?> superclass = getSuperclass(in);
		
		if(superclass != null)
		{
			result.add(superclass);
			getInheritance(superclass, result);
		}
		
		getInterfaceInheritance(in, result);
	}
	
	/**
	 * Get interfaces that the type inherits from.
	 * 
	 * @param in
	 * @param result
	 */
	private static void getInterfaceInheritance(Class<?> in, Set<Class<?>> result)
	{
		for(Class<?> c : in.getInterfaces())
		{
			result.add(c);
			
			getInterfaceInheritance(c, result);
		}
	}
	
	/**
	 * Get superclass of class.
	 * 
	 * @param in
	 * @return
	 */
	private static Class<?> getSuperclass(Class<?> in)
	{
		if(in == null)
		{
			return null;
		}
		
		if(in.isArray() && in != Object[].class)
		{
			Class<?> type = in.getComponentType();
			
			while(type.isArray())
			{
				type = type.getComponentType();
			}
			
			return type;
		}
		
		return in.getSuperclass();
	}
	
	/**
	 * Wrap the given primitive in its object equivalent.
	 * 
	 * @param in
	 * @return
	 */
	private static Class<?> wrap(Class<?> in)
	{
		if(false == in.isPrimitive())
		{
			return in;
		}
		
		Class<?> c = primitives.get(in);
		if(c != null)
		{
			return c;
		}
		else
		{
			throw new ConversionException("Unsupported type " + in);
		}
	}
	
	private static class CacheKey
	{
		private Class<?> in;
		private Class<?> out;
		
		public CacheKey(Class<?> in, Class<?> out)
		{
			this.in = in;
			this.out = out;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if(obj instanceof CacheKey)
			{
				CacheKey key = (CacheKey) obj;
				return in.equals(key.in) && out.equals(key.out);
			}
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			int c = 37;
			int t = 17; 
			
			t = t * c + in.hashCode();
			t = t * c + out.hashCode();
			
			return t;
		}
	}
}
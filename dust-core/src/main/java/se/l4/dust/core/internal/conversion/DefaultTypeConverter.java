package se.l4.dust.core.internal.conversion;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import se.l4.dust.api.conversion.Conversion;
import se.l4.dust.api.conversion.ConversionException;
import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.api.conversion.TypeConverter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.primitives.Primitives;

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
	private static final Conversion<?, ?> NULL = new Conversion<Object, Object>()
	{
		@Override
		public Object convert(Object in)
		{
			return null;
		}
	};
	
	private static final Conversion<?, ?> NO_CONVERSION = new Conversion<Object, Object>()
		{
			@Override
			public Object convert(Object in)
			{
				return in;
			}
		};
		
	private static final Conversion<?, ?> STRING_CONVERSION = new Conversion<Object, Object>()
		{
			@Override
			public Object convert(Object in)
			{
				return String.valueOf(in);
			}
		};
	
	private final LoadingCache<Class<?>, List<Conversion<?, ?>>> conversions;
	private final Map<CacheKey, Conversion<?, ?>> cache;
	
	public DefaultTypeConverter()
	{
		conversions = CacheBuilder.newBuilder()
			.build(new CacheLoader<Class<?>, List<Conversion<?, ?>>>()
			{
				@Override
				public List<Conversion<?, ?>> load(Class<?> key)
					throws Exception
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
			List<Conversion<?, ?>> list = conversions.getUnchecked(c);
			if(list == null)
			{
				list = new LinkedList<Conversion<?,?>>();
				conversions.put(c, list);
			}
			
			return list;
		}
	}
	
	@Override
	public void add(Conversion<?, ?> conversion)
	{
		NonGenericConversion<?, ?> nonGeneric = toNonGeneric(conversion);
		List<Conversion<?, ?>> list = conversions.getUnchecked(nonGeneric.getInput());
		list.add(nonGeneric);
	}
	
	private <I, O> NonGenericConversion<I, O> toNonGeneric(Conversion<I, O> conversion)
	{
		if(conversion instanceof NonGenericConversion)
		{
			return (NonGenericConversion<I, O>) conversion;
		}
		
		return new ConversionWrapper<I, O>(conversion);
	}
	
	private Class<?> getType(Class<?> in, Class<?> output)
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
			type = in;
		}
		
		return type;
	}
	
	private Conversion<?, ?> getConversion0(Class<?> in, Class<?> output)
	{
		if(in == null) return NULL;
		
		if(in == output || output.isAssignableFrom(in))
		{
			// If the same type or output is assignable from input
			return NO_CONVERSION;
		}
		
		// Check cache first
		CacheKey key = new CacheKey(in, output);
		Conversion tc = cache.get(key);
		
		if(tc == null)
		{
			// If not cached find suitable conversion
			tc = findConversion(in, output);
			if(tc == null)
			{
				if(output == String.class)
				{
					tc = STRING_CONVERSION;
				}
				else
				{
					tc = NULL;
				}
			}
			
			cache.put(key, tc);
		}
		
		return tc == NULL ? null : tc;
	}
	
	@Override
	public <I, O> NonGenericConversion<I, O> getConversion(Class<I> in, Class<O> out)
	{
		Class<?> type = getType(in, out);
		Conversion<?, ?> c = getConversion0(type, out);
		if(c == null)
		{
			throw new ConversionException("Can not convert between " + in + " and " + out);
		}
		
		return (NonGenericConversion<I, O>) toNonGeneric(c);
	}
	
	@Override
	public <I, O> NonGenericConversion<Object, O> getDynamicConversion(Class<I> in, Class<O> out)
	{
		if(canConvertBetween(in, out))
		{
			return (NonGenericConversion<Object, O>) getConversion(in, out);
		}
		
		if(canBeDynamic(in))
		{
			return createDynamicConversionTo(out);
		}
		
		throw new ConversionException("Unable to find a conversion between " + in + " and " + out);
	}
	
	private boolean canBeDynamic(Class<?> in)
	{
		return in == Object.class;
	}
	
	@Override
	public <T> NonGenericConversion<Object, T> createDynamicConversionTo(final Class<T> out)
	{
		return new NonGenericConversion<Object, T>()
		{
			@Override
			public Class<Object> getInput()
			{
				return Object.class;
			}
			
			@Override
			public Class<T> getOutput()
			{
				return out;
			}
			
			@Override
			public T convert(Object in)
			{
				return DefaultTypeConverter.this.convert(in, out);
			}
		};
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T convert(Object in, Class<T> output)
	{
		Class<?> type = getType(in == null ? null : in.getClass(), output);
		
		// Check if it is assignable
		if(type == null)
		{
			// Non-primitive output and no input, return null
			return null;
		}
		else if(output.isAssignableFrom(type))
		{
			return (T) in;
		}
		
		Conversion tc = getConversion0(type, output);
		if(tc == null)
		{
			throw new ConversionException("Unable to find suitable conversion between " + type + " and " + output);
		}
		
		return (T) tc.convert(in);
	}
	
	@Override
	public boolean canConvertBetween(Class<?> in, Class<?> out)
	{
		Class<?> type = getType(in, out);
		return getConversion0(type, out) != null;
	}
	
	@Override
	public boolean canConvertBetween(Object in, Class<?> out)
	{
		return canConvertBetween(in == null ? null : in.getClass(), out);
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
		in = Primitives.wrap(in);
		out = Primitives.wrap(out);
		
		Set<Conversion<I, O>> tested = new HashSet<Conversion<I, O>>();
		PriorityQueue<NonGenericConversion<I, O>> queue = new PriorityQueue<NonGenericConversion<I, O>>(
			10,
			new Comparator<NonGenericConversion<I, O>>()
			{
				@Override
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
	
	@SuppressWarnings("unchecked")
	@Override
	public <I, O> Conversion<I, O> nullConversion()
	{
		return (Conversion<I, O>) NULL;
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
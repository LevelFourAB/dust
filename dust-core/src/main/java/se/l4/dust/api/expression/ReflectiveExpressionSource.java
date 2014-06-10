package se.l4.dust.api.expression;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import se.l4.dust.api.Context;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Stage;

/**
 * Expression source that works by using reflection to find methods on its
 * subclasses. Every method annotated with either {@link Property}
 * or {@link Method} will be exposed in the source.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class ReflectiveExpressionSource
	implements ExpressionSource
{
	/**
	 * Annotation that defines that a method should be exposed as a property.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface Property
	{
		/**
		 * Name of the property.
		 * 
		 * @return
		 */
		String value() default "";
	}
	
	/**
	 * Annotation that defines that a method should be exposed as a method.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface Method
	{
		/**
		 * Name of the method.
		 * 
		 * @return
		 */
		String value() default "";
	}
	
	/**
	 * Annotation to define that a parameter should be an instance.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface Instance
	{
		
	}
	
	/**
	 * Annotation to define that a parameter should be an external value
	 * such as the current context.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface Bind
	{
		
	}
	
	private final Map<String, PropertyImpl> properties;
	private final Multimap<String, MethodImpl> methods;
	
	/**
	 * Creata a new expression source that will scan and expose any public
	 * methods annotated with {@link Method} or {@link Property}.
	 * 
	 * @param stage
	 * 		the stage that Guice is in, should be injected into the subclass
	 */
	public ReflectiveExpressionSource(Stage stage)
	{
		properties = createProperties();
		methods = createMethods();
	}

	/**
	 * Create all the exported properties.
	 * 
	 * @return
	 */
	private Map<String, PropertyImpl> createProperties()
	{
		ImmutableMap.Builder<String, PropertyImpl> results = ImmutableMap.builder();
		
		for(java.lang.reflect.Method method : getClass().getMethods())
		{
			if(! method.isAnnotationPresent(Property.class)) continue;
			
			Class<?>[] parameterTypes = method.getParameterTypes();
			Annotation[][] annotations = method.getParameterAnnotations();
			Provider[] providers = new Provider[annotations.length];
			
			Class<?> instance = null;
			for(int i=0, n=parameterTypes.length; i<n; i++)
			{
				if(hasAnnotation(annotations[i], Instance.class))
				{
					if(instance != null)
					{
						throw new IllegalArgumentException("Method " + method + " already has an instance annotation");
					}
					
					instance = parameterTypes[i];
					providers[i] = INSTANCE;
				}
				else if(hasAnnotation(annotations[i], Bind.class))
				{
					providers[i] = createFor(parameterTypes[i]);
					if(providers[i] == null)
					{
						throw new IllegalArgumentException("Method " + method + " has an unsupported bound parameter");
					}
				}
				else
				{
					throw new IllegalArgumentException("Method " + method + " has an unsupported parameter");
				}
			}
			
			Property prop = method.getAnnotation(Property.class);
			String name = prop.value().equals("") ? method.getName() : prop.value();
			results.put(name, new PropertyImpl(method, instance, providers));
		}
		
		return results.build();
	}

	/**
	 * Create all the exported method.
	 * 
	 * @return
	 */
	private Multimap<String, MethodImpl> createMethods()
	{
		ImmutableMultimap.Builder<String, MethodImpl> results = ImmutableMultimap.builder();
		for(java.lang.reflect.Method method : getClass().getMethods())
		{
			if(! method.isAnnotationPresent(Method.class)) continue;
			
			Class<?>[] parameterTypes = method.getParameterTypes();
			Annotation[][] annotations = method.getParameterAnnotations();
			Provider[] providers = new Provider[annotations.length];
			List<Class<?>> providerTypes = new ArrayList<Class<?>>();
			
			Class<?> instance = null;
			for(int i=0, n=parameterTypes.length; i<n; i++)
			{
				if(hasAnnotation(annotations[i], Instance.class))
				{
					if(instance != null)
					{
						throw new IllegalArgumentException("Method " + method + " already has an instance annotation");
					}
					
					instance = parameterTypes[i];
					providers[i] = INSTANCE;
				}
				else if(hasAnnotation(annotations[i], Bind.class))
				{
					providers[i] = createFor(parameterTypes[i]);
					if(providers[i] == null)
					{
						throw new IllegalArgumentException("Method " + method + " has an unsupported bound parameter");
					}
				}
				else
				{
					final int param = providerTypes.size();
					providers[i] = new Provider()
					{
						@Override
						public Object get(Context context, Object root, Object[] arguments)
						{
							return arguments[param];
						}
					};
					providerTypes.add(parameterTypes[i]);
				}
			}
			
			Method prop = method.getAnnotation(Method.class);
			String name = prop.value().equals("") ? method.getName() : prop.value();
			results.put(name, new MethodImpl(
				method, 
				instance, 
				providers, 
				providerTypes.toArray(new Class[providerTypes.size()])
			));
		}
		
		return results.build();
	}
	
	private static boolean hasAnnotation(Annotation[] annotations, Class<?> type)
	{
		for(Annotation a : annotations)
		{
			if(type.isAssignableFrom(a.annotationType()))
			{
				return true;
			}
		}
		
		return false;
	}
	

	private Provider createFor(Class<?> type)
	{
		if(type == Context.class)
		{
			return CONTEXT;
		}
		
		return null;
	}
	
	@Override
	public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
	{
		PropertyImpl property = properties.get(name);
		if(property == null) return null;
		
		if(property.instance == null)
		{
			return encounter.isRoot() ? property : null;
		}
		else if(property.instance.isAssignableFrom(encounter.getContext()))
		{
			return property;
		}
		else
		{
			throw encounter.error("Property can only be used on values of type " + property.instance.getName());
		}
	}
	
	@Override
	public DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters)
	{
		Collection<MethodImpl> alts = methods.get(name);
		if(alts.isEmpty())
		{
			return null;
		}
		
		_outer:
		for(MethodImpl m : alts)
		{
			if(parameters.length != m.paramTypes.length)
			{
				continue;
			}
			
			for(int i=0, n=parameters.length; i<n; i++)
			{
				if(! m.paramTypes[i].isAssignableFrom(parameters[i]))
				{
					continue _outer;
				}
			}
			
			if(m.instance == null)
			{
				if(! encounter.isRoot()) continue _outer;
			}
			else if(! m.instance.isAssignableFrom(encounter.getContext()))
			{
				continue _outer;
			}
			
			return m;
		}
		
		return null;
	}
	
	private interface Provider
	{
		Object get(Context context, Object root, Object[] arguments);
	}
	
	/** Provider for the current instance. */
	private static final Provider INSTANCE =
		new Provider()
		{
			@Override
			public Object get(Context context, Object root, Object[] arguments)
			{
				return root;
			}
		};
		
	/** Provider for the current context. */
	private static final Provider CONTEXT =
		new Provider()
		{
			@Override
			public Object get(Context context, Object root, Object[] arguments)
			{
				return context;
			}
		};
	
	private class PropertyImpl
		extends AbstractDynamicProperty
	{
		private final java.lang.reflect.Method method;
		private final Provider[] params;
		private final Class<?> instance;

		public PropertyImpl(
				java.lang.reflect.Method method, 
				Class<?> instance, 
				Provider[] params)
		{
			this.method = method;
			this.instance = instance;
			this.params = params;
		}

		@Override
		public Object get(Context context, Object root)
		{
			Object[] arguments = new Object[params.length];
			for(int i=0, n=arguments.length; i<n; i++)
			{
				arguments[i] = params[i].get(context, root, null);
			}
			
			try
			{
				return method.invoke(ReflectiveExpressionSource.this, arguments);
			}
			catch(InvocationTargetException e)
			{
				Throwables.propagateIfPossible(e.getCause());
				throw Throwables.propagate(e.getCause());
			}
			catch(Exception e)
			{
				Throwables.propagateIfPossible(e);
				throw Throwables.propagate(e);
			}
		}
		
		@Override
		public boolean supportsGet()
		{
			return true;
		}
		
		@Override
		public void set(Context context, Object root, Object value)
		{
			throw new ExpressionException("set is unsupported for this property");
		}
		
		@Override
		public boolean supportsSet()
		{
			return false;
		}
		
		@Override
		public Class<?> getType()
		{
			return method.getReturnType();
		}
	}
	
	private class MethodImpl
		implements DynamicMethod
	{
		private final java.lang.reflect.Method method;
		private final Provider[] params;
		private final Class<?> instance;
		private final Class[] paramTypes;
	
		public MethodImpl(
				java.lang.reflect.Method method, 
				Class<?> instance, 
				Provider[] params,
				Class[] paramTypes)
		{
			this.method = method;
			this.instance = instance;
			this.params = params;
			this.paramTypes = paramTypes;
		}
		
		@Override
		public Object invoke(Context context, Object instance,
				Object... parameters)
		{
			Object[] arguments = new Object[params.length];
			for(int i=0, n=arguments.length; i<n; i++)
			{
				arguments[i] = params[i].get(context, instance, parameters);
			}
			
			try
			{
				return method.invoke(ReflectiveExpressionSource.this, arguments);
			}
			catch(InvocationTargetException e)
			{
				Throwables.propagateIfPossible(e.getCause());
				throw Throwables.propagate(e.getCause());
			}
			catch(Exception e)
			{
				Throwables.propagateIfPossible(e);
				throw Throwables.propagate(e);
			}
		}
		
		@Override
		public Class<?> getType()
		{
			return method.getReturnType();
		}
		
		@Override
		public Class<?>[] getParametersType()
		{
			return paramTypes;
		}
		
		@Override
		public boolean needsContext()
		{
			return true;
		}
	}
}

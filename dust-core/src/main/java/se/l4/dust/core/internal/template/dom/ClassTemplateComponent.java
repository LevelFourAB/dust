package se.l4.dust.core.internal.template.dom;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdom.JDOMException;
import org.jdom.Namespace;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import se.l4.dust.api.TemplateException;
import se.l4.dust.api.annotation.PrepareRender;
import se.l4.dust.api.annotation.TemplateParam;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.dom.Document;
import se.l4.dust.dom.Element;

/**
 * Component based on a class. 
 * 
 * @author Andreas Holstenson
 *
 */
public class ClassTemplateComponent
	extends TemplateComponent
{
	private final TypeConverter converter;
	private final Class<?> type;
	private final Injector injector;
	private final TemplateCache cache;
	private final MethodInvocation[] methods;
	
	public ClassTemplateComponent(
			Namespace ns,
			String name,
			Injector injector, 
			TemplateCache cache,
			TypeConverter converter,
			Class<?> type)
	{
		super(name, ns);
		this.converter = converter;
		
		this.cache = cache;
		this.type = type;
		this.injector = injector;
		
		methods = createMethodInvocations(type);
	}
	
	private MethodInvocation[] createMethodInvocations(Class<?> type)
	{
		List<MethodInvocation> invocations = new ArrayList<MethodInvocation>();
		while(type != Object.class && type != null)
		{
			for(Method m : type.getDeclaredMethods())
			{
				if(m.isAnnotationPresent(PrepareRender.class))
				{
					invocations.add(new MethodInvocation(m));
				}
			}
			
			type = type.getSuperclass();
		}
		
		return invocations.toArray(new MethodInvocation[invocations.size()]);
	}
	
	/**
	 * Process this component and append the result to the given parent
	 * element.
	 * 
	 * @param parent
	 * 		target parent of the component
	 * @param data
	 * 		data root, for e.g. expressions
	 * @throws JDOMException 
	 */
	@Override
	public void process(
			TemplateEmitter emitter, 
			RenderingContext ctx,
			Element parent, 
			Object data,
			TemplateComponent lastComponent,
			Object previousRoot)
		throws JDOMException
	{
		Object o = injector.getInstance(type);
		
		Object root;
		
		if(methods.length == 0)
		{
			// No processing needed
			root = o;
		}
		else
		{
			// Find the best method to invoke
			MethodInvocation bestMethod = null;
			if(methods.length == 1)
			{
				bestMethod = methods[0];
			}
			else
			{
				int bestScore = 0;
				for(MethodInvocation i : methods)
				{
					int score = i.score();
					if(score > bestScore)
					{
						bestMethod = i;
						bestScore = score;
					}
				}
			}
			
			// Actually invoke the method
			root = bestMethod.invoke(ctx, data, o);
			if(root == null)
			{
				root = o;
			}
		}

		if(root instanceof Element)
		{
			emitter.process(ctx, root, parent, (Element) root, this, data);
		}
		else
		{
			try
			{
				// Process the template of the component 
				Document template = cache.getTemplate(type, null);
				Element templateRoot = template.getRootElement();
				
				emitter.process(ctx, root, parent, templateRoot, this, data);
				
				// Set DocType
				if(parent instanceof FakeElement)
				{
					((FakeElement) parent).setDocType(template.getDocType());
				}
			}
			catch(IOException e)
			{
				throw new JDOMException(e.getMessage(), e);
			}
		}
	}
	
	private class MethodInvocation
	{
		private final Method method;
		private final Argument[] arguments;
		
		public MethodInvocation(Method m)
		{
			this.method = m;
			
			Argument[] result = new Argument[m.getParameterTypes().length];
			for(int i=0, n=result.length; i<n; i++)
			{
				result[i] = new Argument(m, i);
			}
			
			arguments = result;
		}
		
		/**
		 * Score the invocation.
		 * 
		 * @return
		 */
		public int score()
		{
			int score = 0;
			for(Argument arg : arguments)
			{
				if(arg.canBeInjected())
				{
					score++;
				}
			}
			
			return score;
		}
		
		public Object invoke(RenderingContext ctx, Object root, Object self)
		{
			Object[] data = new Object[arguments.length];
			for(int i=0, n=arguments.length; i<n; i++)
			{
				Object value = arguments[i].getValue(ctx, root);
				value = converter.convert(value, arguments[i].typeClass);
				data[i] = value;
			}
			
			try
			{
				return method.invoke(self, data);
			}
			catch(InvocationTargetException e)
			{
				Throwable e2 = e.getCause();
				if(e2 instanceof RuntimeException)
				{
					throw (RuntimeException) e2;
				}
				
				throw new TemplateException("Unable to invoke method " + method 
					+ "; " + e2.getMessage() + "\nArguments were: " + Arrays.toString(data), e2);
			}
			catch(Exception e)
			{
				throw new TemplateException("Unable to invoke method " + method 
					+ "; " + e.getMessage() + "\nArguments were: " + Arrays.toString(data), e);
			}
		}
	}
	
	private class Argument
	{
		private final Method method;
		private final Type type;
		private final Annotation[] annotations;
		private final String attribute;
		private final Class<?> typeClass;
		private final Binding binding;
		
		public Argument(Method m, int index)
		{
			method = m;
			annotations = m.getParameterAnnotations()[index];
			type = m.getGenericParameterTypes()[index];
			typeClass = m.getParameterTypes()[index];
			
			attribute = findAttribute(m, annotations);
			binding = findBinding();
		}
		
		private Binding findBinding()
		{
			TypeLiteral literal = TypeLiteral.get(type);
			
			for(Binding b : (List<Binding>) injector.findBindingsByType(literal))
			{
				Key key = b.getKey();
				
				if(key.getAnnotation() != null)
				{
					for(Annotation a : annotations)
					{
						if(key.getAnnotation().equals(a))
						{
							return b;
						}
					}
				}
				
				if(key.getAnnotation() == null)
				{
					return b;
				}
			}
			
			return null;
		}
		
		private String findAttribute(Method m, Annotation[] annotations)
		{
			for(Annotation a : annotations)
			{
				if(a instanceof TemplateParam)
				{
					return ((TemplateParam) a).value();
				}
			}
			
			return null;
		}
		
		public boolean canBeInjected()
		{
			if(binding != null)
			{
				// Bindings can always be handled
				return true;
			}
			else if(attribute != null)
			{
				// If the attribute exists we can be injected
				return getAttribute(attribute) != null;
			}
			else
			{
				// Assume that we can be injected
				// FIXME: Actually check with the context
				return true;
			}
		}
		
		public Object getValue(RenderingContext ctx, Object root)
		{
			if(attribute != null)
			{
				TemplateAttribute attr = (TemplateAttribute) getAttribute(attribute);
				return attr.getValue(ctx, root);
			}
			else if(binding != null)
			{
				return binding.getProvider().get();
			}
			else
			{
				return ctx.resolveObject(method, type, annotations, root);
			}
		}
	}
	
}

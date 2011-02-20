package se.l4.dust.core.internal.template.dom;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.jdom.JDOMException;
import org.jdom.Namespace;

import se.l4.dust.api.TemplateException;
import se.l4.dust.api.annotation.PrepareRender;
import se.l4.dust.api.annotation.TemplateParam;
import se.l4.dust.api.template.TemplateContext;
import se.l4.dust.core.template.TemplateCache;
import se.l4.dust.dom.Document;
import se.l4.dust.dom.Element;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Component based on a class. 
 * 
 * @author Andreas Holstenson
 *
 */
public class ClassTemplateComponent
	extends TemplateComponent
{
	private final Class<?> type;
	private final Injector injector;
	private final TemplateCache cache;
	private final MethodInvocation method;
	
	public ClassTemplateComponent(
			Namespace ns,
			String name,
			Injector injector, 
			TemplateCache cache,
			Class<?> type)
	{
		super(name, ns);
		
		this.cache = cache;
		this.type = type;
		this.injector = injector;
		
		method = createMethodInvocation(type);
	}
	
	private MethodInvocation createMethodInvocation(Class<?> type)
	{
		while(type != Object.class && type != null)
		{
			for(Method m : type.getDeclaredMethods())
			{
				if(m.isAnnotationPresent(PrepareRender.class))
				{
					return new MethodInvocation(m);
				}
			}
			
			type = type.getSuperclass();
		}
		
		return null;
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
			TemplateContext ctx,
			Element parent, 
			Object data,
			TemplateComponent lastComponent,
			Object previousRoot)
		throws JDOMException
	{
		Object o = injector.getInstance(type);
		
		Object root;
		
		if(method == null)
		{
			// No processing needed
			root = o;
		}
		else
		{
			root = method.invoke(ctx, data, o);
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
		
		public Object invoke(TemplateContext ctx, Object root, Object self)
		{
			Object[] data = new Object[arguments.length];
			for(int i=0, n=arguments.length; i<n; i++)
			{
				data[i] = arguments[i].getValue(ctx, root);
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
		private final Type type;
		private final Annotation[] annotations;
		private final String attribute;
		
		public Argument(Method m, int index)
		{
			annotations = m.getParameterAnnotations()[index];
			type = m.getGenericParameterTypes()[index];
			
			attribute = findAttribute(m, annotations);
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
		
		public Object getValue(TemplateContext ctx, Object root)
		{
			if(attribute != null)
			{
				TemplateAttribute attr = (TemplateAttribute) getAttribute(attribute);
				return attr.getValue(ctx, root);
			}
			else
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
								return b.getProvider().get();
							}
						}
					}
					
					if(key.getAnnotation() == null)
					{
						return b.getProvider().get();
					}
				}
				
				return null; // XXX: Exception
			}
		}
	}
	
}

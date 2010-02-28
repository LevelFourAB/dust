package se.l4.dust.core.internal.template.dom;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.jboss.resteasy.core.MessageBodyParameterInjector;
import org.jboss.resteasy.core.MethodInjectorImpl;
import org.jboss.resteasy.core.ValueInjector;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import se.l4.dust.api.annotation.PrepareRender;
import se.l4.dust.api.annotation.TemplateParam;
import se.l4.dust.core.template.TemplateCache;
import se.l4.dust.dom.Document;
import se.l4.dust.dom.Element;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;


public class ClassTemplateComponent
	extends TemplateComponent
{
	private static final ThreadLocal<Object> DATA = new ThreadLocal<Object>();
	
	private final Class<?> type;
	private final Injector injector;
	private final ResteasyProviderFactory factory;
	private final MethodInjectorImpl methodInjector;
	private final TemplateCache cache;
	
	public ClassTemplateComponent(
			Namespace ns,
			String name,
			Injector injector, 
			ResteasyProviderFactory factory,
			TemplateCache cache,
			Class<?> type)
	{
		super(name, ns);
		
		this.factory = factory;
		this.cache = cache;
		this.type = type;
		this.injector = injector;
		
		this.methodInjector = createInjector();
	}
	
	private MethodInjectorImpl createInjector()
	{
		for(Method m : type.getMethods())
		{
			if(m.isAnnotationPresent(PrepareRender.class))
			{
				return new MethodInjectorExt(type, m, factory);
			}
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
			Element parent, 
			Object data,
			TemplateComponent lastComponent,
			Object previousRoot)
		throws JDOMException
	{
		Object o = injector.getInstance(type);
		
		HttpRequest request = injector.getInstance(HttpRequest.class);
		HttpResponse response = injector.getInstance(HttpResponse.class);
		
		Object root;
		if(methodInjector != null)
		{
			// Invoke the given component method
			DATA.set(data);
			
			root = methodInjector.invoke(request, response, o);
			if(root == null)
			{
				root = o;
			}
		}
		else
		{
			root = o;
		}

		if(root instanceof Element)
		{
			emitter.process(root, parent, (Element) root, this, data);
		}
		else
		{
			try
			{
				// Process the template of the component 
				Document template = cache.getTemplate(type, null);
				Element templateRoot = template.getRootElement();
				
				emitter.process(root, parent, templateRoot, this, data);
			}
			catch(IOException e)
			{
				throw new JDOMException(e.getMessage(), e);
			}
		}
	}
	
	private class MethodInjectorExt
		extends MethodInjectorImpl
	{
		public MethodInjectorExt(Class root, Method method,
				ResteasyProviderFactory factory)
		{
			super(root, method, factory);
			
			for(int i=0, n=params.length; i<n; i++)
			{
				ValueInjector vi = params[i];
				if(vi instanceof MessageBodyParameterInjector)
				{
					Type paramType = method.getGenericParameterTypes()[i];
					boolean found = false;
					Annotation[] annotations = method.getParameterAnnotations()[i];
					for(Annotation a : annotations)
					{
						if(a instanceof TemplateParam)
						{
							found = true;
							params[i] = new TemplateValueInjector(
								((TemplateParam) a).value()
							);
							
							break;
						}
						else
						{
							Key k = Key.get(paramType, a);
							
							Binding b = injector.getBinding(k);
							if(b != null)
							{
								found = true;
								params[i] = new BindingValueInjector(k);
							}
							
							break;
						}
					}
					
					if(false == found)
					{
						Key k = Key.get(paramType);
						Binding b = injector.getBinding(k);
						if(b != null)
						{
							params[i] = new BindingValueInjector(k);
						}
					}
				}
			}
		}
		
	}
	
	/** Custom injector for template parameters */
	private class TemplateValueInjector
		implements ValueInjector
	{
		private final String name;
		
		public TemplateValueInjector(String name)
		{
			this.name = name;
		}
		
		public Object inject()
		{
			return null;
		}

		public Object inject(HttpRequest request, HttpResponse response)
		{
			TemplateAttribute a = (TemplateAttribute) getAttribute(name);
			return a.getValue(DATA.get());
		}
	}
	
	private class BindingValueInjector
		implements ValueInjector
	{
		
		private final Key key;

		public BindingValueInjector(Key key)
		{
			this.key = key;
		}
		
		public Object inject()
		{
			return null;
		}

		public Object inject(HttpRequest request, HttpResponse response)
		{
			return injector.getInstance(key);
		}
		
	}
}

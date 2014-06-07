package se.l4.dust.core.internal.template.dom;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.AfterRender;
import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.PrepareRender;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.TemplateParam;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.dom.DocType;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.dom.WrappedElement;
import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;

public class ComponentTemplateFragment
	implements TemplateFragment
{
	private final Injector injector;
	private final TemplateCache cache;
	private final TypeConverter converter;
	
	private final Class<?> type;
	private final Provider<?> instance;
	
	private final MethodDef[] prepare;
	private final MethodDef[] afterRender;
	private final MethodDef[] setters;
	
	private final boolean dev;

	public ComponentTemplateFragment(Injector injector0, Class<?> type0)
	{
		this.injector = injector0;
		this.type = type0;
		
		Stage stage = injector.getInstance(Stage.class);
		dev = stage == Stage.DEVELOPMENT;
		
		cache = injector.getInstance(TemplateCache.class);
		converter = injector.getInstance(TypeConverter.class);
		
		if(dev)
		{
			instance = new Provider()
			{
				@Override
				public Object get()
				{
					return injector.getInstance(type);
				}
			};
			prepare = afterRender = setters = null;
		}
		else
		{
			instance = injector.getProvider(type);
			prepare = createMethodInvocations();
			afterRender = createAfterRenderInvocations();
			setters = createSetMethodInvocations();
		}
	}
	
	@Override
	public void build(FragmentEncounter encounter)
	{
		Methods methods = dev ? new DevMethods(encounter) : new ProductionMethods(encounter);
		encounter.replaceWith(new ComponentEmittable(methods, encounter.getScopedBody()));
	}

	private class ComponentEmittable
		implements Emittable
	{
		private final Emittable scopedBody;
		private final Methods methods;

		public ComponentEmittable(Methods methods, Emittable scopedBody)
		{
			this.methods = methods;
			this.scopedBody = scopedBody;
		}
		
		@Override
		public void emit(TemplateEmitter emitter, TemplateOutputStream out)
			throws IOException
		{
			Object o = instance.get();
			
			Object data = emitter.getObject();
			Object root;
			
			RenderingContext ctx = emitter.getContext();
			
			// Run all set methods
			for(MethodInvocation m : methods.getSetters())
			{
				if(m.valid())
				{
					m.invoke(ctx, data, o);
				}
			}
			
			// Run all methods for prepare render
			MethodInvocation[] prepareMethods = methods.getPrepare();
			if(prepareMethods.length == 0)
			{
				// No processing needed
				root = o;
			}
			else
			{
				// Find the best method to invoke
				MethodInvocation bestMethod = null;
				if(prepareMethods.length == 1)
				{
					bestMethod = prepareMethods[0];
				}
				else
				{
					int bestScore = 0;
					boolean hasExact = false;
					for(MethodInvocation i : prepareMethods)
					{
						int score = i.score();
						if(score > bestScore || (! hasExact && score == i.maxScore()))
						{
							bestMethod = i;
							bestScore = score;
							hasExact = score == i.maxScore();
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

			if(root instanceof Emittable)
			{
				emitter.emit((Emittable) root);
			}
			else
			{
				TemplateEmitterImpl emitterImpl = (TemplateEmitterImpl) emitter;
				
				// Process the template of the component 
				ParsedTemplate template = cache.getTemplate(ctx, root.getClass());
				
				// Switch to new context
				Object current = emitterImpl.getCurrentData();
				Integer old = emitterImpl.switchData(template.getRawId(), root);
				
				Emittable content = scopedBody;
				Element element = content instanceof Element
					? (Element) content
					: (content instanceof WrappedElement ? ((WrappedElement) content).getElement() : null); 
				Integer oldComponent = emitterImpl.switchComponent(template.getRawId(), element);
				
				DocType docType = template.getDocType();
				if(docType != null)
				{
					out.docType(docType.getName(), docType.getPublicId(), docType.getSystemId());
				}
				
				Element templateRoot = template.getRoot();
				
				emitterImpl.emit(templateRoot);
				
				// Switch context back
				emitterImpl.switchData(old, current);
				emitterImpl.switchComponent(oldComponent);
			}
			
			// Run all methods for after render
			for(MethodInvocation i : methods.getAfterRender())
			{
				i.invoke(ctx, data, o);
			}
		}
	}
	
	private MethodInvocation[] bind(MethodDef[] defs, FragmentEncounter encounter)
	{
		MethodInvocation[] result = new MethodInvocation[defs.length];
		for(int i=0, n=result.length; i<n; i++)
		{
			result[i] = defs[i].bind(encounter);
		}
		return result;
	}
	
	private interface Methods
	{
		MethodInvocation[] getPrepare();
		
		MethodInvocation[] getAfterRender();
		
		MethodInvocation[] getSetters();
	}
	
	private class ProductionMethods
		implements Methods
	{
		private final MethodInvocation[] boundPrepare;
		private final MethodInvocation[] boundSetters;
		private final MethodInvocation[] boundAfterRender;
		
		public ProductionMethods(FragmentEncounter encounter)
		{
			boundPrepare = bind(prepare, encounter);
			boundSetters = bind(setters, encounter);
			boundAfterRender = bind(afterRender, encounter);
		}
		
		@Override
		public MethodInvocation[] getAfterRender()
		{
			return boundAfterRender;
		}
		
		@Override
		public MethodInvocation[] getPrepare()
		{
			return boundPrepare;
		}
		
		@Override
		public MethodInvocation[] getSetters()
		{
			return boundSetters;
		}
	}
	
	private class DevMethods
		implements Methods
	{
		private final FragmentEncounter encounter;

		public DevMethods(FragmentEncounter encounter)
		{
			this.encounter = encounter;
		}
		
		@Override
		public MethodInvocation[] getPrepare()
		{
			return bind(createMethodInvocations(), encounter);
		}
		
		@Override
		public MethodInvocation[] getSetters()
		{
			return bind(createSetMethodInvocations(), encounter);
		}
		
		@Override
		public MethodInvocation[] getAfterRender()
		{
			return bind(createAfterRenderInvocations(), encounter);
		}
	}
	
	private MethodDef[] createMethodInvocations()
	{
		Class<?> type = this.type;
		List<MethodDef> invocations = new ArrayList<>();
		while(type != Object.class && type != null)
		{
			for(Method m : type.getDeclaredMethods())
			{
				if(m.isAnnotationPresent(PrepareRender.class))
				{
					invocations.add(new MethodDef(m));
				}
			}
			
			type = type.getSuperclass();
		}
		
		return invocations.toArray(new MethodDef[invocations.size()]);
	}
	
	private MethodDef[] createAfterRenderInvocations()
	{
		Class<?> type = this.type;
		List<MethodDef> invocations = new ArrayList<>();
		while(type != Object.class && type != null)
		{
			for(Method m : type.getDeclaredMethods())
			{
				if(m.isAnnotationPresent(AfterRender.class))
				{
					invocations.add(new MethodDef(m));
				}
			}
			
			type = type.getSuperclass();
		}
		
		return invocations.toArray(new MethodDef[invocations.size()]);
	}
	
	private MethodDef[] createSetMethodInvocations()
	{
		Class<?> type = this.type;
		List<MethodDef> invocations = new ArrayList<>();
		while(type != Object.class && type != null)
		{
			for(Method m : type.getDeclaredMethods())
			{
				if(m.isAnnotationPresent(PrepareRender.class))
				{
					// Skip rendering methods
					continue;
				}
				
				Annotation[][] annotations = m.getParameterAnnotations();
				_annotations:
				for(int i=0, n=annotations.length; i<n; i++)
				{
					for(Annotation a : annotations[i])
					{
						if(a instanceof TemplateParam)
						{
							invocations.add(new MethodDef(m));
							break _annotations;
						}
					}
				}
			}
			
			type = type.getSuperclass();
		}
		
		return invocations.toArray(new MethodDef[invocations.size()]);
	}
	
	private class MethodInvocation
	{
		private final Method method;
		private final Argument[] arguments;

		public MethodInvocation(Method method, Argument[] boundArguments)
		{
			this.method = method;
			this.arguments = boundArguments;
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
		
		public int maxScore()
		{
			return arguments.length;
		}
		
		public boolean valid()
		{
			if(arguments.length == 0) return true;
			
			for(Argument arg : arguments)
			{
				if(! arg.canBeInjected())
				{
					return false;
				}
			}
			
			return true;
		}
		
		public Object invoke(RenderingContext ctx, Object root, Object self)
		{
			Object[] data = null;
			try
			{
				data = new Object[arguments.length];
				for(int i=0, n=arguments.length; i<n; i++)
				{
					Object value = arguments[i].getValue(ctx, root);
					value = converter.convert(value, arguments[i].def.typeClass);
					data[i] = value;
				}
				
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
		private final ArgumentDef def;
		private final Attribute attribute;

		public Argument(ArgumentDef def, Attribute attribute)
		{
			this.def = def;
			this.attribute = attribute;
		}
		
		public boolean canBeInjected()
		{
			if(def.binding != null)
			{
				// Bindings can always be handled
				return true;
			}
			else if(def.attribute != null)
			{
				return attribute != null;
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
				return attribute.get(ctx, root);
			}
			else if(def.binding != null)
			{
				return def.binding.getProvider().get();
			}
			else
			{
				return ctx.resolveObject(def.method, type, def.annotations, root);
			}
		}
	}
	
	private class MethodDef
	{
		private final Method method;
		private final ArgumentDef[] arguments;
		
		public MethodDef(Method m)
		{
			this.method = m;
			
			ArgumentDef[] result = new ArgumentDef[m.getParameterTypes().length];
			for(int i=0, n=result.length; i<n; i++)
			{
				result[i] = new ArgumentDef(m, i);
			}
			
			arguments = result;
		}
		
		public MethodInvocation bind(FragmentEncounter encounter)
		{
			Argument[] boundArguments = new Argument[arguments.length];
			for(int i=0, n=boundArguments.length; i<n; i++)
			{
				boundArguments[i] = arguments[i].bind(encounter);
			}
			return new MethodInvocation(method, boundArguments);
		}
	}
	
	private class ArgumentDef
	{
		private final Method method;
		private final Type type;
		private final Annotation[] annotations;
		private final String attribute;
		private final Class<?> typeClass;
		private final Binding binding;
		
		public ArgumentDef(Method m, int index)
		{
			method = m;
			annotations = m.getParameterAnnotations()[index];
			type = m.getGenericParameterTypes()[index];
			typeClass = m.getParameterTypes()[index];
			
			attribute = findAttribute(m, annotations);
			binding = attribute == null ? findBinding() : null;
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
		
		private Argument bind(FragmentEncounter encounter)
		{
			Attribute attr = null;
			if(attribute != null)
			{
				attr = encounter.getAttribute(attribute);
			}
			
			return new Argument(this, attr);
		}
	}
}

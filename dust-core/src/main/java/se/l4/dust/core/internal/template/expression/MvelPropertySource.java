package se.l4.dust.core.internal.template.expression;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mvel2.CompileException;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import se.l4.dust.api.TemplateException;
import se.l4.dust.api.template.PropertySource;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.dom.Element;

/**
 * Property source for MVEL expressions.
 * 
 * @author Andreas Holstenson
 *
 */
public class MvelPropertySource
	implements PropertySource
{
	private final Map<String, Expressions> cache;
	
	public MvelPropertySource()
	{
		cache = new ConcurrentHashMap<String, Expressions>();
	}
	
	public DynamicContent getPropertyContent(Class<?> context, String propertyExpression, Element parent)
	{
		String key = context.getName() + ":" + propertyExpression;
		Expressions expressions = cache.get(key);
		if(expressions == null)
		{
			expressions = new Expressions(context, propertyExpression);
			cache.put(key, expressions);
		}
		
		return new Content(expressions);
	}
	
	private static class Content
		extends DynamicContent
	{
		private final Expressions expressions;

		public Content(Expressions expressions)
		{
			this.expressions = expressions;
		}
		
		@Override
		public Object getValue(RenderingContext ctx, Object root)
		{
			try
			{
				return MVEL.executeExpression(expressions.expression, root);
			}
			catch(CompileException e)
			{
				throw new TemplateException("Unable to execute expression ${" + expressions.rawExpression + "}: " + e.getMessage(), e);
			}
		}
		
		@Override
		public void setValue(RenderingContext ctx, Object root, Object value)
		{
			try
			{
				MVEL.executeSetExpression(expressions.setter, root, value);
			}
			catch(CompileException e)
			{
				throw new TemplateException("Unable to execute expression ${" + expressions.rawExpression + "}: " + e.getMessage(), e);
			}
		}
	}

	private static class Expressions
	{
		private final Serializable expression;
		private final Serializable setter;
		private final Object rawExpression;

		public Expressions(Class<?> context, String expression)
		{
			ParserContext ctx = new ParserContext();
			ctx.setStrongTyping(true);
			
			ctx.addImport("Escape", EscapeHelper.class);
			ctx.addImport("$", EscapeHelper.class);
			ctx.addInput("this", context);
			
			extractProperties(context, ctx);
		    
		    BeanInfo info = getBeanInfo(context);
		    for(PropertyDescriptor d : info.getPropertyDescriptors())
		    {
		    	String name = d.getName();
		    	Class<?> type = d.getPropertyType();
		    	TypeVariable<?>[] params = type.getTypeParameters();
		    	
//				Type propertyType = d.getReadMethod() == null
//					? d.getWriteMethod().getGenericParameterTypes()[0]
//					: d.getReadMethod().getGenericReturnType();
//					
//		    	if(params.length == 0)
		    	{
		    		ctx.addInput(name, type);
		    	}
//		    	else
//		    	{
//		    		ctx.addInput(name, type, params);
//		    	}
		    }
		    
			this.rawExpression = expression; 
			
			try
			{
				this.expression = MVEL.compileExpression(expression, ctx);
				this.setter = MVEL.compileSetExpression(expression, ctx);
			}
			catch(Throwable t)
			{
				throw new TemplateException("Unable to compile expression ${" + expression + "}; " + t.getMessage(), t);
			}
		}

		private void extractProperties(Class<?> context, ParserContext ctx)
		{
			Class<?> type = context;
			while(type != null && type != Object.class)
			{
				for(Field field : type.getDeclaredFields())
				{
					if(Modifier.isPublic(field.getModifiers()))
					{
						ctx.addInput(field.getName(), field.getType());
					}
				}
				
				type = type.getSuperclass();
			}
		}
		
		private BeanInfo getBeanInfo(Class<?> context)
		{
			try
			{
				return Introspector.getBeanInfo(context);
			}
			catch(IntrospectionException e)
			{
				throw new TemplateException("Unable to get properties of " + context);
			}
		}
		
		private Class getClass(Type type)
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
					throw new IllegalArgumentException();
				}
				
				return getClass(lowerBounds[0]);
			}
			
			throw new IllegalArgumentException("Unable to get class for " + type);
		}
	}
}

package se.l4.dust.core.internal.expression.invoke;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import se.l4.dust.api.Context;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.primitives.Primitives;

/**
 * Invoker for a property method.
 * 
 * @author Andreas Holstenson
 *
 */
public class MethodPropertyInvoker
	implements Invoker
{
	private final Node node;
	private final Method getter;
	private final Method setter;
	private final ResolvedType type;

	public MethodPropertyInvoker(Node node, ResolvedType type, Method getter, Method setter)
	{
		this.node = node;
		this.type = type;
		
		this.getter = getter;
		this.setter = setter;
	}
	
	@Override
	public Class<?> getReturnClass()
	{
		return type.getErasedType();
	}
	
	@Override
	public ResolvedType getReturnType()
	{
		return type;
	}
	
	@Override
	public Object get(ErrorHandler errors, Context context, Object root, Object instance)
	{
		if(instance == null)
		{
			throw errors.error(node, "Object is null, can't fetch a property on a null object");
		}
		
		try
		{
			return getter.invoke(instance);
		}
		catch(InvocationTargetException e)
		{
			throw errors.error(node, "Exception caught while executing getter: " + e.getMessage(), e);
		}
		catch(Exception e)
		{
			throw errors.error(node, e + "; Object is: " + instance);
		}
	}
	
	@Override
	public boolean supportsGet()
	{
		return getter != null;
	}
	
	@Override
	public void set(ErrorHandler errors, Context context, Object root,
			Object instance, Object value)
	{
		if(setter == null)
		{
			throw errors.error(node, "No setter available matching getter " + getter.getName());
		}
		
		if(instance == null)
		{
			throw errors.error(node, "Object is null, can't set a property on a null object");
		}
		
		try
		{
			setter.invoke(instance, value);
		}
		catch(InvocationTargetException e)
		{
			throw errors.error(node, "Exception caught while executing getter: " + e.getMessage(), e);
		}
		catch(Exception e)
		{
			throw errors.error(node, "Error executing: " + e.getMessage(), e);
		}
	}
	
	@Override
	public boolean supportsSet()
	{
		return setter != null;
	}
	
	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		StringBuilder builder = new StringBuilder();
		builder
			.append("(")
			.append(compiler.cast(getReturnClass()))
			.append(" ")
			.append(context)
			.append(".")
			.append(getter.getName())
			.append("())");
		
		return builder.toString();
	}
	
	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		if(setter == null) return null;
		
		Class<?> in = setter.getParameterTypes()[0];
		if(in.isPrimitive())
		{
			Class<?> type = Primitives.wrap(setter.getParameterTypes()[0]);
			return context + "." + setter.getName() + "(" + compiler.unwrap(type, "(" + compiler.cast(type) + " $3)") + ")";
		}
		else
		{
			return context + "." + setter.getName() + "( " + compiler.cast(in) + " $3)";
		}
	}
	
	@Override
	public Node getNode()
	{
		return node;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getter == null) ? 0 : getter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		MethodPropertyInvoker other = (MethodPropertyInvoker) obj;
		if(getter == null)
		{
			if(other.getter != null)
				return false;
		}
		else if(!getter.equals(other.getter))
			return false;
		return true;
	}
}

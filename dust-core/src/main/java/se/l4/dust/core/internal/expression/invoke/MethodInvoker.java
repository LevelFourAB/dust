package se.l4.dust.core.internal.expression.invoke;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import se.l4.dust.api.Context;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

/**
 * Invoker that runs methods.
 * 
 * @author Andreas Holstenson
 *
 */
public class MethodInvoker
	implements Invoker
{
	private final Node node;
	private final Method method;
	private final Invoker[] params;
	private final ResolvedType type;

	public MethodInvoker(Node node, ResolvedType type, Method method, Invoker[] params)
	{
		this.node = node;
		this.type = type;
		this.method = method;
		this.params = params;
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
			throw errors.error(node, "Object is null, can't invoke a method on a null object");
		}
		
		try
		{
			Object[] values = new Object[params.length];
			for(int i=0, n=params.length; i<n; i++)
			{
				values[i] = params[i].get(errors, context, root, root);
			}
			
			return method.invoke(instance, values);
		}
		catch(InvocationTargetException e)
		{
			throw errors.error(node, "Exception caught while executing: " + e.getMessage(), e);
		}
		catch(Exception e)
		{
			throw errors.error(node, e);
		}
	}
	
	@Override
	public void set(ErrorHandler errors, Context context, Object root,
			Object instance, Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
	}
	
	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		StringBuilder builder = new StringBuilder();
		builder
			.append("(")
			.append(compiler.cast(getReturnClass()))
			.append(" ")
			.append(context).append(".").append(method.getName())
			.append("(");
		
		for(int i=0, n=params.length; i<n; i++)
		{
			if(i > 0) builder.append(", ");
			
			builder.append(
				params[i].toJavaGetter(errors, compiler, compiler.getRootContext())
			);
		}
		
		builder.append("))");
		return builder.toString();
	}
	
	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		return null;
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
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + Arrays.hashCode(params);
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
		MethodInvoker other = (MethodInvoker) obj;
		if(method == null)
		{
			if(other.method != null)
				return false;
		}
		else if(!method.equals(other.method))
			return false;
		if(!Arrays.equals(params, other.params))
			return false;
		return true;
	}
}

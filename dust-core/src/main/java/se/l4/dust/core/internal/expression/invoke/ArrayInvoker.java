package se.l4.dust.core.internal.expression.invoke;

import java.lang.reflect.Array;
import java.util.Arrays;

import se.l4.dust.api.Context;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

/**
 * Invoker that will create an array.
 * 
 * @author Andreas Holstenson
 *
 */
public class ArrayInvoker
	implements Invoker
{
	private final Node node;
	private final Invoker[] values;
	private final Class<?> componentType;
	private final Class<? extends Object> arrayType;

	public ArrayInvoker(Node node, Class<?> componentType, Invoker[] values)
	{
		this.node = node;
		this.componentType = componentType;
		arrayType = Array.newInstance(componentType, 0).getClass();
		this.values = values;
	}
	
	@Override
	public Node getNode()
	{
		return node;
	}
	
	@Override
	public Class<?> getReturnClass()
	{
		return arrayType;
	}
	
	@Override
	public ResolvedType getReturnType()
	{
		return null;
	}
	
	@Override
	public Object get(ErrorHandler errors, Context context, Object root, Object instance)
	{
		Object result = Array.newInstance(componentType, values.length);
		for(int i=0, n=values.length; i<n; i++)
		{
			Object v = values[i].get(errors, context, root, root);
			Array.set(result, i, v);
		}
		
		return result;
	}
	
	@Override
	public boolean supportsGet()
	{
		return true;
	}
	
	@Override
	public void set(ErrorHandler errors, Context context, Object root,
			Object instance, Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
	}
	
	@Override
	public boolean supportsSet()
	{
		return true;
	}
	
	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		StringBuilder builder = new StringBuilder();
		builder
			.append("(new ")
			.append(compiler.castNoParens(getReturnClass()))
			.append(" {");
		
		for(int i=0, n=values.length; i<n; i++)
		{
			if(i > 0) builder.append(", ");
			
			Invoker value = values[i];
			
			String getter = value.toJavaGetter(errors, compiler, compiler.getRootContext());
			
			if(value.getReturnClass().isPrimitive() && ! componentType.isPrimitive())
			{
				// Wrap if non-primitive array with primitive input
				builder.append(compiler.wrap(value.getReturnClass(), getter));
			}
			else
			{
				builder.append(getter);				
			}
		}
		
		builder.append("})");
		return builder.toString();
	}
	
	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		return null;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[componentType=" + componentType 
			+ ", arrayType=" + arrayType + ", values=" + Arrays.toString(values) 
			+ "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arrayType == null)
			? 0
			: arrayType.hashCode());
		result = prime * result + ((componentType == null)
			? 0
			: componentType.hashCode());
		result = prime * result + Arrays.hashCode(values);
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
		ArrayInvoker other = (ArrayInvoker) obj;
		if(arrayType == null)
		{
			if(other.arrayType != null)
				return false;
		}
		else if(!arrayType.equals(other.arrayType))
			return false;
		if(componentType == null)
		{
			if(other.componentType != null)
				return false;
		}
		else if(!componentType.equals(other.componentType))
			return false;
		if(!Arrays.equals(values, other.values))
			return false;
		return true;
	}
}

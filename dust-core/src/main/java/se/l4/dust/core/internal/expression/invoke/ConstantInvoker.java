package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.api.Context;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.primitives.Primitives;

/**
 * Invoker that holds a constant value.
 *
 * @author Andreas Holstenson
 *
 */
public class ConstantInvoker
	implements Invoker
{
	private final Node node;
	private final Object value;

	public ConstantInvoker(Node node, Object value)
	{
		this.node = node;
		this.value = value;
	}

	@Override
	public Node getNode()
	{
		return node;
	}

	@Override
	public Class<?> getReturnClass()
	{
		return value == null ? void.class : Primitives.unwrap(value.getClass());
	}

	@Override
	public ResolvedType getReturnType()
	{
		return null;
	}

	@Override
	public Object get(ErrorHandler errors, Context context, Object root, Object instance)
	{
		return value;
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
		return false;
	}

	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		if(value == null)
		{
			return "null";
		}
		else if(value instanceof Number)
		{
			if(value instanceof Long)
			{
				return ((Number) value).longValue() + "l";
			}
			else
			{
				return String.valueOf(((Number) value).doubleValue());
			}
		}
		else if(value instanceof Boolean)
		{
			return String.valueOf(((Boolean) value).booleanValue());
		}
		else
		{
			return compiler.addInput((Class) value.getClass(), value);
		}
	}

	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		return null;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ConstantInvoker other = (ConstantInvoker) obj;
		if(value == null)
		{
			if(other.value != null)
				return false;
		}
		else if(!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "ConstantInvoker{value=" + value + "}";
	}
}

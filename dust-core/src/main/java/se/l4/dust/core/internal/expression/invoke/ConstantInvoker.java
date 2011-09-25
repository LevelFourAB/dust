package se.l4.dust.core.internal.expression.invoke;

import java.lang.reflect.Type;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

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
		return value == null ? void.class : value.getClass();
	}
	
	@Override
	public Type getReturnType()
	{
		return getReturnClass();
	}
	
	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		return value;
	}
	
	@Override
	public void set(ErrorHandler errors, Object root, Object instance,
			Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
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
}

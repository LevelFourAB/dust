package se.l4.dust.core.internal.expression.invoke;

import java.lang.reflect.Type;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

/**
 * Invoker for a chain of properties or methods.
 * 
 * @author Andreas Holstenson
 *
 */
public class ChainInvoker
	implements Invoker
{
	private final Node node;
	private final Invoker left;
	private final Invoker right;

	public ChainInvoker(Node node, Invoker left, Invoker right)
	{
		this.node = node;
		this.left = left;
		this.right = right;
	}
	
	@Override
	public Class<?> getReturnClass()
	{
		return right.getReturnClass();
	}
	
	@Override
	public Type getReturnType()
	{
		return right.getReturnType();
	}
	
	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		Object result = left.interpret(errors, root, instance);
		return right.interpret(errors, root, result);
	}
	
	@Override
	public void set(ErrorHandler errors, Object root, Object instance, Object value)
	{
		Object result = left.interpret(errors, root, instance);
		right.set(errors, root, result, value);
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
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		ChainInvoker other = (ChainInvoker) obj;
		if(left == null)
		{
			if(other.left != null)
				return false;
		}
		else if(!left.equals(other.left))
			return false;
		if(right == null)
		{
			if(other.right != null)
				return false;
		}
		else if(!right.equals(other.right))
			return false;
		return true;
	}
}

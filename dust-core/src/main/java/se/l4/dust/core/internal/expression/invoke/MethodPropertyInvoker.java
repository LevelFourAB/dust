package se.l4.dust.core.internal.expression.invoke;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

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
	private final Method method;

	public MethodPropertyInvoker(Node node, Method method)
	{
		this.node = node;
		this.method = method;
	}
	
	@Override
	public Class<?> getResult()
	{
		return method.getReturnType();
	}
	
	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		if(instance == null)
		{
			throw errors.error(node, "Object is null, can't fetch a property on a null object");
		}
		
		try
		{
			return method.invoke(instance);
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
		if(method == null)
		{
			if(other.method != null)
				return false;
		}
		else if(!method.equals(other.method))
			return false;
		return true;
	}
}

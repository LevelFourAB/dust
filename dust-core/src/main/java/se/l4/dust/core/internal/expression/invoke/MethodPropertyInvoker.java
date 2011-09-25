package se.l4.dust.core.internal.expression.invoke;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

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
	private final Method getter;
	private final Method setter;
	private final Class<?> returnClass;

	public MethodPropertyInvoker(Node node, Class<?> type, Method getter, Method setter)
	{
		this.node = node;
		
		this.returnClass = type;
		this.getter = getter;
		this.setter = setter;
	}
	
	@Override
	public Class<?> getReturnClass()
	{
		return returnClass;
	}
	
	@Override
	public Type getReturnType()
	{
		return getter.getGenericReturnType();
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
			return getter.invoke(instance);
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
	public void set(ErrorHandler errors, Object root, Object instance,
			Object value)
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

package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

/**
 * Invoker that handles the this keyword.
 * 
 * @author Andreas Holstenson
 *
 */
public class ThisInvoker
	implements Invoker
{
	private final Node node;
	private final Class<?> context;

	public ThisInvoker(Node node, Class<?> context)
	{
		this.node = node;
		this.context = context;
	}
	
	@Override
	public Node getNode()
	{
		return node;
	}
	
	@Override
	public Class<?> getResult()
	{
		return context;
	}
	
	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		return root;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
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
		ThisInvoker other = (ThisInvoker) obj;
		if(context == null)
		{
			if(other.context != null)
				return false;
		}
		else if(!context.equals(other.context))
			return false;
		return true;
	}
	
	@Override
	public void set(ErrorHandler errors, Object root, Object instance,
			Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
	}
}

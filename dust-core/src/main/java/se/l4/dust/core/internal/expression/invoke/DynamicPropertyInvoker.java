package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.api.expression.DynamicProperty;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

/**
 * Invoker that wraps {@link DynamicProperty}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DynamicPropertyInvoker
	implements Invoker
{
	private final Node node;
	private final DynamicProperty property;

	public DynamicPropertyInvoker(Node node, DynamicProperty property)
	{
		this.node = node;
		this.property = property;
	}

	@Override
	public Class<?> getResult()
	{
		return property.getType();
	}

	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		return property.getValue(null, instance);
	}
	
	@Override
	public void set(ErrorHandler errors, Object root, Object instance,
			Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
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
		result = prime * result
				+ ((property == null) ? 0 : property.hashCode());
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
		DynamicPropertyInvoker other = (DynamicPropertyInvoker) obj;
		if(property == null)
		{
			if(other.property != null)
				return false;
		}
		else if(!property.equals(other.property))
			return false;
		return true;
	}
}

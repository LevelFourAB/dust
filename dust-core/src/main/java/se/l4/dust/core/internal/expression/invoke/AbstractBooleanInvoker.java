package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

/**
 * Abstract base class for boolean operations.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class AbstractBooleanInvoker
	implements Invoker
{
	private final Node node;
	private final Invoker left;
	private final Invoker right;

	public AbstractBooleanInvoker(Node node, Invoker left, Invoker right)
	{
		this.node = node;
		this.left = left;
		this.right = right;
	}
	
	@Override
	public Node getNode()
	{
		return node;
	}
	
	@Override
	public Class<?> getResult()
	{
		return Boolean.class;
	}
	
	protected <T> T castLeftNotNull(ErrorHandler errors, Object value)
	{
		return castNotNull(errors, left.getNode(), value);
	}
	
	protected <T> T castRightNotNull(ErrorHandler errors, Object value)
	{
		return castNotNull(errors, right.getNode(), value);
	}
	
	protected <T> T castNotNull(ErrorHandler errors, Node node, Object value)
	{
		if(value == null)
		{
			throw errors.error(node, "Result of expression was null");
		}
		
		return (T) value;
	}
	
	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		Object lv = left.interpret(errors, root, instance);
		Object rv = right.interpret(errors, root, instance);
		return check(errors, lv, rv);
	}
	
	protected abstract boolean check(ErrorHandler errors, Object left, Object right);
}

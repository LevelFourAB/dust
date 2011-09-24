package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

/**
 * Invoker for ternary ifs.
 * 
 * @author Andreas Holstenson
 *
 */
public class TernaryInvoker
	implements Invoker
{
	private final Node node;
	private final Invoker test;
	private final Invoker left;
	private final Invoker right;

	public TernaryInvoker(Node node, Invoker test, Invoker left, Invoker right)
	{
		this.node = node;
		this.test = test;
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
		if(right == null)
		{
			return left.getResult();
		}
		else if(left.getResult().isAssignableFrom(right.getResult()))
		{
			return left.getResult();
		}
		else if(right.getResult().isAssignableFrom(left.getResult()))
		{
			return right.getResult();
		}
		
		// TODO: Better guessing for the return type
		return Object.class;
	}
	
	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		Object result = test.interpret(errors, root, instance);
		if(Boolean.TRUE.equals(result))
		{
			return left.interpret(errors, root, instance);
		}
		else if(right == null)
		{
			return null;
		}
		else
		{
			return right.interpret(errors, root, instance);
		}
	}
	
	@Override
	public void set(ErrorHandler errors, Object root, Object instance,
			Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
	}
}

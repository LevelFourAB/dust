package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

/**
 * Invoker that negates a value.
 * 
 * @author Andreas Holstenson
 *
 */
public class NegateInvoker
	implements Invoker
{
	private final Node node;
	private final Invoker wrapped;

	public NegateInvoker(Node node, Invoker wrapped)
	{
		this.node = node;
		this.wrapped = wrapped;
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
	
	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		Object result = wrapped.interpret(errors, root, instance);
		if(result == null)
		{
			throw errors.error(wrapped.getNode(), "Result of invocation was null but should have returned a boolean");
		}
		
		return ! ((Boolean) result).booleanValue();
	}
}

package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

/**
 * Invoker for and operations.
 * 
 * @author Andreas Holstenson
 *
 */
public class AndInvoker
	extends AbstractBooleanInvoker
{

	public AndInvoker(Node node, Invoker left, Invoker right)
	{
		super(node, left, right);
	}

	@Override
	protected boolean check(ErrorHandler errors, Object left, Object right)
	{
		Boolean lb = castLeftNotNull(errors, left);
		Boolean rb = castRightNotNull(errors, right);
		return lb && rb;
	}
	
}

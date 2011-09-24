package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

/**
 * Invoker for OR operations.
 * 
 * @author Andreas Holstenson
 *
 */
public class OrInvoker
	extends AbstractBooleanInvoker
{

	public OrInvoker(Node node, Invoker left, Invoker right)
	{
		super(node, left, right);
	}

	@Override
	protected boolean check(ErrorHandler errors, Object left, Object right)
	{
		Boolean lb = castLeftNotNull(errors, left);
		Boolean rb = castLeftNotNull(errors, right);
		return lb || rb;
	}

}

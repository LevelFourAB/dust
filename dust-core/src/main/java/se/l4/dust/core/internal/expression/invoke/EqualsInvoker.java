package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

/**
 * Invoker that checks for equality.
 *
 * @author Andreas Holstenson
 *
 */
public class EqualsInvoker
	extends AbstractBooleanInvoker
{

	public EqualsInvoker(Node node, Invoker left, Invoker right)
	{
		super(node, left, right);
	}

	@Override
	protected boolean check(ErrorHandler errors, Object left, Object right)
	{
		if(left == null && right == null)
		{
			return true;
		}
		else if(left == null || right == null)
		{
			return false;
		}

		return left.equals(right);
	}

	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		if(left.getReturnClass().isPrimitive() && right.getReturnClass().isPrimitive())
		{
			return "(" + left.toJavaGetter(errors, compiler, context) + " == " + right.toJavaGetter(errors, compiler, context) + ")";
		}

		String l = InvokerMethods.class.getName() + ".equals("
			+ compiler.wrap(left.getReturnClass(), left.toJavaGetter(errors, compiler, context))
			+ ","
			+ compiler.wrap(right.getReturnClass(), right.toJavaGetter(errors, compiler, context)) + ")";

		return l;
	}
}

package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.api.Context;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

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
	public Class<?> getReturnClass()
	{
		return context;
	}

	@Override
	public ResolvedType getReturnType()
	{
		return null;
	}

	@Override
	public Object get(ErrorHandler errors, Context context, Object root, Object instance)
	{
		return root;
	}

	@Override
	public boolean supportsGet()
	{
		return true;
	}

	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		// $2 is the second parameter which is the instance
		return "$2";
	}

	@Override
	public void set(ErrorHandler errors, Context context, Object root,
			Object instance, Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
	}

	@Override
	public boolean supportsSet()
	{
		return false;
	}

	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		return null;
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
}

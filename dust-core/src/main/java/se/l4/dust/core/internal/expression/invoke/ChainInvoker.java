package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.api.Context;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

/**
 * Invoker for a chain of properties or methods.
 *
 * @author Andreas Holstenson
 *
 */
public class ChainInvoker
	implements Invoker
{
	private final Node node;
	private final Invoker left;
	private final Invoker right;

	public ChainInvoker(Node node, Invoker left, Invoker right)
	{
		this.node = node;
		this.left = left;
		this.right = right;
	}

	@Override
	public Class<?> getReturnClass()
	{
		return right.getReturnClass();
	}

	@Override
	public ResolvedType getReturnType()
	{
		return right.getReturnType();
	}

	@Override
	public Object get(ErrorHandler errors, Context context, Object root, Object instance)
	{
		try
		{
			Object result = left.get(errors, context, root, instance);
			return right.get(errors, context, root, result);
		}
		catch(Throwable t)
		{
			throw errors.error(node, t);
		}
	}

	@Override
	public boolean supportsGet()
	{
		return left.supportsGet() && right.supportsGet();
	}

	@Override
	public void set(ErrorHandler errors, Context context, Object root, Object instance, Object value)
	{
		try
		{
			Object result = left.get(errors, context, root, instance);
			right.set(errors, context, root, result, value);
		}
		catch(Throwable t)
		{
			throw errors.error(node, t);
		}
	}

	@Override
	public boolean supportsSet()
	{
		return left.supportsGet() && right.supportsSet();
	}

	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		context = left.toJavaGetter(errors, compiler, context);
		return right.toJavaGetter(errors, compiler, context);
	}

	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		context = left.toJavaGetter(errors, compiler, context);
		return right.toJavaSetter(errors, compiler, context);
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
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		ChainInvoker other = (ChainInvoker) obj;
		if(left == null)
		{
			if(other.left != null)
				return false;
		}
		else if(!left.equals(other.left))
			return false;
		if(right == null)
		{
			if(other.right != null)
				return false;
		}
		else if(!right.equals(other.right))
			return false;
		return true;
	}

	public Invoker getLeft()
	{
		return left;
	}

	public Invoker getRight()
	{
		return right;
	}

	@Override
	public String toString()
	{
		return "ChainInvoker{left=" + left + ", right=" + right + "}";
	}
}

package se.l4.dust.core.internal.expression.invoke;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.base.Defaults;

import se.l4.dust.api.Context;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
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
	public Class<?> getReturnClass()
	{
		if(right == null)
		{
			return left.getReturnClass();
		}
		else if(left.getReturnClass().isAssignableFrom(right.getReturnClass()))
		{
			return left.getReturnClass();
		}
		else if(right.getReturnClass().isAssignableFrom(left.getReturnClass()))
		{
			return right.getReturnClass();
		}

		// TODO: Better guessing for the return type
		return Object.class;
	}

	@Override
	public ResolvedType getReturnType()
	{
		if(right == null)
		{
			return left.getReturnType();
		}
		else if(left.getReturnClass().isAssignableFrom(right.getReturnClass()))
		{
			return left.getReturnType();
		}
		else if(right.getReturnClass().isAssignableFrom(left.getReturnClass()))
		{
			return right.getReturnType();
		}

		// TODO: Better guessing for the return type
		return null;
	}

	@Override
	public Object get(ErrorHandler errors, Context context, Object root, Object instance)
	{
		try
		{
			Object result = test.get(errors, context, root, instance);
			if(Boolean.TRUE.equals(result))
			{
				return left.get(errors, context, root, instance);
			}
			else if(right == null)
			{
				return null;
			}
			else
			{
				return right.get(errors, context, root, instance);
			}
		}
		catch(Throwable t)
		{
			throw errors.error(node, t);
		}
	}

	@Override
	public boolean supportsGet()
	{
		return true;
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
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		Class<?> type = getReturnClass();
		return "(" + compiler.cast(type) + " ("
			+ test.toJavaGetter(errors, compiler, context)
			+ " ? "
			+ compiler.castOrWrap(type, left.toJavaGetter(errors, compiler, context), left.getReturnClass())
			+ " : "
			+ (right == null ? String.valueOf(Defaults.defaultValue(type)) :
				compiler.castOrWrap(type, right.toJavaGetter(errors, compiler, context), right.getReturnClass())
			)
			+ "))";
	}

	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		return null;
	}
}

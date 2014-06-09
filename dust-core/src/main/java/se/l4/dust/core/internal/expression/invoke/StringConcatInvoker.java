package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.api.Context;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

public class StringConcatInvoker
	implements Invoker
{
	private final Node node;
	private final Invoker left;
	private final Invoker right;

	public StringConcatInvoker(Node node, Invoker left, Invoker right)
	{
		this.node = node;
		this.left = left;
		this.right = right;
	}

	@Override
	public Class<?> getReturnClass()
	{
		return String.class;
	}
	
	@Override
	public ResolvedType getReturnType()
	{
		return null;
	}

	@Override
	public Object get(ErrorHandler errors, Context context, Object root, Object instance)
	{
		try
		{
			Object lv = left.get(errors, context, root, instance);
			Object rv = right.get(errors, context, root, instance);
			return String.valueOf(lv) + String.valueOf(rv);
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
		return "(" + left.toJavaGetter(errors, compiler, context) + " + " + right.toJavaGetter(errors, compiler, context) + ")";
	}
	
	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		return null;
	}

	@Override
	public Node getNode()
	{
		return node;
	}

}

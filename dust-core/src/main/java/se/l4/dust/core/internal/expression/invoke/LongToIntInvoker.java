package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.api.Context;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

public class LongToIntInvoker
	implements Invoker
{
	private final Node node;
	private final Invoker input;

	public LongToIntInvoker(Node node, Invoker input)
	{
		this.node = node;
		this.input = input;
	}

	@Override
	public Class<?> getReturnClass()
	{
		return int.class;
	}

	@Override
	public ResolvedType getReturnType()
	{
		return null;
	}

	@Override
	public Object get(ErrorHandler errors, Context context, Object root, Object instance)
	{
		Object value = input.get(errors, context, root, instance);
		return ((Number) value).intValue();
	}

	@Override
	public boolean supportsGet()
	{
		return input.supportsGet();
	}

	@Override
	public void set(ErrorHandler errors, Context context, Object root, Object instance, Object value)
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
		String value = input.toJavaGetter(errors, compiler, context);
		return "((int) " + value + ")";
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

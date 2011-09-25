package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
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
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		Object lv = left.interpret(errors, root, instance);
		Object rv = right.interpret(errors, root, instance);
		return String.valueOf(lv) + String.valueOf(rv);
	}

	@Override
	public void set(ErrorHandler errors, Object root, Object instance,
			Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
	}

	@Override
	public Node getNode()
	{
		return node;
	}

}

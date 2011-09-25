package se.l4.dust.core.internal.expression.invoke;

import java.lang.reflect.Array;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

/**
 * Invoker that gets a specific index from an array.
 * 
 * @author Andreas Holstenson
 *
 */
public class ArrayIndexInvoker
	implements Invoker
{
	private final Node node;
	private final Invoker index;
	private final Class<?> type;

	public ArrayIndexInvoker(Node node, Class<?> type, Invoker index)
	{
		this.node = node;
		this.type = type;
		this.index = index;
	}

	@Override
	public Class<?> getReturnClass()
	{
		return type;
	}

	@Override
	public ResolvedType getReturnType()
	{
		return null;
	}

	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		Number number = (Number) index.interpret(errors, root, root);
		return Array.get(instance, number.intValue());
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

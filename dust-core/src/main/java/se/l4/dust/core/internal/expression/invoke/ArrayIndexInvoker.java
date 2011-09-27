package se.l4.dust.core.internal.expression.invoke;

import java.lang.reflect.Array;

import com.fasterxml.classmate.ResolvedType;

import se.l4.dust.api.Context;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

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
	public Object get(ErrorHandler errors, Context context, Object root, Object instance)
	{
		Number number = (Number) index.get(errors, context, root, root);
		return Array.get(instance, number.intValue());
	}

	@Override
	public void set(ErrorHandler errors, Context context, Object root,
			Object instance, Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
	}
	
	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		return context + "[" + index.toJavaGetter(errors, compiler, context) + "]";
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

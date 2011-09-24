package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.api.conversion.Conversion;
import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

/**
 * Special invoker that will use {@link Conversion} to convert the return
 * value of another invoker.
 * 
 * @author Andreas Holstenson
 *
 */
public class ConvertingInvoker
	implements Invoker
{
	private final Node node;
	private final NonGenericConversion conversion;
	private final Invoker wrapped;

	public ConvertingInvoker(Node node, NonGenericConversion<?, ?> conversion, Invoker wrapped)
	{
		this.node = node;
		this.conversion = conversion;
		this.wrapped = wrapped;
	}

	@Override
	public Class<?> getResult()
	{
		return conversion.getOutput();
	}

	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		Object value = wrapped.interpret(errors, root, instance);
		return conversion.convert(value);
	}

	@Override
	public Node getNode()
	{
		return node;
	}

}

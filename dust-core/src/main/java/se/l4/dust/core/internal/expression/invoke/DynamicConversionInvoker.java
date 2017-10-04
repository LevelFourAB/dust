package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.api.Context;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.primitives.Primitives;

/**
 * Invoker that will attempt to use a dynamic conversion from the return
 * type of the wrapped invoker to a predetermined type.
 *
 * @author Andreas Holstenson
 *
 */
public class DynamicConversionInvoker
	implements Invoker
{
	private final Node node;
	private final Class<?> type;
	private final Invoker wrapped;
	private final TypeConverter converter;

	public DynamicConversionInvoker(TypeConverter converter, Node node, Class<?> type, Invoker wrapped)
	{
		this.converter = converter;
		this.node = node;
		this.type = type;
		this.wrapped = wrapped;
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
		try
		{
			Object result = wrapped.get(errors, context, root, instance);
			return converter.convert(result, type);
		}
		catch(Throwable t)
		{
			throw errors.error(node, t);
		}
	}

	@Override
	public boolean supportsGet()
	{
		return wrapped.supportsGet();
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
	public Node getNode()
	{
		return node;
	}

	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		String in = compiler.addInput(TypeConverter.class, converter);

		Class<?> upper = Primitives.wrap(type);
		String expr = "(" + compiler.cast(upper) + " "
			+ in + ".convert("
			+ compiler.wrap(wrapped.getReturnClass(), wrapped.toJavaGetter(errors, compiler, context))
			+ ", " + type.getName() + ".class))";

		if(type.isPrimitive())
		{
			return compiler.unwrap(upper, expr);
		}
		else
		{
			return expr;
		}
	}

	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		return null;
	}

}

package se.l4.dust.core.internal.expression.invoke;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.primitives.Primitives;

import se.l4.dust.api.Context;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

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
		Object result = wrapped.get(errors, context, root, instance);
		return converter.convert(result, type);
	}

	@Override
	public void set(ErrorHandler errors, Context context, Object root, Object instance, Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
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

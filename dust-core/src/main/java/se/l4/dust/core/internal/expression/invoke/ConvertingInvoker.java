package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.api.Context;
import se.l4.dust.api.conversion.Conversion;
import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

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
	public Class<?> getReturnClass()
	{
		return conversion.getOutput();
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
			Object value = wrapped.get(errors, context, root, instance);
			return conversion.convert(value);
		}
		catch(Throwable t)
		{
			throw errors.error(node, t);
		}
	}
	
	@Override
	public void set(ErrorHandler errors, Context context, Object root,
			Object instance, Object value)
	{
		wrapped.set(errors, context, root, instance, value);
	}
	
	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		return null;
	}
	
	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		String expr = wrapped.toJavaGetter(errors, compiler, context);
		String id = compiler.addInput(Conversion.class, conversion);
		
		return "(" + compiler.cast(getReturnClass()) + " " + id + ".convert(" + compiler.wrap(wrapped.getReturnClass(), expr) + "))";
	}

	@Override
	public Node getNode()
	{
		return node;
	}

}
